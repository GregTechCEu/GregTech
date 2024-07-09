package gregtech.api.pipenet;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface IPipeNetHandler {

    WorldPipeNetBase<?, ?, ?> getNet();

    EnumFacing getFacing();

    default Cover getCoverOnNeighbour(BlockPos pos, EnumFacing facing) {
        NetNode<?, ?, ?> node = getNet().getNode(pos);
        if (node != null) {
            TileEntity tile = node.getConnnected(facing);
            if (tile != null) {
                CoverHolder coverHolder = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                        facing.getOpposite());
                if (coverHolder == null) return null;
                return coverHolder.getCoverAtSide(facing.getOpposite());
            }
        }
        return null;
    }
}
