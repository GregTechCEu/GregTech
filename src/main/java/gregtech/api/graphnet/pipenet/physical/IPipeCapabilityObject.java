package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.Nullable;

public interface IPipeCapabilityObject {
    void setTile(PipeTileEntity tile);

    Capability<?>[] getCapabilities();

    <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing);
}
