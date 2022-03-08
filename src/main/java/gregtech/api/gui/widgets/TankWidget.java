package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IIngredientSlot;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.*;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TankWidget extends Widget implements IIngredientSlot {

    public final IFluidTank fluidTank;

    public int fluidRenderOffset = 1;
    private boolean hideTooltip;
    private boolean alwaysShowFull;
    private boolean drawHoveringText;

    private boolean allowClickFilling;
    private boolean allowClickEmptying;

    private IGuiTexture[] backgroundTexture;
    private IGuiTexture overlayTexture;

    protected FluidStack lastFluidInTank;
    private int lastTankCapacity;
    protected boolean isClient;

    public TankWidget(IFluidTank fluidTank, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.drawHoveringText = true;
    }

    public TankWidget setClient() {
        this.isClient = true;
        this.lastFluidInTank = fluidTank != null ? fluidTank.getFluid() != null ? fluidTank.getFluid().copy() : null : null;
        this.lastTankCapacity = fluidTank != null ? fluidTank.getCapacity() : 0;
        return this;
    }

    public TankWidget setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public TankWidget setDrawHoveringText(boolean drawHoveringText) {
        this.drawHoveringText = drawHoveringText;
        return this;
    }

    public TankWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public TankWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public TankWidget setOverlayTexture(IGuiTexture overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public TankWidget setFluidRenderOffset(int fluidRenderOffset) {
        this.fluidRenderOffset = fluidRenderOffset;
        return this;
    }

    public TankWidget setContainerClicking(boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        if (!(fluidTank instanceof IFluidHandler))
            throw new IllegalStateException("Container IO is only supported for fluid tanks that implement IFluidHandler");
        this.allowClickFilling = allowClickContainerFilling;
        this.allowClickEmptying = allowClickContainerEmptying;
        return this;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank;
        }
        return null;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", lastFluidInTank == null ? 0 : lastFluidInTank.amount);
    }

    public String getFluidLocalizedName() {
        return lastFluidInTank == null ? "" : lastFluidInTank.getLocalizedName();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture textureArea : backgroundTexture) {
                textureArea.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        //do not draw fluids if they are handled by JEI - it draws them itself
        if (lastFluidInTank != null && !gui.isJEIHandled) {
            GlStateManager.disableBlend();
            FluidStack stackToDraw = lastFluidInTank;
            int drawAmount = alwaysShowFull ? lastFluidInTank.amount : lastTankCapacity;
            if (alwaysShowFull && lastFluidInTank.amount == 0) {
                stackToDraw = lastFluidInTank.copy();
                stackToDraw.amount = 1;
                drawAmount = 1;
            }
            RenderUtil.drawFluidForGui(stackToDraw, drawAmount,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset,
                    size.width - fluidRenderOffset, size.height - fluidRenderOffset);

            if (alwaysShowFull && !hideTooltip && drawHoveringText) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);

                String s = TextFormattingUtil.formatLongToCompactString(lastFluidInTank.amount, 4) + "L";

                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3)) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.y + (size.height / 3) + 6) * 2, 0xFFFFFF);
                GlStateManager.popMatrix();
            }
            GlStateManager.enableBlend();
        }
        if (overlayTexture != null) {
            overlayTexture.draw(pos.x, pos.y, size.width, size.height);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (!hideTooltip && !gui.isJEIHandled && isMouseOverElement(mouseX, mouseY)) {
            List<String> tooltips = new ArrayList<>();
            if (lastFluidInTank != null) {
                Fluid fluid = lastFluidInTank.getFluid();
                tooltips.add(fluid.getLocalizedName(lastFluidInTank));

                // Amount Tooltip
                tooltips.add(LocalizationUtils.format("gregtech.fluid.amount", lastFluidInTank.amount, lastTankCapacity));

                // Add various tooltips from the material
                List<String> formula = FluidTooltipUtil.getFluidTooltip(lastFluidInTank);
                if (formula != null) {
                    for (String s : formula) {
                        if (s.isEmpty()) continue;
                        tooltips.add(s);
                    }
                }

            } else {
                tooltips.add(LocalizationUtils.format("gregtech.fluid.empty"));
                tooltips.add(LocalizationUtils.format("gregtech.fluid.amount", 0, lastTankCapacity));
            }
            if(allowClickEmptying && allowClickFilling) {
                tooltips.add(""); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_combined"));
            }
            else if (allowClickFilling) {
                tooltips.add(""); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_to_fill"));
            }
            else if (allowClickEmptying) {
                tooltips.add(""); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_to_empty"));
            }
            drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void updateScreen() {
        if (isClient) {
            FluidStack fluidStack = fluidTank.getFluid();
            if (fluidTank.getCapacity() != lastTankCapacity) {
                this.lastTankCapacity = fluidTank.getCapacity();
            }
            if (fluidStack == null && lastFluidInTank != null) {
                this.lastFluidInTank = null;

            } else if (fluidStack != null) {
                if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                    this.lastFluidInTank = fluidStack.copy();
                } else if (fluidStack.amount != lastFluidInTank.amount) {
                    this.lastFluidInTank.amount = fluidStack.amount;
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidTank.getCapacity() != lastTankCapacity) {
            this.lastTankCapacity = fluidTank.getCapacity();
            writeUpdateInfo(0, buffer -> buffer.writeVarInt(lastTankCapacity));
        }
        if (fluidStack == null && lastFluidInTank != null) {
            this.lastFluidInTank = null;
            writeUpdateInfo(1, buffer -> {
            });
        } else if (fluidStack != null) {
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
                writeUpdateInfo(2, buffer -> buffer.writeCompoundTag(fluidStackTag));
            } else if (fluidStack.amount != lastFluidInTank.amount) {
                this.lastFluidInTank.amount = fluidStack.amount;
                writeUpdateInfo(3, buffer -> buffer.writeVarInt(lastFluidInTank.amount));
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            NBTTagCompound fluidStackTag;
            try {
                fluidStackTag = buffer.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            this.lastFluidInTank = FluidStack.loadFluidStackFromNBT(fluidStackTag);
        } else if (id == 3 && lastFluidInTank != null) {
            this.lastFluidInTank.amount = buffer.readVarInt();
        }

        if (id == 4) {
            ItemStack currentStack = gui.entityPlayer.inventory.getItemStack();
            int newStackSize = buffer.readVarInt();
            currentStack.setCount(newStackSize);
            gui.entityPlayer.inventory.setItemStack(currentStack);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            boolean isShiftKeyDown = buffer.readBoolean();
            int clickResult = tryClickContainer(isShiftKeyDown);
            if (clickResult >= 0) {
                writeUpdateInfo(4, buf -> buf.writeVarInt(clickResult));
            }
        }
    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack currentStack = player.inventory.getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (allowClickFilling && fluidTank.getFluidAmount() > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = fluidTank.getFluid();
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryFillContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedFill = true;
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFillSound(initialFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                gui.entityPlayer.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        if (allowClickEmptying) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryEmptyContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedEmptying = true;
            }
            FluidStack filledFluid = fluidTank.getFluid();
            if (performedEmptying && filledFluid != null) {
                SoundEvent soundevent = filledFluid.getFluid().getEmptySound(filledFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                gui.entityPlayer.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ItemStack currentStack = gui.entityPlayer.inventory.getItemStack();
            if (button == 0 && (allowClickEmptying || allowClickFilling) &&
                    currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                boolean isShiftKeyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                writeClientAction(1, writer -> writer.writeBoolean(isShiftKeyDown));
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }
}
