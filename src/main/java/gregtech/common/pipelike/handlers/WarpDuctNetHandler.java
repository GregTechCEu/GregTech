package gregtech.common.pipelike.handlers;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO
public class WarpDuctNetHandler implements IPipeNetNodeHandler {

    public static final WarpDuctNetHandler INSTANCE = new WarpDuctNetHandler();

    @Override
    public @NotNull Collection<WorldPipeNode> getOrCreateFromNets(World world, BlockPos pos, IPipeStructure structure) {
        return Collections.emptySet();
    }

    @Override
    public @NotNull Collection<WorldPipeNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {}

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeStructure structure) {
        tooltip.add(I18n.format("tile.warp_duct_normal.tooltip1"));
    }
}
