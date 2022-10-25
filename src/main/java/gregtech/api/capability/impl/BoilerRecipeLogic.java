package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.BOILER_HEAT;
import static gregtech.api.capability.GregtechDataCodes.BOILER_LAST_TICK_STEAM;

public class BoilerRecipeLogic extends AbstractRecipeLogic {

    private static final long STEAM_PER_WATER = 160;

    private static final int FLUID_DRAIN_MULTIPLIER = 100;
    private static final int FLUID_BURNTIME_TO_EU = 800 / FLUID_DRAIN_MULTIPLIER;

    private int currentHeat;
    private int lastTickSteamOutput;
    private int excessWater, excessFuel, excessProjectedEU;

    public BoilerRecipeLogic(MetaTileEntityLargeBoiler tileEntity) {
        super(tileEntity, null);
        this.fluidOutputs = Collections.emptyList();
        this.itemOutputs = NonNullList.create();
    }

    @Override
    public void update() {
        if ((!isActive() || !canProgressRecipe() || !isWorkingEnabled()) && currentHeat > 0) {
            setHeat(currentHeat - 1);
            setLastTickSteam(0);
        }
        super.update();
    }

    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !(metaTileEntity instanceof IMultiblockController && ((IMultiblockController) metaTileEntity).isStructureObstructed());
    }

    @Override
    protected void trySearchNewRecipe() {
        MetaTileEntityLargeBoiler boiler = (MetaTileEntityLargeBoiler) metaTileEntity;
        if (ConfigHolder.machines.enableMaintenance && boiler.hasMaintenanceMechanics() && boiler.getNumMaintenanceProblems() > 5) {
            return;
        }

        // can optimize with an override of checkPreviousRecipe() and a check here

        IMultipleTankHandler importFluids = boiler.getImportFluids();
        List<ItemStack> dummyList = NonNullList.create();
        boolean didStartRecipe = false;

        for (IFluidTank fluidTank : importFluids.getFluidTanks()) {
            FluidStack fuelStack = fluidTank.drain(Integer.MAX_VALUE, false);
            if (fuelStack == null || ModHandler.isWater(fuelStack)) continue;

            Recipe dieselRecipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipe(
                    GTValues.V[GTValues.MAX], dummyList, Collections.singletonList(fuelStack), Integer.MAX_VALUE);
            // run only if it can apply a certain amount of "parallel", this is to mitigate int division
            if (dieselRecipe != null && fuelStack.amount >= dieselRecipe.getFluidInputs().get(0).getAmount() * FLUID_DRAIN_MULTIPLIER) {
                fluidTank.drain(dieselRecipe.getFluidInputs().get(0).getAmount() * FLUID_DRAIN_MULTIPLIER, true);
                // divide by 2, as it is half burntime for combustion
                setMaxProgress(adjustBurnTimeForThrottle(Math.max(1, boiler.boilerType.runtimeBoost((Math.abs(dieselRecipe.getEUt()) * dieselRecipe.getDuration()) / FLUID_BURNTIME_TO_EU / 2))));
                didStartRecipe = true;
                break;
            }

            Recipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(
                    GTValues.V[GTValues.MAX], dummyList, Collections.singletonList(fuelStack), Integer.MAX_VALUE);
            // run only if it can apply a certain amount of "parallel", this is to mitigate int division
            if (denseFuelRecipe != null && fuelStack.amount >= denseFuelRecipe.getFluidInputs().get(0).getAmount() * FLUID_DRAIN_MULTIPLIER) {
                fluidTank.drain(denseFuelRecipe.getFluidInputs().get(0).getAmount() * FLUID_DRAIN_MULTIPLIER, true);
                // multiply by 2, as it is 2x burntime for semi-fluid
                setMaxProgress(adjustBurnTimeForThrottle(Math.max(1, boiler.boilerType.runtimeBoost((Math.abs(denseFuelRecipe.getEUt()) * denseFuelRecipe.getDuration() / FLUID_BURNTIME_TO_EU * 2)))));
                didStartRecipe = true;
                break;
            }
        }

        if (!didStartRecipe) {
            IItemHandlerModifiable importItems = boiler.getImportItems();
            for (int i = 0; i < importItems.getSlots(); i++) {
                ItemStack stack = importItems.getStackInSlot(i);
                int fuelBurnTime = (int) Math.ceil(ModHandler.getFuelValue(stack));
                if (fuelBurnTime / 80 > 0) { // try to ensure this fuel can burn for at least 1 tick
                    if (FluidUtil.getFluidHandler(stack) != null) continue;
                    this.excessFuel += fuelBurnTime % 80;
                    int excessProgress = this.excessFuel / 80;
                    this.excessFuel %= 80;
                    setMaxProgress(excessProgress + adjustBurnTimeForThrottle(boiler.boilerType.runtimeBoost(fuelBurnTime / 80)));
                    stack.shrink(1);
                    didStartRecipe = true;
                    break;
                }
            }
        }
        if (didStartRecipe) {
            this.progressTime = 1;
            this.recipeEUt = adjustEUtForThrottle(boiler.boilerType.steamPerTick());
            if (wasActiveAndNeedsUpdate) {
                wasActiveAndNeedsUpdate = false;
            } else {
                setActive(true);
            }
        }
        metaTileEntity.getNotifiedItemInputList().clear();
        metaTileEntity.getNotifiedFluidInputList().clear();
    }

    @Override
    protected void updateRecipeProgress() {
        if (canRecipeProgress) {
            int generatedSteam = this.recipeEUt * getMaximumHeatFromMaintenance() / getMaximumHeat();
            if (generatedSteam > 0) {
                long amount = (generatedSteam + STEAM_PER_WATER) / STEAM_PER_WATER;
                excessWater += amount * STEAM_PER_WATER - generatedSteam;
                amount -= excessWater / STEAM_PER_WATER;
                excessWater %= STEAM_PER_WATER;

                FluidStack drainedWater = ModHandler.getBoilerFluidFromContainer(getInputTank(), (int) amount, true);
                if (amount != 0 && (drainedWater == null || drainedWater.amount < amount)) {
                    getMetaTileEntity().explodeMultiblock();
                } else {
                    setLastTickSteam(generatedSteam);
                    getOutputTank().fill(ModHandler.getSteam(generatedSteam), true);
                }
            }
            if (currentHeat < getMaximumHeat()) {
                setHeat(currentHeat + 1);
            }

            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        }
    }

    private int getMaximumHeatFromMaintenance() {
        if (ConfigHolder.machines.enableMaintenance) {
            return (int) Math.min(currentHeat, (1 - 0.1 * getMetaTileEntity().getNumMaintenanceProblems()) * getMaximumHeat());
        } else return currentHeat;
    }

    private int adjustEUtForThrottle(int rawEUt) {
        int throttle = ((MetaTileEntityLargeBoiler) metaTileEntity).getThrottle();
        return Math.max(25, (int) (rawEUt * (throttle / 100.0)));
    }

    private int adjustBurnTimeForThrottle(int rawBurnTime) {
        MetaTileEntityLargeBoiler boiler = (MetaTileEntityLargeBoiler) metaTileEntity;
        int EUt = boiler.boilerType.steamPerTick();
        int adjustedEUt = adjustEUtForThrottle(EUt);
        int adjustedBurnTime = rawBurnTime * EUt / adjustedEUt;
        this.excessProjectedEU += EUt * rawBurnTime - adjustedEUt * adjustedBurnTime;
        adjustedBurnTime += this.excessProjectedEU / adjustedEUt;
        this.excessProjectedEU %= adjustedEUt;
        return adjustedBurnTime;
    }

    private int getMaximumHeat() {
        return ((MetaTileEntityLargeBoiler) metaTileEntity).boilerType.getTicksToBoiling();
    }

    public int getHeatScaled() {
        return (int) Math.round(currentHeat / (1.0 * getMaximumHeat()) * 100);
    }

    public void setHeat(int heat) {
        if (heat != this.currentHeat && !metaTileEntity.getWorld().isRemote) {
            writeCustomData(BOILER_HEAT, b -> b.writeVarInt(heat));
        }
        this.currentHeat = heat;
    }

    public int getLastTickSteam() {
        return lastTickSteamOutput;
    }

    public void setLastTickSteam(int lastTickSteamOutput) {
        if (lastTickSteamOutput != this.lastTickSteamOutput && !metaTileEntity.getWorld().isRemote) {
            writeCustomData(BOILER_LAST_TICK_STEAM, b -> b.writeVarInt(lastTickSteamOutput));
        }
        this.lastTickSteamOutput = lastTickSteamOutput;
    }

    public void invalidate() {
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        setActive(false);
        setLastTickSteam(0);
    }

    @Override
    protected void completeRecipe() {
        progressTime = 0;
        setMaxProgress(0);
        recipeEUt = 0;
        wasActiveAndNeedsUpdate = true;
    }

    @Override
    public MetaTileEntityLargeBoiler getMetaTileEntity() {
        return (MetaTileEntityLargeBoiler) super.getMetaTileEntity();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("Heat", currentHeat);
        compound.setInteger("ExcessFuel", excessFuel);
        compound.setInteger("ExcessWater", excessWater);
        compound.setInteger("ExcessProjectedEU", excessProjectedEU);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentHeat = compound.getInteger("Heat");
        this.excessFuel = compound.getInteger("ExcessFuel");
        this.excessWater = compound.getInteger("ExcessWater");
        this.excessProjectedEU = compound.getInteger("ExcessProjectedEU");
    }

    @Override
    public void writeInitialData(@Nonnull PacketBuffer buf) {
        super.writeInitialData(buf);
        buf.writeVarInt(currentHeat);
        buf.writeVarInt(lastTickSteamOutput);
    }

    @Override
    public void receiveInitialData(@Nonnull PacketBuffer buf) {
        super.receiveInitialData(buf);
        this.currentHeat = buf.readVarInt();
        this.lastTickSteamOutput = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == BOILER_HEAT) {
            this.currentHeat = buf.readVarInt();
        } else if (dataId == BOILER_LAST_TICK_STEAM) {
            this.lastTickSteamOutput = buf.readVarInt();
        }
    }

    // Required overrides to use RecipeLogic, but all of them are redirected by the above overrides.

    @Override
    protected long getEnergyInputPerSecond() {
        GTLog.logger.error("Large Boiler called getEnergyInputPerSecond(), this should not be possible!");
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        GTLog.logger.error("Large Boiler called getEnergyStored(), this should not be possible!");
        return 0;
    }

    @Override
    protected long getEnergyCapacity() {
        GTLog.logger.error("Large Boiler called getEnergyCapacity(), this should not be possible!");
        return 0;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        GTLog.logger.error("Large Boiler called drawEnergy(), this should not be possible!");
        return false;
    }

    @Override
    protected long getMaxVoltage() {
        GTLog.logger.error("Large Boiler called getMaxVoltage(), this should not be possible!");
        return 0;
    }
}
