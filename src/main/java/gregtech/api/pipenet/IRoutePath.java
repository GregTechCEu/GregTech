package gregtech.api.pipenet;

import gregtech.api.pipenet.tile.IPipeTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public interface IRoutePath<T extends IPipeTile<?, ?>> {

    T getTargetPipe();

    default BlockPos getTargetPipePos() {
        return getTargetPipe().getPipePos();
    }

    EnumFacing getTargetFacing();

    int getDistance();

    default TileEntity getTargetTileEntity() {
        return getTargetPipe().getNeighbor(getTargetFacing());
    }

    default <I> I getTargetCapability(Capability<I> capability) {
        TileEntity tile = getTargetTileEntity();
        return tile == null ? null : tile.getCapability(capability, getTargetFacing().getOpposite());
    }
}
