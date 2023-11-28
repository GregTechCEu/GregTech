package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.common.items.tool.rotation.ICustomRotationBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import codechicken.lib.raytracer.RayTracer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockRotatingBehavior implements IToolBehavior {

    public static final BlockRotatingBehavior INSTANCE = new BlockRotatingBehavior();

    protected BlockRotatingBehavior() {/**/}

    @Override
    public EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                           @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
                                           @NotNull EnumHand hand) {
        TileEntity te = world.getTileEntity(pos);
        // MTEs have special handling on rotation
        if (te instanceof IGregTechTileEntity) {
            return EnumActionResult.PASS;
        }

        IBlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        // leave rail rotation to Crowbar only
        if (b instanceof BlockRailBase) {
            return EnumActionResult.FAIL;
        }

        if (!player.isSneaking() && world.canMineBlockBody(player, pos)) {
            // Special cases for vanilla blocks where the default rotation behavior is less than ideal
            ICustomRotationBehavior behavior = CustomBlockRotations.getCustomRotation(b);
            if (behavior != null) {
                if (behavior.customRotate(state, world, pos, RayTracer.retraceBlock(world, player, pos))) {
                    ToolHelper.onActionDone(player, world, hand);
                    return EnumActionResult.SUCCESS;
                }
            } else if (b.rotateBlock(world, pos, side)) {
                ToolHelper.onActionDone(player, world, hand);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.block_rotation"));
    }
}
