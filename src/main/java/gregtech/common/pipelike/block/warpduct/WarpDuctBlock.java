package gregtech.common.pipelike.block.warpduct;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.handlers.WarpDuctNetHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class WarpDuctBlock extends PipeBlock {

    public WarpDuctBlock(WarpDuctStructure structure) {
        super(structure);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
    }

    @Override
    public WarpDuctStructure getStructure() {
        return (WarpDuctStructure) super.getStructure();
    }

    @Override
    public Class<? extends WarpDuctTileEntity> getTileClass(@NotNull World world, @NotNull IBlockState state) {
        return WarpDuctTileEntity.class;
    }

    @Override
    public @Nullable WarpDuctTileEntity getTileEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (GTUtility.arePosEqual(lastTilePos.get(), pos)) {
            PipeTileEntity tile = lastTile.get().get();
            if (tile != null && !tile.isInvalid()) return (WarpDuctTileEntity) tile;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof WarpDuctTileEntity pipe) {
            lastTilePos.set(pos.toImmutable());
            lastTile.set(new WeakReference<>(pipe));
            return pipe;
        } else return null;
    }

    @Override
    @NotNull
    public IPipeNetNodeHandler getHandler(PipeTileEntity tileContext) {
        return WarpDuctNetHandler.INSTANCE;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(@NotNull ItemStack stack) {
        return WarpDuctNetHandler.INSTANCE;
    }
}
