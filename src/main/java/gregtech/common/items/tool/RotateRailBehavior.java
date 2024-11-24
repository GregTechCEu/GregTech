package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RotateRailBehavior implements IToolBehavior {

    public static final RotateRailBehavior INSTANCE = new RotateRailBehavior();

    protected RotateRailBehavior() {/**/}

    @NotNull
    @Override
    public EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                           @NotNull EnumFacing facing, float hitX, float hitY, float hitZ,
                                           @NotNull EnumHand hand) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockRailBase) {
            if (world.setBlockState(pos, state.withRotation(Rotation.CLOCKWISE_90))) {
                ToolHelper.onActionDone(player, world, hand);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.rail_rotation"));
    }
}
