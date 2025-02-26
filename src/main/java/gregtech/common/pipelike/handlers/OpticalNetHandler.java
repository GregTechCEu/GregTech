package gregtech.common.pipelike.handlers;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.common.pipelike.block.optical.OpticalStructure;
import gregtech.common.pipelike.net.optical.WorldOpticalNet;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class OpticalNetHandler implements IPipeNetNodeHandler {

    public static final OpticalNetHandler INSTANCE = new OpticalNetHandler();

    @Override
    public @NotNull Collection<WorldPipeNode> getOrCreateFromNets(World world, BlockPos pos,
                                                                  IPipeStructure structure) {
        if (structure instanceof OpticalStructure) {
            return Collections.singletonList(WorldOpticalNet.getWorldNet(world).getOrCreateNode(pos));
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<WorldPipeNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof OpticalStructure) {
            WorldPipeNode node = WorldOpticalNet.getWorldNet(world).getNode(pos);
            if (node != null) return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof OpticalStructure) {
            WorldOpticalNet net = WorldOpticalNet.getWorldNet(world);
            WorldPipeNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeStructure structure) {
        tooltip.add(I18n.format("tile.optical_pipe_normal.tooltip1"));
    }
}
