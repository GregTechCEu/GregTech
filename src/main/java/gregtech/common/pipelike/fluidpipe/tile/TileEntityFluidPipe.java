package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.GTUtility;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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

    protected EnumSet<EnumFacing> getOpenFaces() {
        return openConnections;
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
    public void transferDataFrom(IPipeTile<FluidPipeType, FluidPipeProperties> tileEntity) {
        super.transferDataFrom(tileEntity);
        //this.fluidTanks = ((TileEntityFluidPipe) tileEntity).fluidTanks;
        //pipeTankList = new PipeTankList(this, fluidTanks);
    }

    public void checkAndDestroy(FluidStack stack) {
        boolean burning = getNodeData().getMaxFluidTemperature() < stack.getFluid().getTemperature(stack);
        boolean leaking = !getNodeData().isGasProof() && stack.getFluid().isGaseous(stack);
        if (burning || leaking) {
            destroyPipe(burning, leaking);
        }
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
