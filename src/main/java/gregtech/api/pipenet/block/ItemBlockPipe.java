package gregtech.api.pipenet.block;

import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
        boolean superVal = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (superVal && ConfigHolder.machines.gt6StylePipesCables && !world.isRemote) {
            IPipeTile selfTile = (IPipeTile) world.getTileEntity(pos);
            if (selfTile != null && selfTile.getPipeBlock().canConnect(selfTile, side.getOpposite())) {
                selfTile.setConnectionBlocked(AttachmentType.PIPE, side.getOpposite(), false, false);
            }
        }
        return superVal;
    }
}
