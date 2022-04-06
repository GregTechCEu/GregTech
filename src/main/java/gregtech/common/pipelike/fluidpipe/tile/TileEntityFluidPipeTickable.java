package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.fluids.MaterialFluid;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.fluidpipe.net.PipeTankList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.MutableTriple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable, IDataInfoProvider {

    public byte lastReceivedFrom = 0, oldLastReceivedFrom = 0;
    private PipeTankList pipeTankList;
    private final EnumMap<EnumFacing, PipeTankList> tankLists = new EnumMap<>(EnumFacing.class);
    private FluidTank[] fluidTanks;
    private long timer = 0L;
    private final int offset = GTValues.RNG.nextInt(20);

    public long getOffsetTimer() {
        return timer + offset;
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

    @Override
    public void update() {
        timer++;
        getCoverableImplementation().update();
        if (!world.isRemote && getOffsetTimer() % FREQUENCY == 0) {
            lastReceivedFrom &= 63;
            if (lastReceivedFrom == 63) {
                lastReceivedFrom = 0;
            }

            boolean shouldDistribute = (oldLastReceivedFrom == lastReceivedFrom);
            int tanks = getNodeData().getTanks();
            for (int i = 0, j = GTValues.RNG.nextInt(tanks); i < tanks; i++) {
                int index = (i + j) % tanks;
                FluidTank tank = getFluidTanks()[index];
                FluidStack fluid = tank.getFluid();
                if (fluid == null)
                    continue;
                if (fluid.amount <= 0) {
                    tank.setFluid(null);
                    continue;
                }

                if (shouldDistribute) {
                    distributeFluid(index, tank, fluid);
                    lastReceivedFrom = 0;
                }
            }
            oldLastReceivedFrom = lastReceivedFrom;
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    private void distributeFluid(int channel, FluidTank tank, FluidStack fluid) {
        // Tank, From, Amount to receive
        List<MutableTriple<IFluidHandler, IFluidHandler, Integer>> tanks = new ArrayList<>();
        int amount = fluid.amount;

        FluidStack maxFluid = fluid.copy();
        double availableCapacity = 0;

        for (byte side, i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
            // Get a list of tanks accepting fluids, and what side they're on
            side = (byte) ((i + j) % 6);
            EnumFacing facing = EnumFacing.VALUES[side];
            if (!isConnected(facing) || (lastReceivedFrom & (1 << side)) != 0)
                continue;
            EnumFacing oppositeSide = facing.getOpposite();

            IFluidHandler fluidHandler = getFluidHandlerAt(facing, oppositeSide);
            if (fluidHandler == null)
                continue;

            IFluidHandler pipeTank = tank;
            CoverBehavior cover = getCoverableImplementation().getCoverAtSide(facing);
            if (cover != null) {
                pipeTank = cover.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, pipeTank);
            }

            FluidStack drainable = pipeTank.drain(maxFluid, false);
            if (drainable == null || drainable.amount <= 0) {
                continue;
            }

            int filled = Math.min(fluidHandler.fill(maxFluid, false), drainable.amount);

            if (filled > 0) {
                tanks.add(MutableTriple.of(fluidHandler, pipeTank, filled));
                availableCapacity += filled;
            }
            maxFluid.amount = amount; // Because some mods do actually modify input fluid stack
        }

        if (availableCapacity <= 0)
            return;

        // How much of this fluid is available for distribution?
        final double maxAmount = Math.min(getCapacityPerTank() / 2, fluid.amount);

        // Now distribute
        for (MutableTriple<IFluidHandler, IFluidHandler, Integer> triple : tanks) {
            if (availableCapacity > maxAmount) {
                triple.setRight((int) Math.floor(triple.getRight() * maxAmount / availableCapacity)); // Distribute fluids based on percentage available space at destination
            }
            if (triple.getRight() == 0) {
                if (tank.getFluidAmount() <= 0) break; // If there is no more stored fluid, stop transferring to prevent dupes
                triple.setRight(1); // If the percent is not enough to give at least 1L, try to give 1L
            } else if (triple.getRight() < 0) {
                continue;
            }

            FluidStack toInsert = fluid.copy();
            toInsert.amount = triple.getRight();

            int inserted = triple.getLeft().fill(toInsert, true);
            if (inserted > 0) {
                triple.getMiddle().drain(inserted, true);
            }
        }
    }

    public void checkAndDestroy(@Nonnull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        boolean burning = getNodeData().getMaxFluidTemperature() < fluid.getTemperature(stack);
        boolean leaking = !getNodeData().isGasProof() && fluid.isGaseous(stack);
        boolean shattering = !getNodeData().isCryoProof() && fluid.getTemperature() < 120; // fluids less than 120K are cryogenic
        boolean corroding = false;
        boolean melting = false;
        if (fluid instanceof MaterialFluid) {
            MaterialFluid materialFluid = (MaterialFluid) fluid;
            corroding = !getNodeData().isAcidProof() && materialFluid.getFluidType().equals(FluidTypes.ACID);
            melting = !getNodeData().isPlasmaProof() && materialFluid.getFluidType().equals(FluidTypes.PLASMA);

            // carrying plasmas which are too hot when plasma proof does not burn pipes
            if (burning && getNodeData().isPlasmaProof() && materialFluid.getFluidType().equals(FluidTypes.PLASMA))
                burning = false;
        }

        if (burning || leaking || corroding || shattering || melting) {
            destroyPipe(stack, burning, leaking, corroding, shattering, melting);
        }
    }

    public void destroyPipe(FluidStack stack, boolean isBurning, boolean isLeaking, boolean isCorroding, boolean isShattering, boolean isMelting) {
        // prevent the sound from spamming when filled from anything not a pipe
        if (getOffsetTimer() % 10 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        if (isLeaking) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.SMOKE_NORMAL, 7 + GTValues.RNG.nextInt(2));

            // voids 10%
            stack.amount = Math.max(0, stack.amount * 9 / 10);

            // apply heat damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack), 2.0F, 10);
                }
            }

            // chance to do a small explosion
            if (GTValues.RNG.nextInt(isBurning ? 3 : 7) == 0) {
                this.doExplosion(1.0f + GTValues.RNG.nextFloat());
            }
        }

        if (isCorroding) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CRIT_MAGIC, 3 + GTValues.RNG.nextInt(2));

            // voids 25%
            stack.amount = Math.max(0, stack.amount * 3 / 4);

            // apply chemical damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(getPipePos()).grow(1));
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
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.FLAME, (isMelting ? 7 : 3) + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.amount = Math.max(0, stack.amount / 4);

            // 1/4 chance to burn everything around it
            if (GTValues.RNG.nextInt(4) == 0) {
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
            }

            // apply heat damage in area surrounding the pipe
            if (isMelting && getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack), 2.0F, 10);
                }
            }

            // 1/10 chance to void everything and burn the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        if (isShattering) {
            TileEntityFluidPipe.spawnParticles(world, pos, EnumFacing.UP, EnumParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.amount = Math.max(0, stack.amount / 4);

            // apply frost damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<EntityLivingBase> entities = getPipeWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(getPipePos()).grow(2));
                for (EntityLivingBase entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack), 2.0F, 10);
                }
            }

            // 1/10 chance to void everything and freeze the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
            }
        }
    }

    private IFluidHandler getFluidHandlerAt(EnumFacing facing, EnumFacing oppositeSide) {
        TileEntity tile = world.getTileEntity(pos.offset(facing));
        if (tile == null) {
            return null;
        }
        return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, oppositeSide);
    }

    public void receivedFrom(EnumFacing facing) {
        if (facing != null) {
            lastReceivedFrom |= (1 << facing.getIndex());
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
    }

    @Nonnull
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
                            new TextComponentTranslation(GTUtility.formatNumbers(fluids[i].amount)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(GTUtility.formatNumbers(this.getCapacityPerTank())).setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            new TextComponentTranslation(fluids[i].getFluid().getLocalizedName(fluids[i])).setStyle(new Style().setColor(TextFormatting.GOLD))
                    ));
                }
            }

            if (allTanksEmpty)
                list.add(new TextComponentTranslation("behavior.tricorder.tanks_empty"));
        }
        return list;
    }
}
