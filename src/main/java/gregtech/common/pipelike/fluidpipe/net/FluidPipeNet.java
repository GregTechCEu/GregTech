package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> implements ITickable {

    private final Set<FluidStack> fluids = new HashSet<>();
    private Set<FluidStack> dirtyStacks = new HashSet<>();

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        dirtyStacks.addAll(fluids);
    }

    protected int drain(FluidStack stack) {
        if (stack == null || stack.amount <= 0) return 0;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                int amount = Math.min(stack.amount, stack1.amount);
                stack1.amount -= amount;
                markDirtyPipeNetStack(stack1);
                return amount;
            }
        }
        return 0;
    }

    protected void fill(FluidStack stack) {
        if (stack == null || stack.amount <= 0) return;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                stack1.amount += stack.amount;
                markDirtyPipeNetStack(stack1);
                return;
            }
        }
        fluids.add(stack);
        markDirty(stack);
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet1) {
        super.transferNodeData(transferredNodes, parentNet1);
        FluidPipeNet parentNet = (FluidPipeNet) parentNet1;
        fluids.addAll(parentNet.fluids);
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

    @Override
    public void update() {
        if (dirtyStacks.size() == 0) return;
        Iterator<FluidStack> iterator = dirtyStacks.iterator();
        while (iterator.hasNext()) {
            FluidStack dirtyStack = iterator.next();
            if (dirtyStack.amount <= 0) {
                iterator.remove();
                continue;
            }
            int c = dirtyStack.amount / getAllNodes().size();
            int m = dirtyStack.amount % getAllNodes().size();
            int overflow = 0;
            for (BlockPos pos : getAllNodes().keySet()) {
                int count = c;
                if (m > 0) {
                    count++;
                    m--;
                }
                FluidStack stack = dirtyStack.copy();
                stack.amount = count;
                TileEntityFluidPipe tile = (TileEntityFluidPipe) getWorldData().getTileEntity(pos);
                int channel = tile.findChannel(dirtyStack);
                overflow += tile.setContainingFluid(stack, channel);
            }
            dirtyStack.amount -= overflow;
            iterator.remove();
        }
        dirtyStacks.clear();
    }

    private void markDirtyPipeNetStack(FluidStack stack) {
        if (stack != null && stack.amount > 0) {
            dirtyStacks.add(stack);
        }
    }

    public void markDirty(FluidStack stack) {
        if (stack == null || stack.amount <= 0) return;
        for (FluidStack stack1 : this.fluids) {
            if (stack1.isFluidEqual(stack)) {
                dirtyStacks.add(stack);
                return;
            }
        }
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
