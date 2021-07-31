package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.MonolithicPipeNet;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.pipenet.tickable.TickableWorldPipeNetEventHandler;
import gregtech.common.pipelike.fluidpipe.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
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

    private FluidStack fluid;
    private int emptyTimer;

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
        this.emptyTimer = 0;
    }

    public List<Inventory> getNetData(BlockPos pipePos) {
        return NET_DATA.computeIfAbsent(pipePos, pos -> {
            List<Inventory> data = FluidNetWalker.createNetData(this, getWorldData(), pos);
            data.sort(Comparator.comparingInt(inv -> inv.distance));
            return data;
        });
    }

    public FluidStack getContainedFluid() {
        return fluid;
    }

    protected void setContainingFluid(FluidStack stack) {
        this.fluid = stack;
        this.emptyTimer = 20;
    }

    private void emptyTank() {
        this.fluid = null;
    }

    public void nodeNeighbourChanged(BlockPos pos) {
        NET_DATA.clear();
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        NET_DATA.clear();
    }

    public void destroyNetwork(boolean isLeaking, boolean isBurning) {
        World world = worldData.getWorld();
        ((WorldFluidPipeNet) (Object) worldData).removePipeNet(this);
        for (BlockPos nodePos : getAllNodes().keySet()) {
            TileEntity tileEntity = world.getTileEntity(nodePos);
            if (tileEntity instanceof TileEntityFluidPipe) {
                if (isBurning) {
                    world.setBlockState(nodePos, Blocks.FIRE.getDefaultState());
                } else {
                    world.setBlockToAir(nodePos);
                }
            }

            Random random = world.rand;
            if (isBurning) {
                TileEntityFluidPipe.spawnParticles(world, nodePos, EnumFacing.UP,
                        EnumParticleTypes.FLAME, 3 + random.nextInt(2), random);
                if (random.nextInt(4) == 0) {
                    TileEntityFluidPipe.setNeighboursToFire(world, nodePos);
                }
            }
            if (isLeaking && world.rand.nextInt(isBurning ? 3 : 7) == 0) {
                world.createExplosion(null,
                        nodePos.getX() + 0.5, nodePos.getY() + 0.5, nodePos.getZ() + 0.5,
                        1.0f + world.rand.nextFloat(), false);
            }
        }
    }

    @Override
    protected void onConnectionsUpdate() {
        super.onConnectionsUpdate();
        //monolithic net always contains exactly one kind of nodes, so this is always safe
        //int newTankCapacity = nodeData.throughput * getAllNodes().size();
        //fluidNetTank.updateTankCapacity(newTankCapacity);
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet1) {
        super.transferNodeData(transferredNodes, parentNet1);
        FluidPipeNet parentNet = (FluidPipeNet) parentNet1;
        NET_DATA.clear();
        parentNet.NET_DATA.clear();
        FluidStack parentFluid = parentNet.getContainedFluid();
        if (parentFluid != null && parentFluid.amount > 0) {
            if (parentNet.getAllNodes().isEmpty()) {
                //if this is merge of pipe nets, just add all fluid to our internal tank
                //use fillInternal to ignore throughput restrictions
                setContainingFluid(parentFluid);
            }
        }
    }

    @Override
    protected boolean areNodesCustomContactable(FluidPipeProperties first, FluidPipeProperties second, PipeNet<FluidPipeProperties> secondNodeNet) {
        FluidPipeNet fluidPipeNet = (FluidPipeNet) secondNodeNet;
        return super.areNodesCustomContactable(first, second, secondNodeNet) &&
                (secondNodeNet == null || getContainedFluid() == null || fluidPipeNet.getContainedFluid() == null ||
                        getContainedFluid().isFluidEqual(fluidPipeNet.getContainedFluid()));
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
        if (getContainedFluid() != null) {
            nbt.setTag("Fluid", getContainedFluid().writeToNBT(new NBTTagCompound()));
            nbt.setInteger("Timer", emptyTimer);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("Fluid")) {
            setContainingFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid")));
            this.emptyTimer = nbt.getInteger("Timer");
        }
    }

    @Override
    public void update() {
        if (emptyTimer > 0 && --emptyTimer == 0) {
            emptyTank();
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
