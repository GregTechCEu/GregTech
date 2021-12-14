package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public class EUToFEItemProvider implements ICapabilityProvider {

    private final ItemStack itemStack;

    public EUToFEItemProvider(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return ConfigHolder.U.energyOptions.nativeEUToFE &&
                capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM &&
                itemStack.hasCapability(CapabilityEnergy.ENERGY, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (!ConfigHolder.U.energyOptions.nativeEUToFE || capability != GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM)
            return null;
        IEnergyStorage energyStorage = itemStack.getCapability(CapabilityEnergy.ENERGY, facing);
        return energyStorage != null ?
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM.cast(new GTEnergyItemWrapper(energyStorage)) :
                null;
    }
}
