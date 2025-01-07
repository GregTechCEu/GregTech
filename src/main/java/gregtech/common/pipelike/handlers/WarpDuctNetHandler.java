package gregtech.common.pipelike.handlers;

import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.common.pipelike.block.warp.WarpDuctStructure;
import gregtech.common.pipelike.net.warp.WorldWarpNet;

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
        if (structure instanceof WarpDuctStructure w) {
            WorldPipeNode node = WorldWarpNet.getWorldNet(world).getOrCreateNode(pos);
            node.getData().setLogicEntry(WeightFactorLogic.TYPE.getWith(w.weight()));
            return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<WorldPipeNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof WarpDuctStructure) {
            WorldPipeNode node = WorldWarpNet.getWorldNet(world).getNode(pos);
            if (node != null) return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        WorldWarpNet net = WorldWarpNet.getWorldNet(world);
        WorldPipeNode node = net.getNode(pos);
        if (node != null) net.removeNode(node);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeStructure structure) {
        tooltip.add(I18n.format("tile.warp_duct_normal.tooltip1"));
    }
}
