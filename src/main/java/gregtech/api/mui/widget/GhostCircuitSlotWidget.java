package gregtech.api.mui.widget;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class GhostCircuitSlotWidget extends ItemSlot {

    private static final int SYNC_CIRCUIT_INDEX = 10;
    @Nullable
    private IPanelHandler selectorPanel;
    private GhostCircuitSyncHandler syncHandler;

    public GhostCircuitSlotWidget() {
        super();
        tooltipBuilder(this::getCircuitSlotTooltip);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isSelectorPanelOpen()) {
            if (mouseButton == 0 && TooltipHelper.isShiftDown()) {
                this.getSelectorPanel().openPanel();
            } else {
                MouseData mouseData = MouseData.create(mouseButton);
                getSyncHandler().syncToServer(2, mouseData::writeToPacket);
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (isSelectorPanelOpen()) return true;
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        getSyncHandler().syncToServer(3, mouseData::writeToPacket);
        return true;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, GhostCircuitSyncHandler.class);
        if (this.syncHandler == null) return false;
        return super.isValidSyncHandler(syncHandler);
    }

    @Override
    public ItemSlot slot(ModularSlot slot) {
        this.syncHandler = new GhostCircuitSyncHandler(slot);
        isValidSyncHandler(this.syncHandler);
        setSyncHandler(this.syncHandler);
        return this;
    }

    protected void getCircuitSlotTooltip(@NotNull RichTooltip tooltip) {
        String configString;
        int value = this.syncHandler.getCircuitValue();
        if (value == GhostCircuitItemStackHandler.NO_CONFIG) {
            configString = IKey.lang("gregtech.gui.configurator_slot.no_value").get();
        } else {
            configString = String.valueOf(value);
        }
        tooltip.clearText();
        tooltip.addLine(IKey.lang("gregtech.gui.configurator_slot.tooltip", configString));
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {}

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }

    private boolean isSelectorPanelOpen() {
        return this.getSelectorPanel().isPanelOpen();
    }

    @NotNull
    private IPanelHandler getSelectorPanel() {
        if (this.selectorPanel == null) {
            this.selectorPanel = IPanelHandler.simple(getPanel(), (mainPanel, player) -> {
                ItemDrawable circuitPreview = new ItemDrawable(this.syncHandler.getCircuitStack());

                return GTGuis.createPopupPanel("circuit_selector", 176, 120)
                        .child(IKey.lang("metaitem.circuit.integrated.gui").asWidget().pos(5, 5))
                        .child(new Widget<>()
                                .size(18)
                                .top(19).alignX(0.5f)
                                .overlay(circuitPreview.asIcon().margin(1))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY))
                        .child(new Grid()
                                .left(7).right(7).top(41).height(4 * 18)
                                .mapTo(9, 33, value -> new ButtonWidget<>()
                                        .size(18)
                                        .background(GTGuiTextures.SLOT, new ItemDrawable(
                                                IntCircuitIngredient.getIntegratedCircuit(value)).asIcon())
                                        .disableHoverBackground()
                                        .onMousePressed(mouseButton -> {
                                            getSyncHandler().syncToServer(SYNC_CIRCUIT_INDEX,
                                                    buf -> buf.writeShort(value));
                                            circuitPreview.setItem(IntCircuitIngredient.getIntegratedCircuit(value));
                                            if (Interactable.hasShiftDown()) this.selectorPanel.closePanel();
                                            return true;
                                        }))
                                .minColWidth(18).minRowHeight(18)
                                .minElementMargin(0, 0));
            }, true);
        }
        return this.selectorPanel;
    }

    private static class GhostCircuitSyncHandler extends PhantomItemSlotSH {

        // TODO: should we be using this as it's marked internal? Or is it fine.
        @SuppressWarnings("UnstableApiUsage")
        public GhostCircuitSyncHandler(ModularSlot slot) {
            super(slot);
        }

        @Override
        protected void phantomClick(MouseData mouseData) {
            if (mouseData.mouseButton == 0) {
                // increment on left-click
                setCircuitValue(getNextCircuitValue(1));
            } else if (mouseData.mouseButton == 1 && mouseData.shift) {
                // clear on shift-right-click
                setCircuitValue(GhostCircuitItemStackHandler.NO_CONFIG);
            } else if (mouseData.mouseButton == 1) {
                // decrement on right-click
                setCircuitValue(getNextCircuitValue(-1));
            }
        }

        @Override
        protected void phantomScroll(MouseData mouseData) {
            setCircuitValue(getNextCircuitValue(mouseData.mouseButton));
        }

        private void setCircuitValue(int value) {
            GhostCircuitItemStackHandler handler = getGhostCircuitHandler();
            if (handler.getCircuitValue() != value) {
                handler.setCircuitValue(value);
                syncToClient(1, buf -> {
                    buf.writeBoolean(false);
                    buf.writeItemStack(handler.getStackInSlot(0));
                    buf.writeBoolean(false);
                });
            }
        }

        public int getCircuitValue() {
            return getGhostCircuitHandler().getCircuitValue();
        }

        public ItemStack getCircuitStack() {
            return getSlot().getStack();
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {
            if (id == SYNC_CIRCUIT_INDEX) {
                setCircuitValue(buf.readShort());
            } else {
                super.readOnServer(id, buf);
            }
        }

        private int getNextCircuitValue(int delta) {
            GhostCircuitItemStackHandler handler = getGhostCircuitHandler();

            // if no circuit, skip 0 and return 32 if decrementing,
            // or, skip 0 and return 1 when incrementing
            if (!handler.hasCircuitValue()) {
                return delta == 1 ? 1 : IntCircuitIngredient.CIRCUIT_MAX;
                // if at max, loop around to no circuit
            } else if (handler.getCircuitValue() + delta > IntCircuitIngredient.CIRCUIT_MAX) {
                return GhostCircuitItemStackHandler.NO_CONFIG;
                // if at 1, skip 0 and return to no circuit
            } else if (handler.getCircuitValue() + delta < 1) {
                return GhostCircuitItemStackHandler.NO_CONFIG;
            }

            // normal case: change by "delta" which is either 1 or -1
            return handler.getCircuitValue() + delta;
        }

        public GhostCircuitItemStackHandler getGhostCircuitHandler() {
            IItemHandler handler = getSlot().getItemHandler();
            if (!(handler instanceof GhostCircuitItemStackHandler ghostHandler)) {
                throw new IllegalStateException(
                        "GhostCircuitSyncHandler has IItemHandler that is not GhostCircuitItemStackHandler");
            }
            return ghostHandler;
        }
    }
}
