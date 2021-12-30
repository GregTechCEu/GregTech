package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.FluidFilterMode;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.PipeTankList;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperties> {

    public static final int FREQUENCY = 5;
    private static final Random random = new Random();
    private final EnumSet<EnumFacing> openConnections = EnumSet.noneOf(EnumFacing.class);
    private WeakReference<FluidPipeNet> currentPipeNet = new WeakReference<>(null);
    private PipeTankList pipeTankList;
    private FluidTank[] fluidTanks;
    private List<Pair<IFluidHandler, Predicate<FluidStack>>> neighbourCache = new ArrayList<>();

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
            PipeTankList tankList = getTankList();
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankList);
        }
        return super.getCapabilityInternal(capability, facing);
    }

    protected EnumSet<EnumFacing> getOpenFaces() {
        return openConnections;
    }

    public List<Pair<IFluidHandler, Predicate<FluidStack>>> getNeighbourCache() {
        return neighbourCache;
    }

    public List<Pair<IFluidHandler, Predicate<FluidStack>>> getNeighbourHandlers() {
        List<Pair<IFluidHandler, Predicate<FluidStack>>> handlers = new ArrayList<>();
        for (EnumFacing facing : getOpenFaces()) {
            if (!isConnectionOpenAny(facing)) continue;
            TileEntity tile = getWorld().getTileEntity(pos.offset(facing));
            if (tile == null) continue;
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
            if (handler != null) {
                ICoverable coverable = getCoverableImplementation();
                CoverBehavior cover = coverable.getCoverAtSide(facing);
                Predicate<FluidStack> filter = fluid -> true;
                if (cover instanceof CoverPump && ((CoverPump) cover).getPumpMode() == CoverPump.PumpMode.IMPORT && ((CoverPump) cover).blocksInput()) {
                    continue;
                } else if (cover instanceof CoverFluidFilter) {
                    CoverFluidFilter fluidFilter = (CoverFluidFilter) cover;
                    if (fluidFilter.getFilterMode() == FluidFilterMode.FILTER_DRAIN || fluidFilter.getFilterMode() == FluidFilterMode.FILTER_BOTH)
                        filter = fluidFilter::testFluidStack;
                }
                coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing.getOpposite());
                if (coverable != null) {
                    cover = coverable.getCoverAtSide(facing.getOpposite());
                    if (cover instanceof CoverPump && ((CoverPump) cover).getPumpMode() == CoverPump.PumpMode.EXPORT && ((CoverPump) cover).blocksInput()) {
                        continue;
                    }
                }
                handlers.add(Pair.of(handler, filter));
            }
        }
        neighbourCache = handlers;
        return handlers;
    }

    public int getCapacityPerTank() {
        return getNodeData().getThroughput() * 2 * FREQUENCY;
    }

    public void checkNeighbours() {
        openConnections.clear();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isConnectionOpen(AttachmentType.PIPE, facing)) {
                TileEntity tile = world.getTileEntity(pos.offset(facing));
                if (tile == null || tile instanceof TileEntityFluidPipe) continue;
                IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                if (handler != null) {
                    openConnections.add(facing);
                    if (!(this instanceof TileEntityFluidPipeTickable)) {
                        TileEntityFluidPipeTickable pipe = (TileEntityFluidPipeTickable) setSupportsTicking();
                        pipe.checkNeighbours();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void setConnectionBlocked(AttachmentType attachmentType, EnumFacing side, boolean blocked, boolean fromNeighbor) {
        int oldConnections = getOpenConnections();
        super.setConnectionBlocked(attachmentType, side, blocked, fromNeighbor);
        if (oldConnections != getOpenConnections()) {
            checkNeighbours();
            TileEntity te = world.getTileEntity(pos.offset(side));
            if (!blocked && attachmentType == AttachmentType.PIPE && te instanceof TileEntityFluidPipe) {
                FluidPipeNet net = getFluidPipeNet();
                for (FluidTank tank : getFluidTanks()) {
                    FluidStack fluid = tank.getFluid();
                    if (fluid != null) {
                        net.markDirty(fluid, pos);
                    }
                }
            }
        }
    }

    @Override
    public void transferDataFrom(IPipeTile<FluidPipeType, FluidPipeProperties> tileEntity) {
        super.transferDataFrom(tileEntity);
        this.fluidTanks = ((TileEntityFluidPipe) tileEntity).fluidTanks;
        pipeTankList = new PipeTankList(this, fluidTanks);
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0) return null;
        return getFluidTanks()[channel].getFluid();
    }

    public FluidStack findFluid(FluidStack stack) {
        return getContainedFluid(findChannel(stack));
    }

    private void createTanksList() {
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, fluidTanks);
    }

    public PipeTankList getTankList() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return pipeTankList;
    }

    public FluidTank[] getFluidTanks() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return fluidTanks;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidTanks().length];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidTanks[i].getFluid();
        }
        return fluids;
    }

    public int setFluidAuto(FluidStack stack, boolean fill) {
        return setContainingFluid(stack, findChannel(stack), fill);
    }

    public int setContainingFluid(FluidStack stack, int channel, boolean fill) {
        if (channel < 0)
            return stack == null ? 0 : stack.amount;
        if (stack == null || stack.amount <= 0) {
            getFluidTanks()[channel].setFluid(null);
            return 0;
        }
        FluidTank tank = getFluidTanks()[channel];
        FluidStack currentStack = tank.getFluid();
        if (currentStack == null || currentStack.amount <= 0) {
            checkAndDestroy(stack);
        } else if (fill) {
            int toFill = stack.amount;
            if (toFill + currentStack.amount > tank.getCapacity())
                toFill = tank.getCapacity() - currentStack.amount;
            currentStack.amount += toFill;
            return toFill;
        }
        stack.amount = Math.min(stack.amount, tank.getCapacity());
        tank.setFluid(stack);
        return stack.amount;
    }

    public void checkAndDestroy(FluidStack stack) {
        boolean burning = getNodeData().getMaxFluidTemperature() < stack.getFluid().getTemperature(stack);
        boolean leaking = !getNodeData().isGasProof() && stack.getFluid().isGaseous(stack);
        if (burning || leaking) {
            destroyPipe(burning, leaking);
        }
    }

    public boolean areTanksEmpty() {
        for (FluidStack fluidStack : getContainedFluids())
            if (fluidStack != null) {
                if (fluidStack.amount <= 0) {
                    setContainingFluid(null, findChannel(fluidStack), false);
                    continue;
                }
                return false;
            }
        return true;
    }

    /**
     * Finds a channel for the given fluid
     *
     * @param stack to find a channel fot
     * @return channel
     */
    public int findChannel(FluidStack stack) {
        if (getFluidTanks().length == 1) {
            FluidStack channelStack = getContainedFluid(0);
            return (channelStack == null || channelStack.amount <= 0 || channelStack.isFluidEqual(stack)) ? 0 : -1;
        }
        int emptyTank = -1;
        for (int i = fluidTanks.length - 1; i >= 0; i--) {
            FluidStack channelStack = getContainedFluid(i);
            if (channelStack == null || channelStack.amount <= 0)
                emptyTank = i;
            else if (channelStack.isFluidEqual(stack))
                return i;
        }
        return emptyTank;
    }

    public void destroyPipe(boolean isBurning, boolean isLeaking) {
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
            this.doExplosion(1.0f + GTValues.RNG.nextFloat());
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getFluidTanks().length; i++) {
            FluidStack stack1 = getContainedFluid(i);
            NBTTagCompound fluidTag = new NBTTagCompound();
            if (stack1 == null || stack1.amount <= 0)
                fluidTag.setBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.setTag("Fluids", list);
        NBTTagList faces = new NBTTagList();
        for (EnumFacing facing : openConnections) {
            faces.appendTag(new NBTTagByte((byte) facing.getIndex()));
        }
        nbt.setTag("Faces", faces);
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        createTanksList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if (!tag.getBoolean("isNull")) {
                fluidTanks[i].setFluid(FluidStack.loadFluidStackFromNBT(tag));
            }
        }
        NBTTagList faces = nbt.getTagList("Faces", Constants.NBT.TAG_BYTE);
        for (int i = 0; i < faces.tagCount(); i++) {
            openConnections.add(EnumFacing.byIndex(((NBTTagByte) faces.get(i)).getByte()));
        }
    }

    public FluidPipeNet getFluidPipeNet() {
        if(world == null || world.isRemote)
            return null;
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
