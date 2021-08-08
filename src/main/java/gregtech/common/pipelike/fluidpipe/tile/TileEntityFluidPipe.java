package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidNetHandler;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Random;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperties> {

    private WeakReference<FluidPipeNet> currentPipeNet = new WeakReference<>(null);
    private static final Random random = new Random();

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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("Ticking", this instanceof TileEntityFluidPipeTickable);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.getBoolean("Ticking") && !(this instanceof TileEntityFluidPipeTickable)) {
            TileEntityFluidPipeTickable tickable = (TileEntityFluidPipeTickable) setSupportsTicking();
            tickable.readFromNBT(nbt);
        }
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
