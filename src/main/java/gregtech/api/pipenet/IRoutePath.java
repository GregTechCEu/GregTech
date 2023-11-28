package gregtech.api.pipenet;

import gregtech.api.pipenet.tile.IPipeTile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRoutePath<T extends IPipeTile<?, ?>> {

    @NotNull
    T getTargetPipe();

    @NotNull
    default BlockPos getTargetPipePos() {
        return getTargetPipe().getPipePos();
    }

    @NotNull
    EnumFacing getTargetFacing();

    int getDistance();

    @Nullable
    default TileEntity getTargetTileEntity() {
        return getTargetPipe().getNeighbor(getTargetFacing());
    }

    @Nullable
    default <I> I getTargetCapability(Capability<I> capability) {
        TileEntity tile = getTargetTileEntity();
        return tile == null ? null : tile.getCapability(capability, getTargetFacing().getOpposite());
    }
}
