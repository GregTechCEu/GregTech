package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeGatherer;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> {

    private final Map<BlockPos, List<Inventory>> NET_DATA = new HashMap<>();

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    public List<Inventory> getNetData(BlockPos pipePos) {
        return NET_DATA.computeIfAbsent(pipePos, pos -> {
            List<Inventory> data = FluidNetWalker.createNetData(this, getWorldData(), pos);
            data.sort(Comparator.comparingInt(inv -> inv.distance));
            return data;
        });
    }

    public void nodeNeighbourChanged(BlockPos pos) {
        NET_DATA.clear();
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        NET_DATA.clear();
    }

    public void destroyNetwork(BlockPos source, boolean isLeaking, boolean isBurning, int temp) {
        World world = getWorldData();
        List<IPipeTile<?, ?>> pipes = PipeGatherer.gatherPipesInDistance(this, world, source, pipe -> {
            if (pipe instanceof TileEntityFluidPipe) {
                TileEntityFluidPipe fluidPipe = (TileEntityFluidPipe) pipe;
                return (isBurning && fluidPipe.getNodeData().maxFluidTemperature < temp) || (isLeaking && !fluidPipe.getNodeData().gasProof);
            }
            return false;
        }, 2 + world.rand.nextInt(4));
        for (IPipeTile<?, ?> pipeTile : pipes) {
            BlockPos pos = pipeTile.getPipePos();
            Random random = world.rand;
            if (isBurning) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP,
                        EnumParticleTypes.FLAME, 3 + random.nextInt(2), random);
                if (random.nextInt(4) == 0)
                    TileEntityFluidPipe.setNeighboursToFire(world, pos);
            } else
                world.setBlockToAir(pos);
            if (isLeaking && world.rand.nextInt(isBurning ? 3 : 7) == 0) {
                world.createExplosion(null,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1.0f + world.rand.nextFloat(), false);
            }
        }
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet1) {
        super.transferNodeData(transferredNodes, parentNet1);
        FluidPipeNet parentNet = (FluidPipeNet) parentNet1;
        NET_DATA.clear();
        parentNet.NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.maxFluidTemperature);
        tagCompound.setInteger("throughput", nodeData.throughput);
        tagCompound.setBoolean("gas_proof", nodeData.gasProof);
        tagCompound.setInteger("channels", nodeData.tanks);
    }

    @Override
    protected FluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        int channels = tagCompound.getInteger("channels");
        return new FluidPipeProperties(maxTemperature, throughput, gasProof, channels);
    }

    public static class Inventory {
        private final BlockPos pipePos;
        private final EnumFacing faceToHandler;
        private final int distance;
        private final Set<Object> objectsInPath;
        private final int minRate;
        private FluidStack lastTransferredFluid;
        private final List<TileEntityFluidPipe> holdingPipes;

        public Inventory(BlockPos pipePos, EnumFacing facing, int distance, Set<Object> objectsInPath, int minRate, List<TileEntityFluidPipe> holdingPipes) {
            this.pipePos = pipePos;
            this.faceToHandler = facing;
            this.distance = distance;
            this.objectsInPath = objectsInPath;
            this.minRate = minRate;
            this.holdingPipes = holdingPipes;
        }

        public void setLastTransferredFluid(FluidStack lastTransferredFluid) {
            this.lastTransferredFluid = lastTransferredFluid;
        }

        public FluidStack getLastTransferredFluid() {
            return lastTransferredFluid;
        }

        public Set<Object> getObjectsInPath() {
            return objectsInPath;
        }

        public int getMinThroughput() {
            return minRate;
        }

        public List<TileEntityFluidPipe> getHoldingPipes() {
            return holdingPipes;
        }

        public BlockPos getPipePos() {
            return pipePos;
        }

        public EnumFacing getFaceToHandler() {
            return faceToHandler;
        }

        public int getDistance() {
            return distance;
        }

        public BlockPos getHandlerPos() {
            return pipePos.offset(faceToHandler);
        }

        public IFluidHandler getHandler(World world) {
            TileEntity tile = world.getTileEntity(getHandlerPos());
            if (tile != null)
                return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, faceToHandler.getOpposite());
            return null;
        }
    }

}
