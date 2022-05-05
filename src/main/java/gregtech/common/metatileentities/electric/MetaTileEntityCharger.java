package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.EnergyContainerBatteryCharger;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if ((electricItem != null && getTier() >= electricItem.getTier()) ||
                        (ConfigHolder.compat.energy.nativeEUToFE && stack.hasCapability(CapabilityEnergy.ENERGY, null))) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(0);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = importItems;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(importItems, index, 89 - rowSize * 9 + x * 18, 18 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
    }
}
