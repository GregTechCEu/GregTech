package gregtech.api.pipenet.block;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.ConfigHolder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class ItemBlockPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends ItemBlock {

    protected final BlockPipe<PipeType, NodeDataType, ?> blockPipe;

    public ItemBlockPipe(BlockPipe<PipeType, NodeDataType, ?> block) {
        super(block);
        this.blockPipe = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean placeBlockAt(@NotNull ItemStack stack, @NotNull EntityPlayer player, @NotNull World world,
                                @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
                                @NotNull IBlockState newState) {
        boolean superVal = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (superVal && !world.isRemote && world.getTileEntity(pos) instanceof IPipeTile pipeTile) {
            if (pipeTile.getPipeBlock().canConnect(pipeTile, side.getOpposite())) {
                pipeTile.setConnection(side.getOpposite(), true, false);
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity te = pipeTile.getNeighbor(facing);
                if (te instanceof IPipeTile otherPipe) {
                    if (otherPipe.isConnected(facing.getOpposite())) {
                        if (otherPipe.getPipeBlock().canPipesConnect(otherPipe, facing.getOpposite(), pipeTile)) {
                            pipeTile.setConnection(facing, true, true);
                        } else {
                            otherPipe.setConnection(facing.getOpposite(), false, true);
                        }
                    }
                } else if (!ConfigHolder.machines.gt6StylePipesCables &&
                        pipeTile.getPipeBlock().canPipeConnectToBlock(pipeTile, facing, te)) {
                            pipeTile.setConnection(facing, true, false);
                        }
            }
        }
        return superVal;
    }
}
