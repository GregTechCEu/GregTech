package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;

public class PipeCapabilityWrapper {

    private byte activeMask;
    private final PipeTileEntity owner;
    private final WorldPipeNetNode node;
    public final Capability<?>[] capabilities;

    public PipeCapabilityWrapper(PipeTileEntity owner, WorldPipeNetNode node) {
        this.owner = owner;
        this.node = node;
        this.capabilities = node.getNet().getTargetCapabilities();
    }

    public void setActive(@NotNull EnumFacing facing) {
        if (!isActive(facing)) {
            this.activeMask |= 1 << facing.ordinal();
            this.node.setActive(this.activeMask > 0);
            this.owner.notifyBlockUpdate();
        }
    }

    public void setIdle(@NotNull EnumFacing facing) {
        if (isActive(facing)) {
            this.activeMask &= ~(1 << facing.ordinal());
            this.node.setActive(this.activeMask > 0);
            this.owner.notifyBlockUpdate();
        }
    }

    public boolean isActive(@NotNull EnumFacing facing) {
        return (this.activeMask & 1 << facing.ordinal()) > 0;
    }
}
