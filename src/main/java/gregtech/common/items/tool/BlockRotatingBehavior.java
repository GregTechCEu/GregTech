package gregtech.common.items.tool;

import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class BlockRotatingBehavior implements IToolBehavior {
    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        TileEntity te = world.getTileEntity(pos);
        //MTEs have special handling on rotation
        if (te instanceof MetaTileEntityHolder) {
            return EnumActionResult.PASS;
        }
        final Block b = world.getBlockState(pos).getBlock();
        //leave rail rotation to Crowbar only
        if (b instanceof BlockRailBase) {
            return EnumActionResult.FAIL;
        }
        if (!player.isSneaking() && world.canMineBlockBody(player, pos)) {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
                return !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
            }

            if (b.rotateBlock(world, pos, side)) {
                player.swingArm(hand);
                return !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
            }
        }
        return EnumActionResult.PASS;
    }
}
