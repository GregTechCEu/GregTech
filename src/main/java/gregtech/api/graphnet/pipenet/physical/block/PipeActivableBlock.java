package gregtech.api.graphnet.pipenet.physical.block;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.tile.PipeActivableTileEntity;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.PipeRenderProperties;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public abstract class PipeActivableBlock extends PipeBlock {

    public PipeActivableBlock(IPipeStructure structure) {
        super(structure);
    }

    @Override
    protected @NotNull BlockStateContainer.Builder constructState(BlockStateContainer.@NotNull Builder builder) {
        return super.constructState(builder).add(PipeRenderProperties.ACTIVE_PROPERTY);
    }

    @Override
    public Class<? extends PipeActivableTileEntity> getTileClass(@NotNull World world, @NotNull IBlockState state) {
        return PipeActivableTileEntity.class;
    }

    @Override
    public @Nullable PipeActivableTileEntity getTileEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (GTUtility.arePosEqual(lastTilePos.get(), pos)) {
            PipeTileEntity tile = lastTile.get().get();
            if (tile != null && !tile.isInvalid()) return (PipeActivableTileEntity) tile;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeActivableTileEntity pipe) {
            lastTilePos.set(pos.toImmutable());
            lastTile.set(new WeakReference<>(pipe));
            return pipe;
        } else return null;
    }
}
