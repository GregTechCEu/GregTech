package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.edge.NetFlowEdge;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.TaskScheduler;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidNetHandler;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperties, NetFlowEdge> {

    private FluidTank[] fluidTanks = null;

    private final EnumMap<EnumFacing, FluidNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private FluidNetHandler defaultHandler;
    // the FluidNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IFluidHandler clientCapability = new FluidTank(0);
    private final int offset = GTValues.RNG.nextInt(20);
    private long nextSoundTime = 0;
    private long nextDamageTime = 0;

    public long getOffsetTimer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() + offset;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (world.isRemote)
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(clientCapability);

            if (handlers.size() == 0)
                initHandlers();
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    private void initHandlers() {
        WorldFluidPipeNet net = WorldFluidPipeNet.getWorldPipeNet(getPipeWorld());
        for (EnumFacing facing : EnumFacing.values()) {
            handlers.put(facing, new FluidNetHandler(net, this, facing));
        }
        defaultHandler = new FluidNetHandler(net, this, null);
    }

    @Override
    public void transferDataFrom(IPipeTile<FluidPipeType, FluidPipeProperties, NetFlowEdge> tileEntity) {
        super.transferDataFrom(tileEntity);
        TileEntityFluidPipe fluidPipe = (TileEntityFluidPipe) tileEntity;
        // take handlers from old pipe
        if (!fluidPipe.handlers.isEmpty()) {
            this.handlers.clear();
            for (FluidNetHandler handler : fluidPipe.handlers.values()) {
                handler.updatePipe(this);
                this.handlers.put(handler.getFacing(), handler);
            }
        }
        if (fluidPipe.defaultHandler != null) {
            fluidPipe.defaultHandler.updatePipe(this);
            this.defaultHandler = fluidPipe.defaultHandler;
        }
    }

    public void playLossSound() {
        long timer = getOffsetTimer();
        if (timer >= this.nextSoundTime) {
            getPipeWorld().playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.nextSoundTime = timer + 20;
        }
    }

    public void dealAreaDamage(int size, Consumer<EntityLivingBase> damageFunction) {
        long timer = getOffsetTimer();
        if (timer >= this.nextDamageTime) {
            List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(getPipePos()).grow(size));
            entities.forEach(damageFunction);
            this.nextDamageTime = timer + 20;
        }
    }

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

    public static void spawnParticles(World worldIn, BlockPos pos, EnumFacing direction, EnumParticleTypes particleType,
                                      int particleCount) {
        if (worldIn instanceof WorldServer) {
            ((WorldServer) worldIn).spawnParticle(particleType,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    particleCount,
                    direction.getXOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getYOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getZOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    0.1);
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (fluidTanks == null) return nbt;
        NBTTagList list = new NBTTagList();
        for (FluidTank fluidTank : fluidTanks) {
            FluidStack stack1 = fluidTank.getFluid();
            NBTTagCompound fluidTag = new NBTTagCompound();
            if (stack1 == null || stack1.amount <= 0)
                fluidTag.setBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.setTag("Fluids", list);
        return nbt;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!nbt.hasKey("Fluids")) return;
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if (!tag.getBoolean("isNull")) {
                FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
                if (stack == null) continue;
                fluidTanks[i].setFluid(stack);
            }
        }
        TaskScheduler.scheduleTask(world, this::pushFluids);
    }

    private boolean pushFluids() {
        boolean remaining = false;
        for (FluidTank tank : fluidTanks) {
            if (tank == null || tank.getFluidAmount() == 0) continue;
            assert tank.getFluid() != null;
            int fill = defaultHandler.fill(tank.getFluid(), true);
            if (fill <= tank.getFluidAmount()) remaining = true;
            tank.getFluid().amount -= fill;
        }
        if (!remaining) fluidTanks = null;
        return remaining;
    }
}
