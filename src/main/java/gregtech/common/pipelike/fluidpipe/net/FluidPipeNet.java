package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.GTLog;
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
        Iterator<FluidStack> iterator = fluids.iterator();
        while (iterator.hasNext()) {
            FluidStack stack1 = iterator.next();
            if (stack1.isFluidEqual(stack)) {
                int amount = Math.min(stack.amount, stack1.amount);
                stack1.amount -= amount;
                if (stack1.amount <= 0)
                    iterator.remove();
                else
                    dirtyStacks.put(stack1, pos);
                GTLog.logger.info("Drained {} * {} from net", stack.getLocalizedName(), amount);
                return amount;
            }
        }
        GTLog.logger.error("Tried draining {} * {} but is not in the net", stack.getFluid().getName(), stack.amount);
        return 0;
    }

    protected void fill(FluidStack stack, BlockPos pos) {
        if (stack == null || stack.amount <= 0) return;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                GTLog.logger.info("Filling {} * {} to net", stack.getLocalizedName(), stack.amount);
                stack1.amount += stack.amount;
                dirtyStacks.put(stack1, pos);
                return;
            }
        }
        GTLog.logger.info("Filling new fluid {} * {} to net", stack.getLocalizedName(), stack.amount);
        fluids.add(stack);
        dirtyStacks.put(stack, pos);
    }

    private void recountFluids() {
        Iterator<FluidStack> iterator = fluids.iterator();
        while (iterator.hasNext()) {
            FluidStack fluid = iterator.next();
            fluid.amount = FluidNetWalker.countFluid(getWorldData(), getAllNodes().keySet().iterator().next(), fluid, true).getCount();
            if (fluid.amount <= 0)
                iterator.remove();
        }
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet1) {
        super.transferNodeData(transferredNodes, parentNet1);
        FluidPipeNet parentNet = (FluidPipeNet) parentNet1;
        fluids.addAll(parentNet.fluids);
        recountFluids();
        parentNet.recountFluids();
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
        if (getWorldData() != null) {
            for (Map.Entry<FluidStack, List<TileEntityFluidPipe>> entry : requestedPipes.entrySet()) {
                FluidStack stack = entry.getKey();
                List<TileEntityFluidPipe> pipes = entry.getValue();
                FluidNetWalker walker = FluidNetWalker.countFluid(getWorldData(), pipes.get(0).getPos(), stack, false);
                int c = walker.getCount() / pipes.size();
                int m = c == 0 ? walker.getCount() % pipes.size() : 0;
                GTLog.logger.info("Distributing {} * {} to {} pipes", stack.getLocalizedName(), walker.getCount(), pipes.size());
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
                if (inserted > 0) {
                    FluidStack toDrain = stack.copy();
                    toDrain.amount = inserted;
                    for (TileEntityFluidPipe pipe : walker.getPipes()) {
                        if (toDrain.amount <= 0)
                            break;
                        FluidStack drained = pipe.getTankList().drain(toDrain, true);
                        if (drained != null)
                            toDrain.amount -= drained.amount;
                    }
                    GTLog.logger.info("Inserted {}, Drained {}", inserted, inserted - toDrain.amount);
                }
            }
            requestedPipes.clear();
            if (dirtyStacks.size() > 0) {
                GTLog.logger.info("{} dirty stacks in net", dirtyStacks.size());
                Iterator<FluidStack> iterator = dirtyStacks.keySet().iterator();
                while (iterator.hasNext()) {
                    FluidStack dirtyStack = iterator.next();
                    if (dirtyStack.amount <= 0) {
                        iterator.remove();
                        continue;
                    }
                    GTLog.logger.info("Evening out {} * {}", dirtyStack.getLocalizedName(), dirtyStack.amount);
                    List<TileEntityFluidPipe> pipes = FluidNetWalker.getPipesForFluid(getWorldData(), dirtyStacks.get(dirtyStack), dirtyStack);
                    if (pipes.size() == 0) {
                        iterator.remove();
                        continue;
                    }
                    GTLog.logger.info("Distributing to {} pipes", pipes.size());
                    int amount = dirtyStack.amount;
                    int round = 0;
                    while (amount > 0 && pipes.size() > 0) {
                        int c = amount / pipes.size();
                        int m = amount % pipes.size();

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
                            int set = pipe.setFluidAuto(stack, round > 0);
                            if (count > set)
                                pipeIterator.remove();
                            amount -= set;
                        }
                        round++;
                    }
                    dirtyStack.amount -= amount;
                    iterator.remove();
                }
                dirtyStacks.clear();
            }
        }
    }

    private void markDirtyPipeNetStack(FluidStack stack, BlockPos pos) {
        if (stack == null)
            throw new NullPointerException("FluidStack can't be null");
        dirtyStacks.put(stack, pos);
    }

    public void markDirty(FluidStack stack, BlockPos pos) {
        if (stack == null)
            throw new NullPointerException("FluidStack can't be null");
        for (FluidStack stack1 : this.fluids) {
            if (stack1.isFluidEqual(stack)) {
                dirtyStacks.put(stack1, pos);
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
