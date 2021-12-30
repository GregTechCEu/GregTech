package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

public class EnergyContainerBatteryCharger extends EnergyContainerHandler {

    private final int tier;

    public EnergyContainerBatteryCharger(MetaTileEntity metaTileEntity, int tier, int inventorySize) {
        super(metaTileEntity, GTValues.V[tier] * 128L, GTValues.V[tier], inventorySize * 4L, 0L, 0L);
        this. tier = tier;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (amperage <= 0 || voltage <= 0)
            return 0;

        List<IElectricItem> batteries = getNonFullBatteries();
        long maxAmps = batteries.size() * 4L - amps;
        long usedAmps = Math.min(maxAmps, amperage);
        if (maxAmps <= 0)
            return 0;

        if (side == null || inputsEnergy(side)) {
            if (voltage > getInputVoltage()) {
                metaTileEntity.doExplosion(GTUtility.getExplosionPower(voltage));
            }

            //Prioritizes as many packets as available from the buffer
            long internalAmps = Math.min(maxAmps, Math.max(0, getInternalStorage() / voltage));

            usedAmps = Math.min(usedAmps, maxAmps - internalAmps);
            amps += usedAmps;
            energyInputPerSec += usedAmps * voltage;

            long energy = (usedAmps + internalAmps) * voltage;
            long distributed = energy / batteries.size();

            for (IElectricItem electricItem : batteries) {
                energy -= electricItem.charge(Math.min(distributed, GTValues.V[electricItem.getTier()] * 4L), getTier(), true, false);
            }

            //Remove energy used and then transfer overflow energy into the internal buffer
            setEnergyStored(getInternalStorage() - internalAmps * voltage + energy);
            return usedAmps;
        }
        return 0;
    }

    @Override
    public void update() {
        amps = 0;
        if (metaTileEntity.getWorld().isRemote) {
            return;
        }
        if (metaTileEntity.getOffsetTimer() % 20 == 0) {
            lastEnergyInputPerSec = energyInputPerSec;
            energyInputPerSec = 0;
        }
    }

    private long getInternalStorage() {
        return energyStored;
    }

    private List<IElectricItem> getNonFullBatteries() {
        IItemHandlerModifiable inventory = getInventory();
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            if (electricItem.getCharge() < electricItem.getMaxCharge()) {
                batteries.add(electricItem);
            }
        }
        return batteries;
    }

    @Override
    public long getEnergyCapacity() {
        long energyCapacity = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            energyCapacity += electricItem.getMaxCharge();
        }
        return energyCapacity;
    }

    @Override
    public long getEnergyStored() {
        long energyStored = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            energyStored += electricItem.getCharge();
        }
        return energyStored;
    }

    @Override
    public void setEnergyStored(long energyStored) {
        this.energyStored = energyStored;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            notifyEnergyListener(false);
        }
    }

    public IElectricItem getBatteryContainer(ItemStack itemStack) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && getTier() >= electricItem.getTier())
            return electricItem;
        return null;
    }

    public void notifyEnergyListener(boolean isInitialChange) {
        if (metaTileEntity instanceof IEnergyChangeListener) {
            ((IEnergyChangeListener) metaTileEntity).onEnergyChanged(this, isInitialChange);
        }
    }

    protected IItemHandlerModifiable getInventory() {
        return metaTileEntity.getImportItems();
    }

    protected int getTier() {
        return tier;
    }
}
