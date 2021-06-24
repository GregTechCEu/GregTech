package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.ICircuitConfigurationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ConfigCircuitItem implements ICircuitConfigurationItem, ICapabilityProvider {

    protected ItemStack itemStack;
    protected final int maxConfiguraiton;

    protected List<BiConsumer<ItemStack, Integer>> listeners = new ArrayList<>();

    public ConfigCircuitItem(ItemStack itemStack, int maxConfiguration) {
        this.itemStack = itemStack;
        this.maxConfiguraiton = maxConfiguration;
    }

    protected void setConfiguration(int configuration) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        itemStack.getTagCompound().setInteger("Configuration", configuration);
        listeners.forEach(l -> l.accept(itemStack, configuration));
    }

    @Override
    public int getConfiguration() {
        NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
        if (nbtTagCompound == null)
            return 0;
        return Math.min(nbtTagCompound.getInteger("Configuration"), this.maxConfiguraiton);
    }

    @Override
    public int changeConfiguration(int amount, boolean simulate) {
        if (itemStack.getCount() != 1) {
            return 0;
        }
        if (amount > 0) {
            int potentialConfiguration = getConfiguration() + amount;

            // check out of bounds cases
            if (potentialConfiguration < 0)
                return 0;

            if (potentialConfiguration > maxConfiguraiton)
                return 0;

            // change config
            if (!simulate)
                setConfiguration(potentialConfiguration);

            return potentialConfiguration;
        }
        return 0;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing) {
        return capability == GregtechCapabilities.CAPABILITY_CIRCUIT_CONFIGURATION;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing) {
        return capability == GregtechCapabilities.CAPABILITY_CIRCUIT_CONFIGURATION ? (T) this : null;
    }
}
