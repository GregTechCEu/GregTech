package gtqt.common.metatileentities.multi.multiblockpart;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.widget.GhostCircuitSlotWidget;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.util.text.TextFormatting.GREEN;

public class MetaTileEntitySuperItemBus extends MetaTileEntityItemBus {

    public MetaTileEntitySuperItemBus(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySuperItemBus(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        panelSyncManager.registerSlotGroup("item_inv", rowSize);

        int backgroundWidth = Math.max(
                9 * 18 + 18 + 14 + 5,   // Player Inv width
                rowSize * 18 + 14); // Bus Inv width
        int backgroundHeight = 18 + 18 * rowSize + 94;

        BooleanSyncValue workingStateValue = new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled);
        BooleanSyncValue collapseStateValue = new BooleanSyncValue(this::isAutoCollapse, this::setAutoCollapse);

        IItemHandlerModifiable handler = isExportHatch ? exportItems : importItems;
        boolean hasGhostCircuit = hasGhostCircuitInventory() && this.circuitInventory != null;

        return GTGuis.createPanel(this, backgroundWidth, backgroundHeight)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .alignX(0.5f)
                        .mapTo(rowSize, rowSize * rowSize, index -> new ItemSlot()
                                .slot(new ModularSlot(handler, index) {

                                    @Override
                                    public int getSlotStackLimit() {
                                        return Integer.MAX_VALUE;
                                    }
                                }
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("item_inv")
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                            if (onlyAmountChanged &&
                                                    handler instanceof GTItemStackHandler gtHandler) {
                                                gtHandler.onContentsChanged(index);
                                            }
                                        })
                                        .accessibility(!isExportHatch, true))))
                .child(Flow.column()
                        .pos(backgroundWidth - 7 - 18, backgroundHeight - 18 * 4 - 7 - 5)
                        .width(18).height(18 * 4 + 5)
                        .child(GTGuiTextures.getLogo(getUITheme()).asWidget().size(17).top(18 * 3 + 5))
                        .child(new ToggleButton()
                                .top(18 * 2)
                                .value(workingStateValue)
                                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                                .tooltipAutoUpdate(true)
                                .tooltipBuilder(t -> t.addLine(isExportHatch ?
                                        (workingStateValue.getBoolValue() ?
                                                IKey.lang("gregtech.gui.item_auto_output.tooltip.enabled") :
                                                IKey.lang("gregtech.gui.item_auto_output.tooltip.disabled")) :
                                        (workingStateValue.getBoolValue() ?
                                                IKey.lang("gregtech.gui.item_auto_input.tooltip.enabled") :
                                                IKey.lang("gregtech.gui.item_auto_input.tooltip.disabled")))))
                        .child(new ToggleButton()
                                .top(18)
                                .value(collapseStateValue)
                                .overlay(GTGuiTextures.BUTTON_AUTO_COLLAPSE)
                                .tooltipAutoUpdate(true)
                                .tooltipBuilder(t -> t.addLine(collapseStateValue.getBoolValue() ?
                                        IKey.lang("gregtech.gui.item_auto_collapse.tooltip.enabled") :
                                        IKey.lang("gregtech.gui.item_auto_collapse.tooltip.disabled"))))
                        .childIf(hasGhostCircuit, new GhostCircuitSlotWidget()
                                .slot(SyncHandlers.itemSlot(circuitInventory, 0))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY))
                        .childIf(!hasGhostCircuit, new Widget<>()
                                .background(GTGuiTextures.SLOT, GTGuiTextures.BUTTON_X)
                                .tooltip(t -> t.addLine(
                                        IKey.lang("gregtech.gui.configurator_slot.unavailable.tooltip")))));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(GREEN + I18n.format("gregtech.machine.super_item_bus.tooltip"));
    }
}
