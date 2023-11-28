package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GrassPathBehavior implements IToolBehavior {

    public static final GrassPathBehavior INSTANCE = new GrassPathBehavior();

    protected GrassPathBehavior() {/**/}

    @NotNull
    @Override
    public EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                      @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        if (facing == EnumFacing.DOWN) return EnumActionResult.PASS;

        ItemStack stack = player.getHeldItem(hand);
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;
        // only attempt to till if the center block is tillable
        if (world.isAirBlock(pos.up()) && isBlockPathConvertible(stack, world, player, pos, null)) {
            if (aoeDefinition == AoESymmetrical.none()) {
                blocks = ImmutableSet.of(pos);
            } else {
                RayTraceResult rayTraceResult = ToolHelper.getPlayerDefaultRaytrace(player);

                if (rayTraceResult == null) return EnumActionResult.PASS;
                if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return EnumActionResult.PASS;
                if (rayTraceResult.sideHit == null) return EnumActionResult.PASS;

                blocks = getPathConvertibleBlocks(stack, aoeDefinition, world, player, rayTraceResult);
                blocks.add(rayTraceResult.getBlockPos());
            }
        } else return EnumActionResult.PASS;

        boolean pathed = false;
        for (BlockPos blockPos : blocks) {
            pathed |= world.setBlockState(blockPos, Blocks.GRASS_PATH.getDefaultState());
            ToolHelper.damageItem(stack, player);
            if (stack.isEmpty()) break;
        }

        if (pathed) {
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_SHOVEL_FLATTEN,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public static Set<BlockPos> getPathConvertibleBlocks(ItemStack stack, AoESymmetrical aoeDefinition, World world,
                                                         EntityPlayer player, RayTraceResult rayTraceResult) {
        return ToolHelper.iterateAoE(stack, aoeDefinition, world, player, rayTraceResult,
                GrassPathBehavior::isBlockPathConvertible);
    }

    private static boolean isBlockPathConvertible(ItemStack stack, World world, EntityPlayer player, BlockPos pos,
                                                  @Nullable BlockPos hitBlockPos) {
        if (world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            return block == Blocks.GRASS || block == Blocks.DIRT; // native dirt to path
        }
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.grass_path"));
    }
}
