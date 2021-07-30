package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.MonolithicPipeNet;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.Map;
import java.util.Random;

public class FluidPipeNet extends MonolithicPipeNet<FluidPipeProperties> {

    private final FluidNetTank fluidNetTank = new FluidNetTank(this);

    public FluidPipeNet(WorldPipeNet<FluidPipeProperties, FluidPipeNet> world) {
        super(world);
    }

    public FluidTank getFluidNetTank() {
        return fluidNetTank;
    }

    public int getMaxThroughput() {
        if (fluidNetTank.getCapacity() == 0) {
            return 0;
        }
        return nodeData.throughput;
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
        int newTankCapacity = nodeData.throughput * getAllNodes().size();
        fluidNetTank.updateTankCapacity(newTankCapacity);
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<FluidPipeProperties>> transferredNodes, PipeNet<FluidPipeProperties> parentNet1) {
        super.transferNodeData(transferredNodes, parentNet1);
        FluidPipeNet parentNet = (FluidPipeNet) parentNet1;
        FluidStack parentFluid = parentNet.getFluidNetTank().getFluid();
        if (parentFluid != null && parentFluid.amount > 0) {
            if (parentNet.getAllNodes().isEmpty()) {
                //if this is merge of pipe nets, just add all fluid to our internal tank
                //use fillInternal to ignore throughput restrictions
                getFluidNetTank().fillInternal(parentFluid, true);
            } else {
                //otherwise, it is donating of some nodes to our net in result of split
                //so, we should establish equal amount of fluid in networks
                int firstNetCapacity = getAllNodes().size() * getNodeData().throughput;
                int secondNetCapacity = parentNet.getAllNodes().size() * parentNet.getNodeData().throughput;
                int totalFluidAmount = getFluidNetTank().getFluidAmount() + parentFluid.amount;
                int fluidAmount1 = totalFluidAmount * firstNetCapacity / (firstNetCapacity + secondNetCapacity);
                int fluidAmount2 = totalFluidAmount - fluidAmount1;

                if (fluidAmount1 > 0) {
                    FluidStack fluidStack1 = parentFluid.copy();
                    fluidStack1.amount = fluidAmount1;
                    fluidNetTank.setFluid(fluidStack1);
                } else fluidNetTank.setFluid(null);

                if (fluidAmount2 > 0) {
                    FluidStack fluidStack2 = parentFluid.copy();
                    fluidStack2.amount = fluidAmount2;
                    parentNet.getFluidNetTank().setFluid(fluidStack2);
                } else parentNet.getFluidNetTank().setFluid(null);
            }
        }
    }

    @Override
    protected boolean areNodesCustomContactable(FluidPipeProperties first, FluidPipeProperties second, PipeNet<FluidPipeProperties> secondNodeNet) {
        FluidPipeNet fluidPipeNet = (FluidPipeNet) secondNodeNet;
        return super.areNodesCustomContactable(first, second, secondNodeNet) &&
                (secondNodeNet == null || getFluidNetTank().getFluid() == null || fluidPipeNet.getFluidNetTank().getFluid() == null ||
                        getFluidNetTank().getFluid().isFluidEqual(fluidPipeNet.getFluidNetTank().getFluid()));
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
        nbt.setTag("FluidTankNet", this.fluidNetTank.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("FluidTankNet"))
            this.fluidNetTank.readFromNBT(nbt.getCompoundTag("FluidTankNet"));
    }

}
