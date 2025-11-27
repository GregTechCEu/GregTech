package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerBatteryCharger;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.mui.drawable.BatteryIndicatorDrawable;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCharger extends TieredMetaTileEntity {

    private final int inventorySize;

    public MetaTileEntityCharger(ResourceLocation metaTileEntityId, int tier, int inventorySize) {
        super(metaTileEntityId, tier);
        this.inventorySize = inventorySize;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCharger(metaTileEntityId, getTier(), inventorySize);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        this.energyContainer = new EnergyContainerBatteryCharger(this, getTier(), inventorySize);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(inventorySize) {

            @Override
            protected void onContentsChanged(int slot) {
                MetaTileEntityCharger.this.markDirty();
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return GTUtility.isItemChargeable(stack, getTier(), true) ? super.insertItem(slot, stack, simulate) :
                        stack;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = importItems;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager, UISettings settings) {
        int rowSize = (int) Math.sqrt(inventorySize);
        panelSyncManager.registerSlotGroup("slots", rowSize);

        return GTGuis.createPanel(this, 176, 18 + 18 * rowSize + 94)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                .child(new Grid()
                        .top(18)
                        .height(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .alignX(0.5f)
                        .mapTo(rowSize, rowSize * rowSize, index -> new ItemSlot()
                                .slot(SyncHandlers.itemSlot(importItems, index)
                                        .slotGroup("slots"))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY,
                                        new BatteryIndicatorDrawable(
                                                () -> GTUtility.itemChargeLevel(importItems.getStackInSlot(index)),
                                                0.85f))))
                .child(SlotGroupWidget.playerInventory(false)
                        .bottom(7)
                        .left(7));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
