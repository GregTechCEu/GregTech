package gregtech.api.graphnet.pipenet.physical.tile;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.EnumSet;

public class PipeCapabilityWrapper {

    private byte activeMask;
    public final Capability<?>[] capabilities;

    public PipeCapabilityWrapper(Capability<?>[] capabilities) {
        this.capabilities = capabilities;
    }

    public void setActive(EnumFacing facing) {
        this.activeMask |= 1 << facing.ordinal();
    }

    public void setIdle(EnumFacing facing) {
        this.activeMask &= ~(1 << facing.ordinal());
    }

    public boolean isActive(EnumFacing facing) {
        return (this.activeMask & 1 << facing.ordinal()) > 0;
    }
}
