package gregtech.api.gui.widgets;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

import gregtech.api.gui.*;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
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
import java.awt.*;

public class SlotWidget extends Widget implements INativeWidget {

    protected final Slot slotReference;
    protected boolean isEnabled = true;

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
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (isEnabled() && backgroundTexture != null) {
            Position pos = getPosition();
            Size size = getSize();
            for (IGuiTexture backgroundTexture : this.backgroundTexture) {
                backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
            }

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(slotReference.getStack(), pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, slotReference.getStack(), pos.x + 1, pos.y + 1, null);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
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

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public void detectAndSendChanges() {
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
        return this.isEnabled;
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

    protected class WidgetSlot extends Slot {
        public WidgetSlot(IInventory inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
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

    protected class WidgetSlotItemHandler extends SlotItemHandler {

        public WidgetSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
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
