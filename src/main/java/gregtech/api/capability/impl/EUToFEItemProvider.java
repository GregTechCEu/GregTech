package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public class EUToFEItemProvider extends CapabilityCompatProvider {

    public EUToFEItemProvider(ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return ConfigHolder.compat.energy.nativeEUToFE &&
                capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM &&
                hasUpvalueCapability(CapabilityEnergy.ENERGY, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (!ConfigHolder.compat.energy.nativeEUToFE || capability != GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM)
            return null;

        IEnergyStorage energyStorage = getUpvalueCapability(CapabilityEnergy.ENERGY, facing);
        return energyStorage != null ?
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM.cast(new GTEnergyItemWrapper(energyStorage)) :
                null;
    }
}
