package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class WrenchBehaviour implements IItemBehaviour {

    private final int cost;

    public WrenchBehaviour(int cost) {
        this.cost = cost;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote && !world.isAirBlock(pos)) {
            ItemStack stack = player.getHeldItem(hand);

            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof MetaTileEntityHolder)
                //machines handle wrench click manually
                return EnumActionResult.PASS;
            if (world.getBlockState(pos).getBlock().rotateBlock(world, pos, side)) {
                GTUtility.doDamageItem(stack, this.cost, false);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(LocalizationUtils.format("behaviour.wrench"));
    }
}
