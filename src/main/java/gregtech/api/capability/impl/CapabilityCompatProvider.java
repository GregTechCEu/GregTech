package gregtech.api.capability.impl;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public abstract class CapabilityCompatProvider implements ICapabilityProvider {

    private final ICapabilityProvider upvalue;

    private volatile boolean hasCapabilityLocked;
    private volatile boolean getCapabilityLocked;

    public CapabilityCompatProvider(ICapabilityProvider upvalue) {
        this.upvalue = upvalue;
    }

    protected <T> boolean hasUpvalueCapability(Capability<T> capability, EnumFacing facing) {
        if (hasCapabilityLocked) return false;
        try {
            hasCapabilityLocked = true;
            return upvalue.hasCapability(capability, facing);
        } finally {
            hasCapabilityLocked = false;
        }
    }

    @Nullable
    protected <T> T getUpvalueCapability(Capability<T> capability, EnumFacing facing) {
        if (getCapabilityLocked) return null;
        try {
            getCapabilityLocked = true;
            return upvalue.getCapability(capability, facing);
        } finally {
            getCapabilityLocked = false;
        }
    }
}
