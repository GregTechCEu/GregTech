package gregtech.common.mui.widget.workbench;

import gregtech.api.util.GTUtility;
import gregtech.api.util.JEIUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.IOnSlotChanged;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftingInputSlot extends Widget<CraftingOutputSlot> implements Interactable,
                               JeiGhostIngredientSlot<ItemStack>,
                               JeiIngredientProvider {

    private final InputSyncHandler syncHandler;
    public boolean hasIngredients = true;
    private static boolean dragging = false;

    private CraftingInputSlot(IItemHandlerModifiable handler, int index) {
        this.syncHandler = new InputSyncHandler(handler, index);
        setSyncHandler(this.syncHandler);
        tooltipAutoUpdate(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = this.syncHandler.getStack();
            if (stack.isEmpty()) return;
            tooltip.addFromItem(stack);
        });

        // for hovering with items in hand
        listenGuiAction((IGuiAction.MouseDrag) (m, t) -> {
            if (isHovering() && dragging && syncHandler.isValid()) {
                var player = syncHandler.getSyncManager().getCursorItem();
                if (!ItemHandlerHelper.canItemStacksStack(player, getStack()))
                    syncHandler.syncStack();
                return true;
            }
            return false;
        });

        // dragging has stopped
        listenGuiAction((IGuiAction.MouseReleased) mouseButton -> {
            dragging = false;
            return true;
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
        if (!this.syncHandler.isValid() || dragging)
            return Result.IGNORE;

        this.syncHandler.syncStack();
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        if (!dragging && timeSinceClick > 100) {
            dragging = true;
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack itemstack = this.syncHandler.getStack();
        boolean jeiIngredientBeingHovered = JEIUtil.hoveringOverIngredient(this);

        if (!itemstack.isEmpty()) {
            if (!jeiIngredientBeingHovered && !this.hasIngredients) {
                RenderUtil.renderRect(0, 0, 18, 18, 200, 0x80FF0000);
            }

            RenderUtil.renderItem(itemstack, 1, 1, 16, 16);
        }

        if (jeiIngredientBeingHovered) {
            RenderUtil.drawJEIGhostSlotOverlay(this);
        } else {
            RenderUtil.handleSlotOverlay(this, widgetTheme);
        }
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
        syncHandler.setStack(ingredient, true);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        ingredient = JEIUtil.getBookStackIfEnchantment(ingredient);
        return areAncestorsEnabled() && ingredient instanceof ItemStack ? (ItemStack) ingredient : null;
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
        this.syncHandler.setStack(stack, true);
    }

    protected static class InputSyncHandler extends SyncHandler {

        public static final int SLOT_CHANGED = 1;

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
            this.lastStoredItem = getStack().copy();
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == SLOT_CHANGED) {
                var stack = NetworkUtils.readItemStack(buf);
                setStack(stack, false);
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == SLOT_CHANGED) {
                var stack = NetworkUtils.readItemStack(buf);
                this.setStack(stack, false);
            }
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            ItemStack itemStack = getStack();
            if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
            if (init || !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack)) {
                this.listener.onChange(itemStack, false, false, init);
                this.lastStoredItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();
                syncToClient(SLOT_CHANGED, buffer -> NetworkUtils.writeItemStack(buffer, itemStack));
            }
        }

        public void syncStack() {
            final var cursorStack = GTUtility.copy(1, getSyncManager().getCursorItem());
            setStack(cursorStack, true);
        }

        public ItemStack getStack() {
            return this.handler.getStackInSlot(this.index);
        }

        /**
         * Sets the stack in this slot and calls the onChange listener.
         * 
         * @param stack stack to put into this slot
         */
        public void setStack(ItemStack stack, boolean sync) {
            var old = getStack();
            boolean onlyAmt = ItemHandlerHelper.canItemStacksStackRelaxed(stack, old);
            this.handler.setStackInSlot(this.index, stack);
            this.listener.onChange(stack, onlyAmt, getSyncManager().isClient(), false);
            if (sync) syncToServer(SLOT_CHANGED, buffer -> NetworkUtils.writeItemStack(buffer, getStack()));
        }
    }
}
