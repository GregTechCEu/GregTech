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
import net.minecraftforge.fluids.FluidTank;

import java.util.*;

public class FluidPipeNet extends PipeNet<FluidPipeProperties> implements ITickable {

    private final Set<FluidStack> fluids = new HashSet<>();
    private final Map<FluidStack, BlockPos> dirtyStacks = new HashMap<>();
    private final Map<FluidStack, BlockPos> fluidsToRemove = new HashMap<>();
    private int netCapacity = -1;

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    public boolean isDirty() {
        return netCapacity < 0;
    }

    public void invalidateNetCapacity() {
        netCapacity = -1;
    }

    public void markDirty(BlockPos pos) {
        GTLog.logger.info("Marking dirty");
        invalidateNetCapacity();
        for (FluidStack fluid : fluids) {
            dirtyStacks.put(fluid, pos);
        }
    }

    public int drain(FluidStack stack, BlockPos pos, boolean silent, boolean doDrain) {
        if (isDirty() || stack == null || stack.amount <= 0) return 0;
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
                    dirtyStacks.put(stack1, pos);
                GTLog.logger.info("Drained {} * {} from net", stack.getLocalizedName(), amount);
                return amount;
            }
        }
        GTLog.logger.error("Tried draining {} * {} but is not in the net", stack.getFluid().getName(), stack.amount);
        return 0;
    }

    public int fill(FluidStack stack, BlockPos pos, boolean doFill) {
        if (isDirty() || stack == null || stack.amount <= 0) return 0;
        for (FluidStack stack1 : fluids) {
            if (stack1.isFluidEqual(stack)) {
                GTLog.logger.info("Filling {} * {} to net", stack.getLocalizedName(), stack.amount);
                int amount = Math.min(stack.amount, netCapacity - stack1.amount);
                if (!doFill || amount <= 0)
                    return amount;
                stack1.amount += amount;
                dirtyStacks.put(stack1, pos);
                return amount;
            }
        }
        stack.amount = Math.min(stack.amount, netCapacity - stack.amount);
        if (!doFill)
            return stack.amount;
        GTLog.logger.info("Filling new fluid {} * {} to net", stack.getLocalizedName(), stack.amount);
        fluids.add(stack);
        dirtyStacks.put(stack, pos);
        return stack.amount;
    }

    private void recountFluids() {
        fluids.clear();
        if(getAllNodes().size() == 0)
            return;
        GTLog.logger.info("Recounting fluids in net");
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

    @Override
    public void update() {
        if (getWorldData() != null && getAllNodes().size() > 0) {
            if (isDirty()) {
                netCapacity = FluidNetWalker.getTotalCapacity(getWorldData(), getAllNodes().keySet().iterator().next());
                for (FluidStack stack : fluids) {
                    stack.amount = Math.min(stack.amount, netCapacity);
                }
            }
            if (fluidsToRemove.size() > 0) {
                for (Map.Entry<FluidStack, BlockPos> entry : fluidsToRemove.entrySet()) {
                    List<TileEntityFluidPipe> pipes = FluidNetWalker.getPipesForFluid(getWorldData(), entry.getValue(), entry.getKey());
                    for (TileEntityFluidPipe pipe : pipes) {
                        pipe.setContainingFluid(null, pipe.findChannel(entry.getKey()), false);
                    }
                }
                fluidsToRemove.clear();
            }
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
                dirtyStacks.put(stack1, pos);
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
        nbt.setInteger("Capacity", netCapacity);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.netCapacity = nbt.getInteger("Capacity");
        this.fluids.clear();
        NBTTagList fluids = nbt.getTagList("Fluids", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fluids.tagCount(); i++) {
            this.fluids.add(FluidStack.loadFluidStackFromNBT((NBTTagCompound) fluids.get(i)));
        }
    }
}
