package gregtech.api.capability.impl;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public abstract class CapabilityCompatProvider implements ICapabilityProvider {

    private final ICapabilityProvider upvalue;

    public CapabilityCompatProvider(ICapabilityProvider upvalue) {
        this.upvalue = upvalue;
    }

    protected <T> boolean hasUpvalueCapability(Capability<T> capability, EnumFacing facing) {
        return upvalue.hasCapability(capability, facing);
    }

    @Nullable
    protected <T> T getUpvalueCapability(Capability<T> capability, EnumFacing facing) {
        return upvalue.getCapability(capability, facing);
    }
}
