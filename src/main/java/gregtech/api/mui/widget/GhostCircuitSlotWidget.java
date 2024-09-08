package gregtech.api.mui.widget;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.recipes.ingredients.old.IntCircuitIngredient;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GhostCircuitSlotWidget extends ItemSlot {

    private static final int SYNC_CIRCUIT_INDEX = 10;

    public GhostCircuitSlotWidget() {
        tooltipBuilder(this::getCircuitSlotTooltip);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isSelectorPanelOpen()) {
            if (mouseButton == 0 && TooltipHelper.isShiftDown()) {
                createSelectorPanel();
            } else {
                MouseData mouseData = MouseData.create(mouseButton);
                getSyncHandler().syncToServer(2, mouseData::writeToPacket);
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (isSelectorPanelOpen()) return true;
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        getSyncHandler().syncToServer(3, mouseData::writeToPacket);
        return false;
    }

    @Override
    public ItemSlot slot(ModularSlot slot) {
        ItemSlotSH sh = new GhostCircuitSyncHandler(slot);
        isValidSyncHandler(sh);
        setSyncHandler(sh);
        return this;
    }

    @Override
    protected List<String> getItemTooltip(ItemStack stack) {
        // we don't want the item tooltip
        return Collections.emptyList();
    }

    protected void getCircuitSlotTooltip(@NotNull Tooltip tooltip) {
        String configString;
        int value = getSyncHandler().getGhostCircuitHandler().getCircuitValue();
        if (value == GhostCircuitItemStackHandler.NO_CONFIG) {
            configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value").getFormattedText();
        } else {
            configString = String.valueOf(value);
        }

        tooltip.addLine(IKey.lang("gregtech.gui.configurator_slot.tooltip", configString));
    }

    @Override
    public @NotNull GhostCircuitSyncHandler getSyncHandler() {
        return (GhostCircuitSyncHandler) super.getSyncHandler();
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {}

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }

    private boolean isSelectorPanelOpen() {
        return getPanel().getScreen().isPanelOpen("circuit_selector");
    }

    private void createSelectorPanel() {
        ItemDrawable circuitPreview = new ItemDrawable(getSyncHandler().getSlot().getStack());

        List<List<IWidget>> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            options.add(new ArrayList<>());
            for (int j = 0; j < 9; j++) {
                int index = i * 9 + j;
                if (index > 32) break;
                options.get(i).add(new ButtonWidget<>()
                        .size(18)
                        .background(GTGuiTextures.SLOT, new ItemDrawable(
                                IntCircuitIngredient.getIntegratedCircuit(index)).asIcon())
                        .disableHoverBackground()
                        .onMousePressed(mouseButton -> {
                            getSyncHandler().syncToServer(SYNC_CIRCUIT_INDEX, buf -> buf.writeShort(index));
                            circuitPreview.setItem(IntCircuitIngredient.getIntegratedCircuit(index));
                            return true;
                        }));
            }
        }

        IPanelHandler.simple(getPanel(), (mainPanel, player) -> GTGuis.createPopupPanel("circuit_selector", 176, 120)
                .child(IKey.lang("metaitem.circuit.integrated.gui").asWidget().pos(5, 5))
                .child(circuitPreview.asIcon().size(16).asWidget()
                        .size(18)
                        .top(19).alignX(0.5f)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY))
                .child(new Grid()
                        .left(7).right(7).top(41).height(4 * 18)
                        .matrix(options)
                        .minColWidth(18).minRowHeight(18)
                        .minElementMargin(0, 0)))
                .openPanel();
    }

    private static class GhostCircuitSyncHandler extends ItemSlotSH {

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
