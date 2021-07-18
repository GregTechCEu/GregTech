package gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.ingredient.IIngredientSlot;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class PhantomFluidWidget extends Widget implements IIngredientSlot, IGhostIngredientTarget {

    protected TextureArea backgroundTexture = GuiTextures.FLUID_SLOT;

    private Supplier<FluidStack> fluidStackSupplier;
    private Consumer<FluidStack> fluidStackUpdater;
    protected FluidStack lastFluidStack;

    public PhantomFluidWidget(int xPosition, int yPosition, int width, int height, Supplier<FluidStack> fluidStackSupplier, Consumer<FluidStack> fluidStackUpdater) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.fluidStackSupplier = fluidStackSupplier;
        this.fluidStackUpdater = fluidStackUpdater;
    }

    private FluidStack drainFrom(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) ingredient;
            IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null)
                return fluidHandler.drain(Integer.MAX_VALUE, false);
        }
        return null;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null) {
            return Collections.emptyList();
        }

        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(Object ingredient) {
                FluidStack ingredientStack;
                if (ingredient instanceof FluidStack)
                    ingredientStack = (FluidStack) ingredient;
                else
                    ingredientStack = drainFrom(ingredient);

                if (ingredientStack != null) {
                    NBTTagCompound tagCompound = ingredientStack.writeToNBT(new NBTTagCompound());
                    writeClientAction(2, buffer -> buffer.writeCompoundTag(tagCompound));
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

    public PhantomFluidWidget setBackgroundTexture(TextureArea backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
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
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            if (buffer.readBoolean()) {
                try {
                    NBTTagCompound tagCompound = buffer.readCompoundTag();
                    this.lastFluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.lastFluidStack = null;
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ItemStack itemStack = gui.entityPlayer.inventory.getItemStack().copy();
            if (!itemStack.isEmpty()) {
                itemStack.setCount(1);
                IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    FluidStack resultFluid = fluidHandler.drain(Integer.MAX_VALUE, false);
                    fluidStackUpdater.accept(resultFluid);
                }
            } else {
                fluidStackUpdater.accept(null);
            }
        } else if (id == 2) {
            FluidStack fluidStack;
            try {
                fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fluidStackUpdater.accept(fluidStack);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(1, buffer -> {
            });
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
        }
        if (lastFluidStack != null) {
            GlStateManager.disableBlend();
            RenderUtil.drawFluidForGui(lastFluidStack, lastFluidStack.amount, pos.x + 1, pos.y + 1, size.width - 1, size.height - 1);
            GlStateManager.enableBlend();
            GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (lastFluidStack != null) {
                String fluidName = lastFluidStack.getLocalizedName();
                drawHoveringText(ItemStack.EMPTY, Lists.newArrayList(fluidName), -1, mouseX, mouseY);
            }
        }
    }
}
