package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoverPlaceBehavior implements IItemBehaviour {

    public final CoverDefinition coverDefinition;

    public CoverPlaceBehavior(CoverDefinition coverDefinition) {
        this.coverDefinition = coverDefinition;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        ICoverable coverable = tileEntity == null ? null : tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
        if (coverable == null) {
            return EnumActionResult.PASS;
        }
        EnumFacing coverSide = ICoverable.rayTraceCoverableSide(coverable, player);
        if (coverable.getCoverAtSide(coverSide) != null || !coverable.canPlaceCoverOnSide(coverSide)) {
            return EnumActionResult.PASS;
        }
        if (!world.isRemote) {
            ItemStack itemStack = player.getHeldItem(hand);
            boolean result = coverable.placeCoverOnSide(coverSide, itemStack, coverDefinition, player);
            if (result && !player.capabilities.isCreativeMode) {
                itemStack.shrink(1);
            }
            return result ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
        return EnumActionResult.SUCCESS;
    }
}
