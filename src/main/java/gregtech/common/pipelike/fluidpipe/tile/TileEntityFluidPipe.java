package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperties> {

    public static final int FREQUENCY = 5;
    private WeakReference<FluidPipeNet> currentPipeNet = new WeakReference<>(null);

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    public int getCapacityPerTank() {
        return getNodeData().getThroughput() * 20;
    }

    public void checkAndDestroy(FluidStack stack) {
        boolean burning = getNodeData().getMaxFluidTemperature() < stack.getFluid().getTemperature(stack);
        boolean leaking = !getNodeData().isGasProof() && stack.getFluid().isGaseous(stack);
        if (burning || leaking) {
            destroyPipe(burning, leaking);
        }
    }

    public void destroyPipe(boolean isBurning, boolean isLeaking) {
        if (isBurning) {
            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP,
                    EnumParticleTypes.FLAME, 3 + GTValues.RNG.nextInt(2));
            if (GTValues.RNG.nextInt(4) == 0)
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
        } else
            world.setBlockToAir(pos);
        if (isLeaking && world.rand.nextInt(isBurning ? 3 : 7) == 0) {
            this.doExplosion(1.0f + GTValues.RNG.nextFloat());
        }
    }

    public FluidPipeNet getFluidPipeNet() {
        if (world == null || world.isRemote)
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
            if (!GTValues.RNG.nextBoolean()) continue;
            BlockPos blockPos = selfPos.offset(side);
            IBlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock().isAir(blockState, world, blockPos) ||
                    blockState.getBlock().isFlammable(world, blockPos, side.getOpposite())) {
                world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());
            }
        }
    }

    public static void spawnParticles(World worldIn, BlockPos pos, EnumFacing direction, EnumParticleTypes particleType, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            worldIn.spawnParticle(particleType,
                    pos.getX() + 0.5 - direction.getXOffset() / 1.8,
                    pos.getY() + 0.5 - direction.getYOffset() / 1.8,
                    pos.getZ() + 0.5 - direction.getZOffset() / 1.8,
                    direction.getXOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getYOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getZOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1);
        }
    }
}
