package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidChannel;
import gregtech.common.pipelike.fluidpipe.net.PipeTankList;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperties>
                                 implements IDataInfoProvider {

    private PipeTankList pipeTankList;
    private final EnumMap<EnumFacing, PipeTankList> tankLists = new EnumMap<>(EnumFacing.class);
    private FluidTank[] fluidTanks;
    private final int offset = GTValues.RNG.nextInt(20);

    public long getOffsetTimer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() + offset;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            PipeTankList tankList = getTankList(facing);
            if (tankList == null)
                return null;
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankList);
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void checkAndDestroy(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        FluidPipeProperties prop = getNodeData();

        boolean burning = prop.getMaxFluidTemperature() < fluid.getTemperature(stack);
        boolean leaking = !prop.isGasProof() && fluid.isGaseous(stack);
        boolean shattering = !prop.isCryoProof() && fluid.getTemperature() < FluidConstants.CRYOGENIC_FLUID_THRESHOLD;
        boolean corroding = false;
        boolean melting = false;

        if (fluid instanceof AttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (!prop.canContain(state)) {
                leaking = state == FluidState.GAS;
                melting = state == FluidState.PLASMA;
            }

            // carrying plasmas which are too hot when plasma proof does not burn pipes
            if (burning && state == FluidState.PLASMA && prop.canContain(FluidState.PLASMA)) {
                burning = false;
            }

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!prop.canContain(attribute)) {
                    // corrodes if the pipe can't handle the attribute, even if it's not an acid
                    corroding = true;
                }
            }
        }

        if (burning || leaking || corroding || shattering || melting) {
            destroyPipe(stack, burning, leaking, corroding, shattering, melting);
        }
    }

    public void destroyPipe(FluidStack stack, boolean isBurning, boolean isLeaking, boolean isCorroding,
                            boolean isShattering, boolean isMelting) {
        // prevent the sound from spamming when filled from anything not a pipe
        if (getOffsetTimer() % 10 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        if (isLeaking) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.SMOKE_NORMAL,
                    7 + GTValues.RNG.nextInt(2));

            // voids 10%
            stack.amount = Math.max(0, stack.amount * 9 / 10);

            // apply heat damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack),
                            2.0F, 10);
                }
            }

            // chance to do a small explosion
            if (GTValues.RNG.nextInt(isBurning ? 3 : 7) == 0) {
                this.doExplosion(1.0f + GTValues.RNG.nextFloat());
            }
        }

        if (isCorroding) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CRIT_MAGIC,
                    3 + GTValues.RNG.nextInt(2));

            // voids 25%
            stack.amount = Math.max(0, stack.amount * 3 / 4);

            // apply chemical damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(1));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyChemicalDamage(entityLivingBase, 2);
                }
            }

            // 1/10 chance to void everything and destroy the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
            }
        }

        if (isBurning || isMelting) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.FLAME,
                    (isMelting ? 7 : 3) + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.amount = Math.max(0, stack.amount / 4);

            // 1/4 chance to burn everything around it
            if (GTValues.RNG.nextInt(4) == 0) {
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
            }

            // apply heat damage in area surrounding the pipe
            if (isMelting && getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack),
                            2.0F, 10);
                }
            }

            // 1/10 chance to void everything and burn the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        if (isShattering) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CLOUD,
                    3 + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.amount = Math.max(0, stack.amount / 4);

            // apply frost damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack),
                            2.0F, 10);
                }
            }

            // 1/10 chance to void everything and freeze the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
            }
        }
    }

    public void receivedFrom(Fluid fluid, EnumFacing facing) {
        // on fluid received, add us as a source to the proper channel
        FluidChannel channel = FluidChannel.getChannelFromGroup(fluid, this.getNode().getGroupSafe());
        channel.addSource(this.getNode());
        if (facing != null) {
            channel.addReceiveSide(this.getNode(), facing);
        }
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0 || channel >= getFluidTanks().length) return null;
        return getFluidTanks()[channel].getFluid();
    }

    private void createTanksList() {
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, null, fluidTanks);
        for (EnumFacing facing : EnumFacing.VALUES) {
            tankLists.put(facing, new PipeTankList(this, facing, fluidTanks));
        }
    }

    public PipeTankList getTankList() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return pipeTankList;
    }

    public PipeTankList getTankList(EnumFacing facing) {
        if (tankLists.isEmpty() || fluidTanks == null) {
            createTanksList();
        }
        return tankLists.getOrDefault(facing, pipeTankList);
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
        return nbt;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        createTanksList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if (!tag.getBoolean("isNull")) {
                FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
                if (stack == null) continue;
                fluidTanks[i].setFluid(stack);
                // the best part is that this naturally gives us backwards compatibility.
                FluidChannel channel = FluidChannel.getChannelFromGroup(stack.getFluid(),
                        this.getNode().getGroupSafe());
                channel.addSource(this.getNode());
            }
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        FluidStack[] fluids = this.getContainedFluids();
        if (fluids != null) {
            boolean allTanksEmpty = true;
            for (int i = 0; i < fluids.length; i++) {
                if (fluids[i] != null) {
                    if (fluids[i].getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation("behavior.tricorder.tank", i,
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(fluids[i].amount))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.getCapacityPerTank()))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            new TextComponentTranslation(fluids[i].getFluid().getLocalizedName(fluids[i]))
                                    .setStyle(new Style().setColor(TextFormatting.GOLD))));
                }
            }

            if (allTanksEmpty)
                list.add(new TextComponentTranslation("behavior.tricorder.tanks_empty"));
        }
        return list;
    }
}
