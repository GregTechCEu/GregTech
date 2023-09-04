package gregtech.api.items.behavior;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover2.Cover;
import gregtech.api.cover2.CoverDefinition2;
import gregtech.api.cover2.CoverHolder;
import gregtech.api.cover2.CoverRayTracer;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.core.advancement.AdvancementTriggers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Item Behavior for an Item that places a cover onto an {@link CoverHolder}.
 */
public class CoverItemBehavior implements IItemBehaviour {

    private final CoverDefinition2 definition;

    /**
     * @param definition the Cover this item places
     */
    public CoverItemBehavior(@NotNull CoverDefinition2 definition) {
        this.definition = definition;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, @NotNull World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) return EnumActionResult.PASS;

        CoverHolder coverHolder = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, null);
        if (coverHolder == null) return EnumActionResult.PASS;

        EnumFacing coverSide = CoverRayTracer.rayTraceCoverableSide(coverHolder, player);
        if (coverSide == null) return EnumActionResult.PASS;

        if (coverHolder.hasCover(coverSide)) return EnumActionResult.PASS;

        if (world.isRemote) return EnumActionResult.SUCCESS;

        Cover cover = definition.createCover(coverHolder, coverSide);
        if (!coverHolder.canPlaceCoverOnSide(coverSide)) return EnumActionResult.PASS;
        if (!cover.canAttach(coverHolder, coverSide)) return EnumActionResult.PASS;

        ItemStack itemStack = player.getHeldItem(hand);

        coverHolder.addCover(coverSide, cover);
        cover.onAttachment(coverHolder, coverSide, player, itemStack);

        AdvancementTriggers.FIRST_COVER_PLACE.trigger((EntityPlayerMP) player);

        if (!player.isCreative()) {
            if (itemStack.isEmpty()) return EnumActionResult.FAIL;
            itemStack.shrink(1);
        }

        return EnumActionResult.SUCCESS;
    }

    public @NotNull CoverDefinition2 getDefinition() {
        return definition;
    }
}
