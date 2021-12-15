package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> implements ITickable {

    private final Set<FluidStack> fluids = new HashSet<>();
    private final Map<FluidStack, Map<BlockPos, Integer>> dirtyStacks = new HashMap<>();
    private final Map<FluidStack, BlockPos> fluidsToRemove = new HashMap<>();
    private final Map<FluidStack, List<TileEntityFluidPipeTickable>> requestedPipes = new HashMap<>();
    private long netCapacity = -1;
    private long timer = 0;

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    public boolean isDirty() {
        return netCapacity < 0;
    }

    public void invalidateNetCapacity() {
        netCapacity = -1;
    }

    private void checkDirty() {
        if (isDirty() && getAllNodes().size() > 0) {
            netCapacity = FluidNetWalker.getTotalCapacity(getWorldData(), getAllNodes().keySet().iterator().next());
            for (FluidStack stack : fluids) {
                stack.amount = (int) Math.min(stack.amount, netCapacity);
            }
        }
    }

    public int drain(FluidStack stack, BlockPos pos, boolean silent, boolean doDrain) {
        checkDirty();
        if (stack == null || stack.amount <= 0) return 0;
        Iterator<FluidStack> iterator = fluids.iterator();
        while (iterator.hasNext()) {
            FluidStack stack1 = iterator.next();
            if (stack1.isFluidEqual(stack)) {
                int amount = Math.min(stack.amount, stack1.amount);
                if (!doDrain || amount <= 0)
                    return amount;
                stack1.amount -= amount;
                if (stack1.amount <= 0) {
                    iterator.remove();
                    fluidsToRemove.put(stack1, pos);
                } else if (!silent)
                    markDirty(stack1, pos, -amount);
                return amount;
            }
        }
        throw new IllegalStateException(String.format("Tried draining %s * %s but is not in the net", stack.getFluid().getName(), stack.amount));
        //GTLog.logger.error("Tried draining {} * {} but is not in the net", stack.getFluid().getName(), stack.amount);
        //return 0;
    }

    private void markDirty(FluidStack stack, BlockPos pos, int amount) {
        dirtyStacks.computeIfAbsent(stack, key -> new HashMap<>()).merge(pos, amount, Integer::sum);
    }

    public int fill(FluidStack stack, BlockPos pos, boolean doFill) {
        checkDirty();
        if (stack == null || stack.amount <= 0) return 0;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                int amount = (int) Math.min(stack.amount, netCapacity - stack1.amount);
                if (!doFill || amount <= 0)
                    return amount;
                stack1.amount += amount;
                markDirty(stack1, pos, amount);
                return amount;
            }
        }
        stack.amount = (int) Math.min(stack.amount, netCapacity);
        if (!doFill)
            return stack.amount;
        fluids.add(stack);
        markDirty(stack, pos, stack.amount);
        return stack.amount;
    }

    private void recountFluids() {
        fluids.clear();
        if (getAllNodes().size() == 0)
            return;
        fluids.addAll(FluidNetWalker.countFluid(getWorldData(), getAllNodes().keySet().iterator().next()));
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet1) {
        super.transferNodeData(transferredNodes, parentNet1);
        FluidPipeNet parentNet = (FluidPipeNet) parentNet1;
        invalidateNetCapacity();
        parentNet.invalidateNetCapacity();
        recountFluids();
        parentNet.recountFluids();
    }

    public void requestFluidForPipe(FluidStack stack, TileEntityFluidPipeTickable pipe) {
        requestedPipes.computeIfAbsent(getFluidStack(stack), key -> new ArrayList<>()).add(pipe);
    }

    @Override
    public void update() {
        timer++;
        if (getWorldData() != null && getAllNodes().size() > 0) {
            checkDirty();
            if (requestedPipes.size() == 0)
                timer = 0;
            else if (timer % TileEntityFluidPipe.FREQUENCY == 0) {
                for (Map.Entry<FluidStack, List<TileEntityFluidPipeTickable>> entry : requestedPipes.entrySet()) {
                    int toDistribute = entry.getKey().amount;
                    BlockPos pos = null;
                    while (toDistribute > 0 && entry.getValue().size() > 0) {
                        int c = toDistribute / entry.getValue().size();
                        int m = c == 0 ? toDistribute % entry.getValue().size() : 0;
                        Iterator<TileEntityFluidPipeTickable> iterator = entry.getValue().iterator();
                        while (iterator.hasNext()) {
                            TileEntityFluidPipeTickable pipe = iterator.next();
                            FluidStack toInsert = entry.getKey().copy();
                            toInsert.amount = c;
                            if (m > 0) {
                                toInsert.amount++;
                                m--;
                            }
                            int inserted = pipe.distribute(toInsert);
                            if (inserted > 0 && pos == null) {
                                pos = pipe.getPos();
                            }
                            if (inserted < toInsert.amount) {
                                iterator.remove();
                            }
                            toDistribute -= inserted;
                        }
                    }
                    if (pos != null) {
                        FluidStack drainFromNet = entry.getKey().copy();
                        drainFromNet.amount = entry.getKey().amount - toDistribute;
                        drain(drainFromNet, pos, false, true);
                    }
                }
                requestedPipes.clear();
            }
            if (fluidsToRemove.size() > 0) {
                for (Map.Entry<FluidStack, BlockPos> entry : fluidsToRemove.entrySet()) {
                    List<TileEntityFluidPipe> pipes = FluidNetWalker.getPipesForFluid(getWorldData(), entry.getValue(), entry.getKey(), false).getPipes();
                    for (TileEntityFluidPipe pipe : pipes) {
                        pipe.setContainingFluid(null, pipe.findChannel(entry.getKey()), false);
                    }
                }
                fluidsToRemove.clear();
            }
            if (dirtyStacks.size() > 0) {
                for(Map.Entry<FluidStack, Map<BlockPos, Integer>> entry : dirtyStacks.entrySet()) {
                    FluidStack dirtyStack = entry.getKey();
                    if (dirtyStack.amount <= 0) {
                        continue;
                    }
                    Map<BlockPos, Integer> subMap = entry.getValue();
                    Iterator<Map.Entry<BlockPos, Integer>> iterator = subMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<BlockPos, Integer> entry2 = iterator.next();
                        FluidNetWalker walker = FluidNetWalker.getPipesForFluid(getWorldData(), entry2.getKey(), dirtyStack, true);
                        List<TileEntityFluidPipe> pipes = walker.getPipes();
                        if (pipes.size() == 0) {
                            continue;
                        }
                        for(TileEntityFluidPipe pipe : pipes) {
                            subMap.remove(pipe.getPos());
                        }
                        long amount = walker.getCount();
                        amount += entry2.getValue();
                        int round = 0;
                        while (amount > 0 && pipes.size() > 0) {
                            int c = (int) (amount / pipes.size());
                            int m = (int) (amount % pipes.size());

                            Iterator<TileEntityFluidPipe> pipeIterator = pipes.iterator();
                            while (pipeIterator.hasNext()) {
                                TileEntityFluidPipe pipe = pipeIterator.next();
                                int count = c;
                                if (m > 0) {
                                    count++;
                                    m--;
                                }
                                FluidStack stack = dirtyStack.copy();
                                stack.amount = count;
                                int channel = pipe.findChannel(stack);
                                pipe.setContainingFluid(null, channel, false);
                                int set = pipe.setContainingFluid(stack, channel, round > 0);
                                if (count > set)
                                    pipeIterator.remove();
                                amount -= set;
                            }
                            round++;
                        }
                        dirtyStack.amount -= amount;
                    }
                }
                dirtyStacks.clear();
            }
        }
    }

    public FluidStack getFluidStack(FluidStack stack) {
        for (FluidStack stack1 : this.fluids) {
            if (stack1.isFluidEqual(stack)) {
                return stack1;
            }
        }
        return null;
    }

    public void markDirty(FluidStack stack, BlockPos pos) {
        if (stack == null)
            throw new NullPointerException("FluidStack can't be null");
        for (FluidStack stack1 : this.fluids) {
            if (stack1.isFluidEqual(stack)) {
                markDirty(stack1, pos, 0);
                return;
            }
        }
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setInteger("channels", nodeData.getTanks());
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
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList fluids = new NBTTagList();
        for (FluidStack fluid : this.fluids) {
            if (fluid.amount > 0)
                fluids.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("Fluids", fluids);
        nbt.setLong("Capacity", netCapacity);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.netCapacity = nbt.getLong("Capacity");
        this.fluids.clear();
        NBTTagList fluids = nbt.getTagList("Fluids", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fluids.tagCount(); i++) {
            this.fluids.add(FluidStack.loadFluidStackFromNBT((NBTTagCompound) fluids.get(i)));
        }
    }
}
