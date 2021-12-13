package gregtech.api.gui.widgets;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ISizeProvider;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotWidget extends Widget implements INativeWidget {

    protected final Slot slotReference;
    protected final boolean canTakeItems;
    protected final boolean canPutItems;
    protected SlotLocationInfo locationInfo = new SlotLocationInfo(false, false);

    protected IGuiTexture[] backgroundTexture;
    protected Runnable changeListener;

    public SlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(inventory, slotIndex);
    }

    public SlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(itemHandler, slotIndex);
    }

    @Override
    public void setSizes(ISizeProvider sizes) {
        super.setSizes(sizes);
        onPositionUpdate();
    }

    protected Slot createSlot(IInventory inventory, int index) {
        return new WidgetSlot(inventory, index, 0, 0);
    }

    protected Slot createSlot(IItemHandler itemHandler, int index) {
        return new WidgetSlotItemHandler(itemHandler, index, 0, 0);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && isActive()) {
            ((ISlotWidget) slotReference).setHover(true);
        } else {
            ((ISlotWidget) slotReference).setHover(false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture backgroundTexture : this.backgroundTexture) {
                backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(slotReference.getStack(), pos.x + 1, pos.y + 1);
        itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, slotReference.getStack(), pos.x + 1, pos.y + 1, null);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        if (isActive()) {
            if (slotReference instanceof ISlotWidget) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    GlStateManager.disableDepth();
                    GlStateManager.colorMask(true, true, true, false);
                    drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableBlend();
                }
            }
        } else {
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, 0xbf000000);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (canTakeItems && isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.needNativeClick = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.needNativeClick = true;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    protected void onPositionUpdate() {
        if (slotReference != null && sizes != null) {
            Position position = getPosition();
            this.slotReference.xPos = position.x + 1 - sizes.getGuiLeft();
            this.slotReference.yPos = position.y + 1 - sizes.getGuiTop();
        }
    }

    public SlotWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition) {
        this(inventory, slotIndex, xPosition, yPosition, true, true);
    }

    /**
     * Sets array of background textures used by slot
     * they are drawn on top of each other
     */
    public SlotWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public SlotWidget setLocationInfo(boolean isPlayerInventory, boolean isHotbarSlot) {
        this.locationInfo = new SlotLocationInfo(isPlayerInventory, isHotbarSlot);
        return this;
    }

    @Override
    public SlotLocationInfo getSlotLocationInfo() {
        return locationInfo;
    }

    public boolean canPutStack(ItemStack stack) {
        return isEnabled() && canPutItems;
    }

    public boolean canTakeStack(EntityPlayer player) {
        return isEnabled() && canTakeItems;
    }

    public boolean isEnabled() {
        return this.isActive() && isVisible();
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return isEnabled();
    }

    public void onSlotChanged() {
        gui.holder.markAsDirty();
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        return INativeWidget.VANILLA_LOGIC;
    }

    @Override
    public final Slot getHandle() {
        return slotReference;
    }

    public interface ISlotWidget {
        void setHover(boolean isHover);
        boolean isHover();
    }

    protected class WidgetSlot extends Slot implements ISlotWidget {
        boolean isHover;

        public WidgetSlot(IInventory inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public void setHover(boolean isHover) {
            this.isHover = isHover;
        }

        @Override
        public boolean isHover() {
            return isHover;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.isItemValid(stack);
        }

        @Override
        public boolean canTakeStack(@Nonnull EntityPlayer playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.canTakeStack(playerIn);
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            super.putStack(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @Nonnull
        @Override
        public final ItemStack onTake(@Nonnull EntityPlayer thePlayer, @Nonnull ItemStack stack) {
            return onItemTake(thePlayer, super.onTake(thePlayer, stack), false);
        }

        @Override
        public void onSlotChanged() {
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isEnabled() {
            return SlotWidget.this.isEnabled();
        }

    }

    protected class WidgetSlotItemHandler extends SlotItemHandler implements ISlotWidget {
        boolean isHover;

        public WidgetSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public void setHover(boolean isHover) {
            this.isHover = isHover;
        }

        @Override
        public boolean isHover() {
            return isHover;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.isItemValid(stack);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.canTakeStack(playerIn);
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            super.putStack(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @Nonnull
        @Override
        public final ItemStack onTake(@Nonnull EntityPlayer thePlayer, @Nonnull ItemStack stack) {
            return onItemTake(thePlayer, super.onTake(thePlayer, stack), false);
        }

        @Override
        public void onSlotChanged() {
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isEnabled() {
            return SlotWidget.this.isEnabled();
        }

    }
}
