package gregtech.common.mui.widget.workbench;

import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.IOnSlotChanged;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CraftingInputSlot extends Widget<CraftingOutputSlot> implements Interactable,
                               JeiGhostIngredientSlot<ItemStack>,
                               JeiIngredientProvider {

    private final InputSyncHandler syncHandler;
    public boolean hasIngredients = true;

    public CraftingInputSlot(IItemHandlerModifiable handler, int index) {
        this.syncHandler = new InputSyncHandler(handler, index);
        setSyncHandler(this.syncHandler);
        tooltip().setAutoUpdate(true);
        // .setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = this.syncHandler.getStack();
            if (stack.isEmpty()) return;
            tooltip.addFromItem(stack);
            // tooltip.addStringLines(getScreen().getScreenWrapper().getItemToolTip(stack));
        });
    }

    public static CraftingInputSlot create(CraftingRecipeLogic logic, IItemHandlerModifiable handler, int index) {
        var slot = new CraftingInputSlot(handler, index);
        logic.setInputSlot(slot, index);
        return slot;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof InputSyncHandler;
    }

    @Override
    public void onInit() {
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    public CraftingInputSlot changeListener(IOnSlotChanged listener) {
        this.syncHandler.listener = listener;
        return this;
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        if (!this.syncHandler.isValid())
            return Result.IGNORE;

        this.syncHandler.syncStack();
        return Result.SUCCESS;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack itemstack = this.syncHandler.getStack();
        if (itemstack.isEmpty()) return;

        if (!this.hasIngredients) {
            RenderUtil.renderRect(0, 0, 18, 18, 200, 0x80FF0000);
        }

        GuiDraw.drawItem(itemstack, 1, 1, 16, 16);
        var renderer = MCHelper.getMc().getRenderItem();
        renderer.renderItemOverlayIntoGUI(MCHelper.getFontRenderer(), itemstack, 1, 1, null);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), this.syncHandler.getStack());
        }
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        syncHandler.setStack(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof ItemStack ? (ItemStack) ingredient : null;
    }

    @Override
    public @NotNull ItemStack getIngredient() {
        return this.getStack();
    }

    public ItemStack getStack() {
        return syncHandler.getStack();
    }

    public int getIndex() {
        return syncHandler.index;
    }

    public void setStack(ItemStack stack) {
        this.syncHandler.setStack(stack);
    }

    @SuppressWarnings("OverrideOnly")
    protected static class InputSyncHandler extends SyncHandler {

        private final IItemHandlerModifiable handler;
        private final int index;
        private ItemStack lastStoredItem;

        private IOnSlotChanged listener = IOnSlotChanged.DEFAULT;

        public InputSyncHandler(IItemHandlerModifiable handler, int index) {
            this.handler = handler;
            this.index = index;
        }

        @Override
        public void init(String key, PanelSyncManager syncHandler) {
            super.init(key, syncHandler);
            this.lastStoredItem = this.handler.getStackInSlot(this.index).copy();
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == 1) {
                boolean c = buf.readBoolean();
                var s = readStackSafe(buf);
                boolean i = buf.readBoolean();
                this.handler.setStackInSlot(this.index, s);
                this.listener.onChange(s, c, true, i);
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == 1) {
                var b = buf.readBoolean();
                var s = readStackSafe(buf);
                this.handler.setStackInSlot(this.index, s);
                this.listener.onChange(s, b, false, false);
            }
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            ItemStack itemStack = getStack();
            if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
            boolean onlyAmountChanged = false;
            if (init ||
                    !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                    (onlyAmountChanged = itemStack.getCount() != this.lastStoredItem.getCount())) {
                this.listener.onChange(itemStack, onlyAmountChanged, false, init);
                if (onlyAmountChanged) {
                    this.lastStoredItem.setCount(itemStack.getCount());
                } else {
                    this.lastStoredItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();
                }
                final boolean finalOnlyAmountChanged = onlyAmountChanged;
                syncToClient(1, buffer -> {
                    buffer.writeBoolean(finalOnlyAmountChanged);
                    buffer.writeItemStack(itemStack);
                    buffer.writeBoolean(init);
                });
            }
        }

        public void syncStack() {
            final var cursorStack = getSyncManager().getCursorItem().copy();
            cursorStack.setCount(1);
            final var curStack = getStack();
            final boolean onlyAmt = ItemHandlerHelper.canItemStacksStackRelaxed(curStack, cursorStack);

            this.handler.setStackInSlot(this.index, cursorStack);
            this.listener.onChange(cursorStack, onlyAmt, true, false);
            syncToServer(1, buffer -> {
                buffer.writeBoolean(onlyAmt);
                buffer.writeItemStack(cursorStack);
            });
        }

        public ItemStack getStack() {
            return this.handler.getStackInSlot(this.index);
        }

        /**
         * Sets the stack in this slot and calls the onChange listener.
         * 
         * @param stack stack to put into this slot
         */
        public void setStack(ItemStack stack) {
            this.handler.setStackInSlot(this.index, stack);
            this.listener.onChange(stack, false, getSyncManager().isClient(), false);
        }

        private ItemStack readStackSafe(PacketBuffer buffer) {
            ItemStack ret = ItemStack.EMPTY;
            try {
                ret = buffer.readItemStack();
            } catch (IOException ignored) {}
            return ret;
        }
    }
}
