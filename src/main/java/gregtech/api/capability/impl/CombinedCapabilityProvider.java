package gregtech.api.capability.impl;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombinedCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    private final ICapabilityProvider[] providers;

    public CombinedCapabilityProvider(ICapabilityProvider... providers) {
        this.providers = providers;
    }

    public CombinedCapabilityProvider(List<ICapabilityProvider> providers) {
        this.providers = providers.toArray(new ICapabilityProvider[0]);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider provider : providers) {
            if (provider.hasCapability(capability, facing)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider provider : providers) {
            T cap = provider.getCapability(capability, facing);
            if (cap != null) {
                return cap;
            }
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        int index = 0;
        for (ICapabilityProvider provider : providers) {
            if (provider instanceof INBTSerializable<?>serializable) {
                tag.setTag(String.valueOf(index++), serializable.serializeNBT());
            }
        }

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        int index = 0;
        for (ICapabilityProvider provider : providers) {
            // Me when no runtime generic type information :joy:
            // This will probably not explode since it'll deserialize in the same order it was serialized so the types
            // should match up.
            // noinspection rawtypes
            if (provider instanceof INBTSerializable serializable) {
                // noinspection unchecked
                serializable.deserializeNBT(tag.getTag(String.valueOf(index++)));
            }
        }
    }
}
