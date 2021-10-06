package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> implements ITickable {

    private final Set<FluidStack> fluids = new HashSet<>();
    private final Map<FluidStack, BlockPos> dirtyStacks = new HashMap<>();
    private final Map<FluidStack, List<TileEntityFluidPipe>> requestedPipes = new HashMap<>();

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    public void requestFluid(TileEntityFluidPipe pipe, FluidStack stack1) {
        FluidStack stack = stack1.copy();
        stack.amount = 1;
        List<TileEntityFluidPipe> pipes = requestedPipes.get(stack);
        if (pipes == null)
            pipes = new ArrayList<>();
        else if (pipes.contains(pipe))
            return;
        pipes.add(pipe);
        requestedPipes.put(stack, pipes);
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        for (FluidStack fluid : fluids) {
            dirtyStacks.put(fluid, nodePos);
        }
    }

    protected int drain(FluidStack stack, BlockPos pos) {
        if (stack == null || stack.amount <= 0) return 0;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                int amount = Math.min(stack.amount, stack1.amount);
                stack1.amount -= amount;
                markDirtyPipeNetStack(stack1, pos);
                return amount;
            }
        }
        throw new IllegalStateException("Tried to drain not existend fluid from net");
    }

    protected void fill(FluidStack stack, BlockPos pos) {
        if (stack == null || stack.amount <= 0) return;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                stack1.amount += stack.amount;
                markDirtyPipeNetStack(stack1, pos);
                return;
            }
        }
        fluids.add(stack);
        markDirtyPipeNetStack(stack, pos);
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
        for (Map.Entry<FluidStack, List<TileEntityFluidPipe>> entry : requestedPipes.entrySet()) {
            FluidStack stack = entry.getKey();
            List<TileEntityFluidPipe> pipes = entry.getValue();
            FluidNetWalker walker = FluidNetWalker.countFluid(getWorldData(), pipes.get(0).getPos(), stack);
            int c = walker.getCount() / pipes.size();
            int m = c == 0 ? walker.getCount() % pipes.size() : 0;
            int inserted = 0;
            for (TileEntityFluidPipe pipe : pipes) {
                FluidStack toInsert = stack.copy();
                toInsert.amount = c;
                if (m > 0) {
                    toInsert.amount++;
                    m--;
                }
                int i = pipe.distribute(toInsert);
                inserted += i;
            }
            FluidStack toDrain = stack.copy();
            toDrain.amount = inserted;
            for (TileEntityFluidPipe pipe : walker.getPipes()) {
                FluidStack drained = pipe.getTankList().drain(toDrain, true);
                if (drained != null)
                    toDrain.amount -= drained.amount;
            }
        }
        requestedPipes.clear();

        if (dirtyStacks.size() > 0) {
            Iterator<FluidStack> iterator = dirtyStacks.keySet().iterator();
            while (iterator.hasNext()) {
                FluidStack dirtyStack = iterator.next();
                if (dirtyStack.amount <= 0) {
                    iterator.remove();
                    continue;
                }
                List<TileEntityFluidPipe> pipes = FluidNetWalker.getPipesForFluid(getWorldData(), dirtyStacks.get(dirtyStack), dirtyStack);
                if (pipes.size() == 0) {
                    iterator.remove();
                    continue;
                }
                int c = dirtyStack.amount / pipes.size();
                int m = dirtyStack.amount % pipes.size();
                int overflow = 0;
                for (TileEntityFluidPipe pipe : pipes) {
                    int count = c;
                    if (m > 0) {
                        count++;
                        m--;
                    }
                    FluidStack stack = dirtyStack.copy();
                    stack.amount = count;
                    overflow += pipe.setFluidAuto(stack);
                }
                dirtyStack.amount -= overflow;
                iterator.remove();
            }
            dirtyStacks.clear();
        }
    }

    private void markDirtyPipeNetStack(FluidStack stack, BlockPos pos) {
        if (stack != null && stack.amount > 0) {
            dirtyStacks.put(stack, pos);
        }
    }

    public void markDirty(FluidStack stack, BlockPos pos) {
        if (stack == null || stack.amount <= 0) return;
        for (FluidStack stack1 : this.fluids) {
            if (stack1.isFluidEqual(stack)) {
                dirtyStacks.put(stack, pos);
                return;
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList fluids = new NBTTagList();
        for (FluidStack fluid : this.fluids) {
            if (fluid.amount > 0)
                fluids.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("Fluids", fluids);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.fluids.clear();
        NBTTagList fluids = nbt.getTagList("Fluids", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fluids.tagCount(); i++) {
            this.fluids.add(FluidStack.loadFluidStackFromNBT((NBTTagCompound) fluids.get(i)));
        }
    }
}
