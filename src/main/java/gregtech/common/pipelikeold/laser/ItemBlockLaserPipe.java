package gregtech.common.pipelikeold.laser;

import gregtech.api.graphnet.pipenetold.block.BlockPipe;
import gregtech.api.graphnet.pipenetold.block.ItemBlockPipe;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBlockLaserPipe extends ItemBlockPipe<LaserPipeType, LaserPipeProperties> {

    public ItemBlockLaserPipe(BlockPipe<LaserPipeType, LaserPipeProperties, NetEdge, ?> block) {
        super(block);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("tile.laser_pipe_normal.tooltip1"));

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.wire_cutter.connect"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.show_tooltips"));
        }
    }
}
