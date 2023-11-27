package gregtech.api.gui.widgets;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ISizeProvider;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SlotWidget extends Widget implements INativeWidget {

    protected final Slot slotReference;
    protected final boolean canTakeItems;
    protected final boolean canPutItems;
    protected SlotLocationInfo locationInfo = new SlotLocationInfo(false, false);

    protected IGuiTexture[] backgroundTexture;
    protected Runnable changeListener;

    protected String tooltipText;
    protected Object[] tooltipArgs;

    protected Consumer<SlotWidget> consumer;

    public SlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                      boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(inventory, slotIndex);
    }

    public SlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                      boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(itemHandler, slotIndex, true);
    }

    public SlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                      boolean canPutItems, boolean canShiftClickInto) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(itemHandler, slotIndex, canShiftClickInto);
    }

    @Override
    public void setSizes(ISizeProvider sizes) {
        super.setSizes(sizes);
        onPositionUpdate();
    }

    protected Slot createSlot(IInventory inventory, int index) {
        return new WidgetSlot(inventory, index, 0, 0);
    }

    protected Slot createSlot(IItemHandler itemHandler, int index, boolean canShiftClickInto) {
        return new WidgetSlotItemHandler(itemHandler, index, 0, 0, canShiftClickInto);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        ((ISlotWidget) slotReference).setHover(isMouseOverElement(mouseX, mouseY) && isActive());
        if (tooltipText != null && isMouseOverElement(mouseX, mouseY) && !slotReference.getHasStack()) {
            List<String> hoverList = Arrays.asList(LocalizationUtils.formatLines(tooltipText, tooltipArgs));
            drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture backgroundTexture : this.backgroundTexture) {
                backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        ItemStack itemStack = slotReference.getStack();
        ModularUIGui modularUIGui = gui == null ? null : gui.getModularUIGui();
        if (itemStack.isEmpty() && modularUIGui != null && modularUIGui.getDragSplitting() &&
                modularUIGui.getDragSplittingSlots().contains(slotReference)) { // draw split
            int splitSize = modularUIGui.getDragSplittingSlots().size();
            itemStack = gui.entityPlayer.inventory.getItemStack();
            if (!itemStack.isEmpty() && splitSize > 1 && Container.canAddItemToSlot(slotReference, itemStack, true)) {
                itemStack = itemStack.copy();
                Container.computeStackSize(modularUIGui.getDragSplittingSlots(), modularUIGui.dragSplittingLimit,
                        itemStack, slotReference.getStack().isEmpty() ? 0 : slotReference.getStack().getCount());
                int k = Math.min(itemStack.getMaxStackSize(), slotReference.getItemStackLimit(itemStack));
                if (itemStack.getCount() > k) {
                    itemStack.setCount(k);
                }
            }
        }
        if (!itemStack.isEmpty()) {
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(itemStack, pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, itemStack, pos.x + 1, pos.y + 1,
                    null);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
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
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ModularUIGui modularUIGui = gui.getModularUIGui();
            boolean last = modularUIGui.getDragSplitting();
            gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            if (last != modularUIGui.getDragSplitting()) {
                modularUIGui.dragSplittingButton = button;
                if (button == 0) {
                    modularUIGui.dragSplittingLimit = 0;
                } else if (button == 1) {
                    modularUIGui.dragSplittingLimit = 1;
                } else if (Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(button - 100)) {
                    modularUIGui.dragSplittingLimit = 2;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseReleased(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseClickMove(mouseX, mouseY, button, timeDragged);
            return true;
        }
        return false;
    }

    @Override
    protected void onPositionUpdate() {
        if (slotReference != null && sizes != null) {
            Position position = getPosition();
            this.slotReference.xPos = position.x + 1 - sizes.getGuiLeft();
            this.slotReference.yPos = position.y + 1 - sizes.getGuiTop();
        }
    }

    public SlotWidget setConsumer(Consumer<SlotWidget> consumer) {
        this.consumer = consumer;
        return this;
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

    public SlotWidget setTooltipText(String tooltipText, Object... args) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        this.tooltipArgs = args;
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
        return null;
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
        public boolean isItemValid(@NotNull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.isItemValid(stack);
        }

        @Override
        public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.canTakeStack(playerIn);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @NotNull
        @Override
        public final ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
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

    public class WidgetSlotItemHandler extends SlotItemHandler implements ISlotWidget {

        boolean isHover;
        final boolean canShiftClickInto;

        public WidgetSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition,
                                     boolean canShiftClickInto) {
            super(itemHandler, index, xPosition, yPosition);
            this.canShiftClickInto = canShiftClickInto;
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
        public boolean isItemValid(@NotNull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.isItemValid(stack);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.canTakeStack(playerIn);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @NotNull
        @Override
        public final ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
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

        public boolean canShiftClickInto() {
            return canShiftClickInto;
        }
    }
}
