package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.ModHandler;
import gregtech.api.util.GTLog;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Collections;

// TODO Move to common

public class BoilerRecipeLogic extends AbstractRecipeLogic {

    private static final long STEAM_PER_WATER = 640;

    private int currentHeat;
    private int excessWater, excessFuel, excessProjectedEU;

    public BoilerRecipeLogic(MetaTileEntityLargeBoiler tileEntity) {
        super(tileEntity, null);
    }

    @Override
    public void update() {
        if (!isActive() && currentHeat > 0) { // might need to call on controller
            currentHeat--;
            writeCustomData(700, buf -> buf.writeVarInt(currentHeat));
        }
        super.update();
    }

    @Override
    protected void trySearchNewRecipe() {
        MetaTileEntityLargeBoiler boiler = (MetaTileEntityLargeBoiler) metaTileEntity;
        if (boiler.hasMaintenanceMechanics() && boiler.getMaintenanceProblems() > 5) {
            return;
        }

        IMultipleTankHandler importFluids = boiler.getInputTank();
        boolean didStartRecipe = false;

        // todo can optimize with an override of checkPreviousRecipe() and a check here
        // (might need to?)

        // TODO Do these after fuel recipes are done differently
/*
        for (IFluidTank fluidTank : importFluids.getFluidTanks()) {
            FluidStack fuelStack = fluidTank.drain(Integer.MAX_VALUE, false);
            if (fuelStack == null || ModHandler.isWater(fuelStack)) continue;

            FuelRecipe dieselRecipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipe(GTValues.V[GTValues.MAX], fuelStack);
            if (dieselRecipe != null) {
                // should exit here
                //int duration = dieselRecipe.getDuration();
                //Recipe recipe = new Recipe(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), )
            }

            FuelRecipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(GTValues.V[GTValues.MAX], fuelStack);
            if (denseFuelRecipe != null) {
                // should exit here
            }
        }
*/
        if (!didStartRecipe) {
            IItemHandlerModifiable importItems = boiler.getImportItems();
            for (int i = 0; i < importItems.getSlots(); i++) {
                ItemStack stack = importItems.getStackInSlot(i);
                int fuelBurnTime = (int) Math.ceil(ModHandler.getFuelValue(stack));
                if (fuelBurnTime / 80 > 0) {
                    this.excessFuel += fuelBurnTime % 80;
                    int excessProgress = this.excessFuel / 80;
                    setMaxProgress(excessProgress + adjustBurnTimeForConfig(boiler.boilerType.runtimeBoost(fuelBurnTime / 80)));
                    this.progressTime = 1;
                    this.recipeEUt = adjustEUtForConfig(boiler.boilerType.steamPerTick());

                    // avoid some NPEs
                    this.fluidOutputs = Collections.emptyList();
                    this.itemOutputs = NonNullList.create();

                    stack.shrink(1);

                    if (wasActiveAndNeedsUpdate) {
                        wasActiveAndNeedsUpdate = false;
                    } else {
                        setActive(true);
                    }
                    break;
                }
            }
        }
        metaTileEntity.getNotifiedItemInputList().clear();
        metaTileEntity.getNotifiedFluidInputList().clear();
    }

    @Override
    protected void updateRecipeProgress() {
        // todo figure out how maintenance fits in
        if (currentHeat >= getMaximumHeat()) {
            int generatedSteam = (int) (this.recipeEUt * 2 * this.currentHeat / 10000L);
            if (generatedSteam > 0) {
                long amount = (generatedSteam + STEAM_PER_WATER) * STEAM_PER_WATER;
                excessWater += amount * STEAM_PER_WATER - generatedSteam;
                amount -= excessWater / STEAM_PER_WATER;
                excessWater %= STEAM_PER_WATER;

                FluidStack drainedWater = ModHandler.getWaterFromContainer(getInputTank(), (int) amount, true);
                MetaTileEntityLargeBoiler boiler = (MetaTileEntityLargeBoiler) metaTileEntity;
                if (drainedWater == null || drainedWater.amount < amount) {
                    boiler.explodeMultiblock();
                } else {
                    boiler.getOutputTank().fill(ModHandler.getSteam(recipeEUt * 2), true);
                }
            }
        } else {
            currentHeat++;
            writeCustomData(700, buf -> buf.writeVarInt(currentHeat));
        }

        if (++progressTime > maxProgressTime) {
            completeRecipe();
        }
    }

    private int adjustEUtForConfig(int rawEUt) {
        int throttle = ((MetaTileEntityLargeBoiler) metaTileEntity).getThrottle();
        return Math.max(25, (int) (rawEUt * (throttle / 100.0)));
    }

    private int adjustBurnTimeForConfig(int rawBurnTime) {
        MetaTileEntityLargeBoiler boiler = (MetaTileEntityLargeBoiler) metaTileEntity;
        int EUt = boiler.boilerType.steamPerTick();
        int adjustedEUt = adjustEUtForConfig(EUt);
        int adjustedBurnTime = rawBurnTime * EUt / adjustedEUt;
        this.excessProjectedEU += EUt * rawBurnTime - adjustedEUt * adjustedBurnTime;
        adjustedBurnTime += this.excessProjectedEU / adjustedEUt;
        this.excessProjectedEU %= adjustedEUt;
        return adjustedBurnTime;
    }

    private int getMaximumHeat() {
        return ((MetaTileEntityLargeBoiler) metaTileEntity).boilerType.getTicksToBoiling();
    }

    public void invalidate() {
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        setActive(false);
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
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentHeat = compound.getInteger("Heat");
        this.excessFuel = compound.getInteger("ExcessFuel");
        this.excessWater = compound.getInteger("ExcessWater");
        this.excessProjectedEU = compound.getInteger("ExcessProjectedEU");
    }

    @Override
    public void writeInitialData(PacketBuffer buf) {
        super.writeInitialData(buf);
        buf.writeVarInt(currentHeat);
    }

    @Override
    public void receiveInitialData(PacketBuffer buf) {
        super.receiveInitialData(buf);
        this.currentHeat = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 700) { // Should put this value in GregtechDataCodes.
            this.currentHeat = buf.readVarInt();
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
    protected boolean drawEnergy(int recipeEUt) {
        GTLog.logger.error("Large Boiler called drawEnergy(), this should not be possible!");
        return false;
    }

    @Override
    protected long getMaxVoltage() {
        GTLog.logger.error("Large Boiler called getMaxVoltage(), this should not be possible!");
        return 0;
    }
}
