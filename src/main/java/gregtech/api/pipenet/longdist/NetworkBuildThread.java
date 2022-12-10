package gregtech.api.pipenet.longdist;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class NetworkBuildThread implements Runnable {

    private final LinkedList<BlockPos> starts = new LinkedList<>();
    private final LongDistanceNetwork.WorldData worldData;
    private LongDistanceNetwork network;
    private final World world;
    private final LinkedList<BlockPos> currentPoints = new LinkedList<>();
    private final ObjectOpenHashSet<BlockPos> walked = new ObjectOpenHashSet<>();
    private final List<BlockPos> pipes = new ArrayList<>();
    private final List<MetaTileEntityLongDistanceEndpoint> endpoints = new ArrayList<>();

    public NetworkBuildThread(LongDistanceNetwork.WorldData worldData, LongDistanceNetwork network, BlockPos start) {
        this.worldData = worldData;
        this.network = network;
        this.world = worldData.getWorld();
        this.starts.add(start);
    }

    public NetworkBuildThread(LongDistanceNetwork.WorldData worldData, LongDistanceNetwork network, Collection<BlockPos> starts) {
        this.worldData = worldData;
        this.network = network;
        this.world = worldData.getWorld();
        this.starts.addAll(starts);
    }

    @Override
    public void run() {
        boolean first = true;
        while (!starts.isEmpty()) {
            BlockPos start = starts.pollFirst();
            if (first) {
                checkNetwork(start);
            } else {
                LongDistanceNetwork ldn = worldData.getNetwork(start);
                if (ldn != null) {
                    continue;
                }
                this.network = this.network.getPipeType().createNetwork(this.worldData);
                this.currentPoints.clear();
                this.walked.clear();
                this.pipes.clear();
                this.endpoints.clear();
                checkNetwork(start);
            }
            first = false;
        }
    }

    private void checkNetwork(BlockPos start) {
        this.currentPoints.add(start);
        checkPos(world.getBlockState(start), start);
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        while (!currentPoints.isEmpty()) {
            BlockPos current = currentPoints.pollFirst();
            for (EnumFacing facing : EnumFacing.VALUES) {
                pos.setPos(current).move(facing);
                if (walked.contains(pos)) {
                    continue;
                }
                IBlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock().isAir(blockState, world, pos)) {
                    continue;
                }
                checkPos(blockState, pos);
            }
        }
        pos.release();
        network.setData(pipes, endpoints);
    }

    private void checkPos(IBlockState blockState, BlockPos pos) {
        BlockPos bp = pos.toImmutable();
        if (blockState.getBlock() instanceof BlockLongDistancePipe && network.getPipeType().isValidBlock(blockState)) {
            pipes.add(bp);
            currentPoints.addLast(bp);
        } else {
            MetaTileEntityLongDistanceEndpoint endpoint = MetaTileEntityLongDistanceEndpoint.tryGet(world, pos);
            if (endpoint != null && network.getPipeType().isValidEndpoint(endpoint)) {
                pipes.add(bp);
                endpoints.add(endpoint);
            }
        }
        walked.add(bp);
    }
}
