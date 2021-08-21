package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.PerTickIntCounter;
import gregtech.api.util.TickingObjectHolder;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidNetHandler;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Random;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperties> {

    private WeakReference<FluidPipeNet> currentPipeNet = new WeakReference<>(null);
    private static final Random random = new Random();
    private TickingObjectHolder<FluidStack>[] fluidHolders;
    private final PerTickIntCounter transferredFluids = new PerTickIntCounter(0);
    private int currentChannel = -1;

    public TileEntityFluidPipe() {
    }

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            FluidPipeNet net = getFluidPipeNet();
            if (net == null) return null;
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidNetHandler(net, this, facing));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void transferFluid(int amount) {
        transferredFluids.increment(getWorld(), amount);
    }

    public int getTransferredFluids() {
        return transferredFluids.get(getWorld());
    }

    public int getCurrentChannel() {
        return currentChannel;
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0) return null;
        return getFluidHolders()[channel].getNullable(getWorld());
    }

    protected TickingObjectHolder<FluidStack>[] getFluidHolders() {
        if(fluidHolders == null) {
            this.fluidHolders = new TickingObjectHolder[getNodeData().tanks];
            for(int i = 0; i < fluidHolders.length; i++) {
                fluidHolders[i] = new TickingObjectHolder<>(null, 20);
            }
        }
        return fluidHolders;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidHolders().length];
        for(int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidHolders[i].getNullable(getWorld());
        }
        return fluids;
    }

    public void setContainingFluid(FluidStack stack, int channel) {
        if (channel < 0) return;
        getFluidHolders()[channel].reset(stack);
        this.currentChannel = -1;
    }

    private void emptyTank(int channel) {
        if (channel < 0) return;
        this.getContainedFluids()[channel] = null;
    }

    public boolean areTanksEmpty() {
        for (FluidStack fluidStack : getContainedFluids())
            if (fluidStack != null)
                return false;
        return true;
    }

    public boolean findAndSetChannel(FluidStack stack) {
        int c = findChannel(stack);
        this.currentChannel = c;
        return c >= 0 && c < fluidHolders.length;
    }

    /**
     * Finds a channel for the given fluid
     *
     * @param stack to find a channel fot
     * @return channel
     */
    public int findChannel(FluidStack stack) {
        if (getFluidHolders().length == 1) {
            FluidStack channelStack = getContainedFluid(0);
            return (channelStack == null || /*channelStack.amount <= 0 || */channelStack.isFluidEqual(stack)) ? 0 : -1;
        }
        int emptyTank = -1;
        for (int i = fluidHolders.length - 1; i >= 0; i--) {
            FluidStack channelStack = getContainedFluid(i);
            if (channelStack == null/* || channelStack.amount <= 0*/)
                emptyTank = i;
            else if (channelStack.isFluidEqual(stack))
                return i;
        }
        return emptyTank;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getFluidHolders().length; i++) {
            FluidStack stack1 = getContainedFluid(i);
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setLong("Timer", fluidHolders[i].getRemainingTime(getWorld()));
            if (stack1 == null)
                fluidTag.setBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.setTag("Fluids", list);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        //fluids = new FluidStack[list.tagCount()];
        fluidHolders = new TickingObjectHolder[list.tagCount()];
        for(int i = 0; i < fluidHolders.length; i++) {
            fluidHolders[i] = new TickingObjectHolder<>(null, 20);
        }
        //emptyTimer = new int[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            //emptyTimer[i] = tag.getInteger("Timer");
            if (!tag.getBoolean("isNull")) {
                fluidHolders[i].reset(FluidStack.loadFluidStackFromNBT(tag), tag.getInteger("Timer"));
            }
        }
    }

    public FluidPipeNet getFluidPipeNet() {
        FluidPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() &&
                currentPipeNet.containsNode(getPipePos()))
            return currentPipeNet; //if current net is valid and does contain position, return it
        WorldFluidPipeNet worldFluidPipeNet = (WorldFluidPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        currentPipeNet = worldFluidPipeNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    public static void setNeighboursToFire(World world, BlockPos selfPos) {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!random.nextBoolean()) continue;
            BlockPos blockPos = selfPos.offset(side);
            IBlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock().isAir(blockState, world, blockPos) ||
                    blockState.getBlock().isFlammable(world, blockPos, side.getOpposite())) {
                world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());
            }
        }
    }

    public static void spawnParticles(World worldIn, BlockPos pos, EnumFacing direction, EnumParticleTypes particleType, int particleCount, Random rand) {
        for (int i = 0; i < particleCount; i++) {
            worldIn.spawnParticle(particleType,
                    pos.getX() + 0.5 - direction.getXOffset() / 1.8,
                    pos.getY() + 0.5 - direction.getYOffset() / 1.8,
                    pos.getZ() + 0.5 - direction.getZOffset() / 1.8,
                    direction.getXOffset() * 0.2 + rand.nextDouble() * 0.1,
                    direction.getYOffset() * 0.2 + rand.nextDouble() * 0.1,
                    direction.getZOffset() * 0.2 + rand.nextDouble() * 0.1);
        }
    }
}
