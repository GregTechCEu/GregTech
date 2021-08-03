package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.MonolithicPipeNet;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.pipenet.tickable.TickableWorldPipeNetEventHandler;
import gregtech.api.pipenet.*;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.fluidpipe.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class FluidPipeNet extends MonolithicPipeNet<FluidPipeProperties> implements ITickable {

    private final Map<BlockPos, List<Inventory>> NET_DATA = new HashMap<>();

    private FluidStack[] fluids;
    private int[] emptyTimer;

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

    public FluidStack getContainedFluid(int channel) {
        return fluids == null ? null : fluids[channel];
    }

    public FluidStack[] getContainedFluids() {
        return Arrays.copyOf(fluids, fluids.length);
    }

    protected void setContainingFluid(FluidStack stack, int channel) {
        this.fluids[channel] = stack;
        this.emptyTimer[channel] = 20;
    }

    private void emptyTank(int channel) {
        this.fluids[channel] = null;
    }

    public void nodeNeighbourChanged(BlockPos pos) {
        NET_DATA.clear();
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        NET_DATA.clear();
    }

    @Override
    protected void addNode(BlockPos nodePos, Node<FluidPipeProperties> node) {
        super.addNode(nodePos, node);
        if (fluids == null) {
            fluids = new FluidStack[node.data.tanks];
            emptyTimer = new int[node.data.tanks];
            Arrays.fill(emptyTimer, 0);
        }
    }

    public void destroyNetwork(BlockPos source, boolean isLeaking, boolean isBurning) {
        World world = getWorldData();
        List<IPipeTile<?, ?>> pipes = PipeGatherer.gatherPipesInDistance(this, world, source, pipe -> pipe instanceof TileEntityFluidPipe, 2 + world.rand.nextInt(5));
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
        FluidStack parentFluid = parentNet.getContainedFluid(0);
        if (parentFluid != null && parentFluid.amount > 0) {
            if (parentNet.getAllNodes().isEmpty()) {
                //if this is merge of pipe nets, just add all fluid to our internal tank
                //use fillInternal to ignore throughput restrictions
                for (int i = 0; i < fluids.length; i++) {
                    setContainingFluid(parentNet.getContainedFluid(i), i);
                }

            }
        }
    }

    public boolean areTanksEmpty() {
        if (fluids == null) return true;
        for (FluidStack fluidStack : fluids)
            if (fluidStack != null)
                return false;
        return true;
    }

    /**
     * Finds a channel for the given stack
     * if fluids are null an array will be created with the given pipe
     *
     * @param pipe  to create fluids for
     * @param stack to find a channel for
     * @return channel
     */
    public int findChannelWith(TileEntityFluidPipe pipe, FluidStack stack) {
        if (fluids == null || fluids.length != pipe.getNodeData().tanks) {
            fluids = new FluidStack[pipe.getNodeData().tanks];
            emptyTimer = new int[pipe.getNodeData().tanks];
            Arrays.fill(emptyTimer, 0);
            return 0;
        }
        return findChannel(stack);
    }

    /**
     * Finds a channel for the given fluid
     *
     * @param stack to find a channel fot
     * @return channel
     * @throws NullPointerException if fluids are null
     */
    public int findChannel(FluidStack stack) {
        if (fluids == null)
            throw new NullPointerException("Fluids can't be null");
        if (fluids.length == 1) {
            FluidStack channelStack = fluids[0];
            return (channelStack == null || channelStack.amount <= 0 || channelStack.isFluidEqual(stack)) ? 0 : -1;
        }
        int emptyTank = -1;
        for (int i = fluids.length - 1; i >= 0; i--) {
            FluidStack channelStack = fluids[i];
            if (channelStack == null || channelStack.amount <= 0)
                emptyTank = i;
            else if (channelStack.isFluidEqual(stack))
                return i;
        }
        return emptyTank;
    }

    @Override
    protected boolean areNodesCustomContactable(FluidPipeProperties first, FluidPipeProperties second, PipeNet<FluidPipeProperties> secondNodeNet) {
        FluidPipeNet fluidPipeNet = (FluidPipeNet) secondNodeNet;
        if (!super.areNodesCustomContactable(first, second, secondNodeNet)) return false;
        if (first.tanks == 1) {
            return secondNodeNet == null ||
                    getContainedFluid(0) == null ||
                    fluidPipeNet.getContainedFluid(0) == null ||
                    getContainedFluid(0).isFluidEqual(fluidPipeNet.getContainedFluid(0));
        }
        return secondNodeNet == null || areTanksEmpty() || fluidPipeNet.areTanksEmpty();
    }

    @Override
    protected void writeNodeData(FluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.maxFluidTemperature);
        tagCompound.setInteger("throughput", nodeData.throughput);
        tagCompound.setBoolean("gas_proof", nodeData.gasProof);
    }

    @Override
    protected FluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        return new FluidPipeProperties(maxTemperature, throughput, gasProof);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < fluids.length; i++) {
            FluidStack stack1 = fluids[i];
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setInteger("Timer", emptyTimer[i]);
            if (stack1 == null)
                fluidTag.setBoolean("isNull", true);
            else
                list.appendTag(stack1.writeToNBT(fluidTag));
        }
        nbt.setTag("Fluids", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        if (fluids == null) {
            fluids = new FluidStack[list.tagCount()];
            emptyTimer = new int[list.tagCount()];
        }
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            emptyTimer[i] = tag.getInteger("Timer");
            if (!tag.hasKey("isNull"))
                fluids[i] = FluidStack.loadFluidStackFromNBT(tag);
        }
    }

    @Override
    public void update() {
        if (fluids == null) return;
        for (int i = 0; i < fluids.length; i++) {
            if (emptyTimer[i] > 0 && --emptyTimer[i] == 0)
                emptyTank(i);
        }
    }

    public static class Inventory {
        private final BlockPos pipePos;
        private final EnumFacing faceToHandler;
        private final int distance;

        public Inventory(BlockPos pipePos, EnumFacing facing, int distance) {
            this.pipePos = pipePos;
            this.faceToHandler = facing;
            this.distance = distance;
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
