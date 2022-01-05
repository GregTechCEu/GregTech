package gregtech.api.items.metaitem;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IMusicDisc;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MusicDiscStats implements IMusicDisc, IItemBehaviour {

    public static final int SOUND_TYPE = 1050;

    private final SoundEvent sound;

    public MusicDiscStats(SoundEvent sound) {
        this.sound = sound;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public SoundEvent getSound() {
        return sound;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = world.getBlockState(pos);
        ItemStack itemStack = player.getHeldItem(hand);
        if (iblockstate.getBlock() == Blocks.JUKEBOX && !(Boolean)iblockstate.getValue(BlockJukebox.HAS_RECORD)) {
            if (!world.isRemote) {
                ((BlockJukebox)Blocks.JUKEBOX).insertRecord(world, pos, iblockstate, itemStack);
                world.playEvent(SOUND_TYPE, pos, itemStack.getItemDamage());
                itemStack.shrink(1);
                player.addStat(StatList.RECORD_PLAYED);
            }

            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }
    }
}
