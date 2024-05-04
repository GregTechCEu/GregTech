package gregtech.common.mui.widget.workbench;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;

import com.cleanroommc.modularui.widgets.slot.IOnSlotChanged;

import gregtech.client.utils.RenderUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;

import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CraftingInputSlot extends Widget<CraftingOutputSlot> implements Interactable {
    private final InputSyncHandler syncHandler;

    public CraftingInputSlot(IItemHandlerModifiable handler, int index) {
        this.syncHandler = new InputSyncHandler(handler, index);
        setSyncHandler(this.syncHandler);
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            tooltip.excludeArea(getArea());
            if (!isSynced()) return;
            ItemStack stack = this.syncHandler.getStack();
            if (stack.isEmpty()) return;
            tooltip.addStringLines(getScreen().getScreenWrapper().getItemToolTip(stack));
        });
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
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        ItemStack itemstack = this.syncHandler.getStack();
        if (itemstack.isEmpty()) return;

        guiScreen.setZ(100f);
        guiScreen.getItemRenderer().zLevel = 100.0F;

        RenderUtil.renderItemInGUI(itemstack, 1, 1);

        guiScreen.getItemRenderer().zLevel = 0.0F;
        guiScreen.setZ(0f);
    }

    @Override
    public void drawForeground(GuiContext context) {
        Tooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), this.syncHandler.getStack());
        }
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
        public void init(String key, GuiSyncManager syncHandler) {
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
                this.handler.setStackInSlot(this.index, readStackSafe(buf));
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
            var s = getSyncManager().getCursorItem().copy();
            s.setCount(1);
            this.handler.setStackInSlot(this.index, s);
            syncToServer(1, buffer -> buffer.writeItemStack(s));
        }

        public ItemStack getStack() {
            return this.handler.getStackInSlot(this.index);
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
