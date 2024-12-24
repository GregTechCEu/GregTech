package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.NotNull;

public class PipeCapabilityWrapper implements ICapabilityProvider {

    protected byte activeMask;
    protected final PipeTileEntity owner;
    protected final WorldPipeNode node;

    protected final Object2ObjectMap<Capability<?>, IPipeCapabilityObject> capabilities;

    protected final int inactiveKey;
    protected final int activeKey;

    public PipeCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node,
                                 Object2ObjectMap<Capability<?>, IPipeCapabilityObject> capabilities,
                                 int inactiveKey, int activeKey) {
        this.owner = owner;
        this.node = node;
        this.inactiveKey = inactiveKey;
        this.activeKey = activeKey;
        this.capabilities = capabilities;
        for (IPipeCapabilityObject o : capabilities.values()) {
            o.init(owner, this);
        }
    }

    public void invalidate() {}

    public void setActive(@NotNull EnumFacing facing) {
        if (!isActive(facing)) {
            setActiveInternal(facing);
        }
    }

    protected void setActiveInternal(@NotNull EnumFacing facing) {
        this.activeMask |= 1 << facing.ordinal();
        this.node.setSortingKey(this.activeMask > 0 ? activeKey : inactiveKey);
        this.owner.notifyBlockUpdate();
    }

    public void setIdle(@NotNull EnumFacing facing) {
        if (isActive(facing)) {
            setIdleInternal(facing);
        }
    }

    protected void setIdleInternal(@NotNull EnumFacing facing) {
        this.activeMask &= ~(1 << facing.ordinal());
        this.node.setSortingKey(this.activeMask > 0 ? activeKey : inactiveKey);
        this.owner.notifyBlockUpdate();
    }

    public boolean isActive(@NotNull EnumFacing facing) {
        return (this.activeMask & 1 << facing.ordinal()) > 0;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
        IPipeCapabilityObject obj = capabilities.get(capability);
        if (obj == null) return false;
        return obj.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
        IPipeCapabilityObject obj = capabilities.get(capability);
        if (obj == null) return null;
        return obj.getCapability(capability, facing);
    }
}
