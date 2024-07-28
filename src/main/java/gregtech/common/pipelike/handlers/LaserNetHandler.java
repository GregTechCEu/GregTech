package gregtech.common.pipelike.handlers;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.common.pipelike.block.laser.LaserStructure;
import gregtech.common.pipelike.net.laser.WorldLaserNet;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class LaserNetHandler implements IPipeNetNodeHandler {

    public static final LaserNetHandler INSTANCE = new LaserNetHandler();

    @Override
    public void addToNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof LaserStructure) {
            WorldLaserNet.getWorldNet(world).getOrCreateNode(pos);
        }
    }

    @Override
    public Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof LaserStructure) {
            WorldPipeNetNode node = WorldLaserNet.getWorldNet(world).getNode(pos);
            if (node != null) return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof LaserStructure) {
            WorldLaserNet net = WorldLaserNet.getWorldNet(world);
            WorldPipeNetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeStructure structure) {
        tooltip.add(I18n.format("tile.laser_pipe_normal.tooltip1"));
    }
}
