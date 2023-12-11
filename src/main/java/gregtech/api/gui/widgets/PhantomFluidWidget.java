package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.ingredient.IIngredientSlot;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.*;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gregtech.api.util.GTUtility.getFluidFromContainer;

public class PhantomFluidWidget extends Widget implements IIngredientSlot, IGhostIngredientTarget {

    private FluidTank fluidTank = null;
    protected IGuiTexture backgroundTexture = GuiTextures.FLUID_SLOT;

    private Supplier<FluidStack> fluidStackSupplier;
    private Consumer<FluidStack> fluidStackUpdater;
    private Supplier<Boolean> showTipSupplier;
    private boolean isClient;
    private boolean showTip;
    protected FluidStack lastFluidStack;

    public PhantomFluidWidget(int xPosition, int yPosition, int width, int height,
                              Supplier<FluidStack> fluidStackSupplier, Consumer<FluidStack> fluidStackUpdater) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.fluidStackSupplier = fluidStackSupplier;
        this.fluidStackUpdater = fluidStackUpdater;
    }

    public PhantomFluidWidget(int xPosition, int yPosition, int width, int height, FluidTank fluidTank) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.fluidTank = fluidTank;
        this.fluidStackSupplier = fluidTank::getFluid;
        this.fluidStackUpdater = fluidTank::setFluid;
    }

    public PhantomFluidWidget showTip(boolean showTip) {
        this.showTip = showTip;
        return this;
    }

    public PhantomFluidWidget showTipSupplier(Supplier<Boolean> showTipSupplier) {
        this.showTipSupplier = showTipSupplier;
        return this;
    }

    public PhantomFluidWidget setFluidStackSupplier(Supplier<FluidStack> fluidStackSupplier, boolean isClient) {
        this.fluidStackSupplier = fluidStackSupplier;
        this.isClient = isClient;
        return this;
    }

    public PhantomFluidWidget setFluidStackUpdater(Consumer<FluidStack> fluidStackUpdater, boolean isClient) {
        this.fluidStackUpdater = fluidStackUpdater;
        this.isClient = isClient;
        return this;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof FluidStack) && getFluidFromContainer(ingredient) == null) {
            return Collections.emptyList();
        }

        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {

            @NotNull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                FluidStack ingredientStack;
                if (ingredient instanceof FluidStack)
                    ingredientStack = (FluidStack) ingredient;
                else
                    ingredientStack = getFluidFromContainer(ingredient);

                if (ingredientStack != null) {
                    NBTTagCompound tagCompound = ingredientStack.writeToNBT(new NBTTagCompound());
                    writeClientAction(2, buffer -> buffer.writeCompoundTag(tagCompound));
                }

                if (isClient && fluidStackUpdater != null) {
                    fluidStackUpdater.accept(ingredientStack);
                }
            }
        });
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidStack;
        }
        return null;
    }

    public PhantomFluidWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClient && fluidStackSupplier != null) {
            this.lastFluidStack = fluidStackSupplier.get();
        }
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack currentStack = fluidStackSupplier.get();
        if (currentStack == null && lastFluidStack != null) {
            this.lastFluidStack = null;
            writeUpdateInfo(1, buffer -> buffer.writeBoolean(false));
        } else if (currentStack != null && !currentStack.isFluidStackIdentical(lastFluidStack)) {
            this.lastFluidStack = currentStack;
            writeUpdateInfo(1, buffer -> {
                buffer.writeBoolean(true);
                buffer.writeCompoundTag(currentStack.writeToNBT(new NBTTagCompound()));
            });
        }
        if (showTipSupplier != null && showTip != showTipSupplier.get()) {
            showTip = showTipSupplier.get();
            writeUpdateInfo(2, buffer -> buffer.writeBoolean(showTip));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            if (buffer.readBoolean()) {
                try {
                    NBTTagCompound tagCompound = buffer.readCompoundTag();
                    this.lastFluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
                } catch (IOException e) {
                    GTLog.logger.error("Could not read NBT from PhantomFluidWidget buffer", e);
                }
            } else {
                this.lastFluidStack = null;
            }
        } else if (id == 2) {
            this.showTip = buffer.readBoolean();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            ItemStack itemStack = gui.entityPlayer.inventory.getItemStack().copy();
            if (!itemStack.isEmpty()) {
                itemStack.setCount(1);
                IFluidHandlerItem fluidHandler = itemStack
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    FluidStack resultFluid = fluidHandler.drain(Integer.MAX_VALUE, false);
                    fluidStackUpdater.accept(resultFluid);
                }
            } else {
                if (showTip) {
                    if (clickData.button == 2) {
                        fluidStackUpdater.accept(null);
                    } else if (clickData.button == 0) {
                        if (fluidStackSupplier.get() != null) {
                            FluidStack fluid = fluidStackSupplier.get().copy();
                            if (clickData.isShiftClick)
                                fluid.amount = (fluid.amount + 1) / 2;
                            else fluid.amount -= 1;
                            if (fluid.amount < 0) {
                                fluid.amount = Integer.MAX_VALUE / 2;
                            }
                            fluid.amount = MathHelper.clamp(fluid.amount, 1, fluidTank.getCapacity());
                            fluidStackUpdater.accept(fluid);
                        }
                    } else if (clickData.button == 1) {
                        if (fluidStackSupplier.get() != null) {
                            FluidStack fluid = fluidStackSupplier.get().copy();
                            if (clickData.isShiftClick)
                                fluid.amount *= 2;
                            else fluid.amount += 1;
                            if (fluid.amount < 0) {
                                fluid.amount = Integer.MAX_VALUE;
                            }
                            fluid.amount = MathHelper.clamp(fluid.amount, 1, fluidTank.getCapacity());
                            fluidStackUpdater.accept(fluid);
                        }
                    }
                } else {
                    fluidStackUpdater.accept(null);
                }
            }
        } else if (id == 2) {
            FluidStack fluidStack;
            try {
                fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fluidStackUpdater.accept(fluidStack);
        } else if (id == 3) {
            WheelData wheelData = WheelData.readFromBuf(buffer);
            if (fluidStackSupplier.get() != null && fluidStackUpdater != null && showTip) {
                int multiplier = wheelData.isCtrlClick ? 100 : 1;
                multiplier *= wheelData.isShiftClick ? 10 : 1;
                FluidStack currentFluid = fluidStackSupplier.get().copy();
                int amount = wheelData.wheelDelta * multiplier;
                currentFluid.amount = MathHelper.clamp(currentFluid.amount + amount, 1, fluidTank.getCapacity());
                fluidStackUpdater.accept(currentFluid);
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData(button, TooltipHelper.isShiftDown(), TooltipHelper.isCtrlDown(), true);
            writeClientAction(1, clickData::writeToBuf);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (showTip) {
                WheelData wheelData = new WheelData(MathHelper.clamp(wheelDelta, -1, 1),
                        TooltipHelper.isShiftDown(), TooltipHelper.isCtrlDown(), true);
                writeClientAction(3, wheelData::writeToBuf);
            }
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
        }
        if (lastFluidStack != null) {
            GlStateManager.disableBlend();
            RenderUtil.drawFluidForGui(lastFluidStack, lastFluidStack.amount, pos.x + 1, pos.y + 1, size.width - 1,
                    size.height - 1);
            if (showTip) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);
                String s = TextFormattingUtil.formatLongToCompactString(lastFluidStack.amount, 4) + "L";
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s,
                        (pos.x + (size.width / 3F)) * 2 - fontRenderer.getStringWidth(s) + 21,
                        (pos.y + (size.height / 3F) + 6) * 2, 0xFFFFFF);
                GlStateManager.popMatrix();
            }
            GlStateManager.enableBlend();
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (lastFluidStack != null) {
                String fluidName = lastFluidStack.getLocalizedName();
                List<String> hoverStringList = new ArrayList<>();
                hoverStringList.add(fluidName);
                if (showTip) {
                    hoverStringList.add(lastFluidStack.amount + " L");
                    Collections.addAll(hoverStringList,
                            LocalizationUtils.formatLines("cover.fluid_filter.config_amount"));
                }
                drawHoveringText(ItemStack.EMPTY, hoverStringList, -1, mouseX, mouseY);
            }
        }
    }
}
