package gregtech.api.capability.impl;

import gregtech.api.fluids.FluidConstants;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.BoilerType;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoilerLogic {

    public static final int STEAM_PER_WATER = 160;
    public static final int EU_PER_WATER = 160;
    public static final int EU_PER_SOLID_BURNTIME = 20;

    private static final int BUFFER_TICKS = 80;

    protected final MetaTileEntityLargeBoiler boiler;
    protected final BoilerType boilerType;
    protected int chassisHeat;

    protected boolean isDry;

    private int targetEUt = -1;
    protected int remainingBurnEU;

    protected boolean needsInputUpdate;

    private int lastTickSteamOutput;

    public BoilerLogic(MetaTileEntityLargeBoiler boiler) {
        this.boiler = boiler;
        this.boilerType = boiler.boilerType;
    }

    /**
     * See {@link gregtech.common.metatileentities.multi.BoilerType} for why this equation exists.
     * 
     * @param chassisTemperature the current chassis temperature
     * @return the amount of water to boil this tick.
     */
    public int getWaterBoilAmount(int chassisTemperature) {
        if (chassisTemperature <= 373) return 0;
        int excess = chassisTemperature - 373;
        return (int) (0.0000313916 * (excess * excess) + 0.0317136 * excess);
    }

    public int getChassisHeat() {
        return chassisHeat;
    }

    public int getChassisTemperature(int chassisHeat) {
        return chassisHeat / boilerType.chassisThermalInertia() + getAmbientTemperature();
    }

    public int getHeatLoss(int chassisTemperature) {
        return (int) Math.sqrt(chassisTemperature - getAmbientTemperature());
    }

    protected int getAmbientTemperature() {
        return FluidConstants.ROOM_TEMPERATURE;
    }

    /**
     * @return target EU generation, such that EU transferred into the chassis matches the amount of EU used to boil
     *         water at maximum chassis temperature.
     */
    protected int targetFuelEUGeneration() {
        if (targetEUt != -1) return adjustEUtForThrottle(targetEUt);
        return adjustEUtForThrottle(
                targetEUt = getWaterBoilAmount(boilerType.maximumChassisTemperature()) * EU_PER_WATER);
    }

    /**
     * @return the maximum temperature the boiler can be at when water is introduced without exploding.
     */
    protected int safetyCutoff() {
        return 473;
    }

    protected int tryDrainWater(final int amount) {
        if (amount == 0) return 0;
        int drain = 0;
        FluidStack drainedWater = boiler.getImportFluids().drain(Materials.Water.getFluid(amount), true);
        if (drainedWater != null) drain += drainedWater.amount;
        if (drain < amount) {
            drainedWater = boiler.getImportFluids().drain(Materials.DistilledWater.getFluid(amount - drain), true);
            if (drainedWater != null) drain += drainedWater.amount;
            if (drain < amount) {
                for (String fluidName : ConfigHolder.machines.boilerFluids) {
                    Fluid fluid = FluidRegistry.getFluid(fluidName);
                    if (fluid != null) {
                        drainedWater = boiler.getImportFluids().drain(new FluidStack(fluid, amount - drain), true);
                        if (drainedWater != null) drain += drainedWater.amount;
                        if (drain >= amount) {
                            break;
                        }
                    }
                }
            }
        }
        return drain;
    }

    protected void tryStartNextBurn() {
        if (ConfigHolder.machines.enableMaintenance && boiler.hasMaintenanceMechanics() &&
                boiler.getNumMaintenanceProblems() > 5) {
            return;
        }
        if (needsInputUpdate) {
            if (!boiler.getNotifiedItemInputList().isEmpty() || !boiler.getNotifiedFluidInputList().isEmpty()) {
                boiler.getNotifiedItemInputList().clear();
                boiler.getNotifiedFluidInputList().clear();
                needsInputUpdate = false;
            } else {
                return;
            }
        }
        // first, combustion.
        CompactibleIterator<Recipe> recipes = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipes(boiler.getImportItems(),
                boiler.getImportFluids(), boiler.computePropertySet());
        while (recipes.hasNext()) {
            if (tryStartRecipe(recipes.next())) return;
        }
        // second, semi-fluid.
        recipes = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipes(boiler.getImportItems(), boiler.getImportFluids(),
                boiler.computePropertySet());
        while (recipes.hasNext()) {
            if (tryStartRecipe(recipes.next())) return;
        }
        // finally, solid.
        IItemHandlerModifiable items = boiler.getImportItems();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack extract = items.extractItem(i, Integer.MAX_VALUE, true);
            // don't burn something that has a container.
            if (extract.getItem().hasContainerItem(extract)) continue;
            int burn = TileEntityFurnace.getItemBurnTime(extract);
            if (burn > 0) {
                int consumption = 1;
                // TODO make this a binary search
                while (consumption < extract.getCount() &&
                        (remainingBurnEU + burn * consumption) < targetFuelEUGeneration() * BUFFER_TICKS) {
                    consumption++;
                }
                items.extractItem(i, consumption, false);
                remainingBurnEU += burn * consumption;
                return;
            }
        }
        needsInputUpdate = true;
    }

    protected boolean tryStartRecipe(Recipe candidate) {
        // we cannot handle outputs
        if (candidate.getItemOutputProvider().getMaximumOutputs(1) +
                candidate.getFluidOutputProvider().getMaximumOutputs(1) != 0)
            return false;
        int eu = GTUtility
                .safeCastLongToInt(candidate.getVoltage() * candidate.getAmperage() * candidate.getDuration());
        int scale = ((targetFuelEUGeneration() * BUFFER_TICKS - remainingBurnEU) / eu) + 1;
        List<ItemStack> itemView = null;
        MatchCalculation<ItemStack> matchItem = null;
        List<FluidStack> fluidView = null;
        MatchCalculation<FluidStack> matchFluid = null;
        if (!candidate.getItemIngredients().isEmpty()) {
            itemView = GTUtility.itemHandlerToList(boiler.getImportItems());
            matchItem = IngredientMatchHelper.matchItems(candidate.getItemIngredients(), itemView);
            scale = matchItem.largestSucceedingScale(scale);
            if (scale == 0) return false;
        }
        if (!candidate.getFluidIngredients().isEmpty()) {
            fluidView = GTUtility.fluidHandlerToList(boiler.getImportFluids());
            matchFluid = IngredientMatchHelper.matchFluids(candidate.getFluidIngredients(), fluidView);
            scale = matchFluid.largestSucceedingScale(scale);
            if (scale == 0) return false;
        }

        if (matchItem != null) {
            long[] consumptions = matchItem.getConsumeResultsForScaleAndBoost(scale, 0);
            if (consumptions == null) return false;
            for (int i = 0; i < consumptions.length; i++) {
                itemView.get(i).shrink((int) consumptions[i]);
            }
        }
        if (matchFluid != null) {
            long[] consumptions = matchFluid.getConsumeResultsForScaleAndBoost(scale, 0);
            if (consumptions == null) return false;
            for (int i = 0; i < consumptions.length; i++) {
                FluidStack stack = fluidView.get(i);
                stack.amount -= (int) consumptions[i];
                if (stack.amount <= 0) fluidView.set(i, null);
            }
        }
        remainingBurnEU += eu * scale;
        return true;
    }

    public boolean isBurningFuel() {
        return boiler.canOperate() && remainingBurnEU > targetFuelEUGeneration();
    }

    public void update() {
        if (boiler.canOperate() && getChassisTemperature(chassisHeat) < boilerType.maximumChassisTemperature()) {
            int generation = targetFuelEUGeneration();
            if (remainingBurnEU > generation) {
                remainingBurnEU -= generation;
                chassisHeat += generation * getBurnEfficiencyFromMaintenance();
            } else {
                tryStartNextBurn();
            }
        }
        int temperature = getChassisTemperature(chassisHeat);
        int boil = getWaterBoilAmount(temperature);
        boil = tryDrainWater(boil);
        chassisHeat -= boil * EU_PER_WATER + getHeatLoss(temperature);
        if (boil == 0) {
            isDry = true;
        } else if (isDry) {
            if (temperature > safetyCutoff()) {
                boiler.explodeMultiblock(8.0f * temperature / boilerType.maximumChassisTemperature());
                return;
            }
            isDry = false;
        }
        setLastTickSteam(boil * STEAM_PER_WATER);
        boiler.getExportFluids().fill(Materials.Steam.getFluid(boil * STEAM_PER_WATER), true);
    }

    private double getBurnEfficiencyFromMaintenance() {
        if (ConfigHolder.machines.enableMaintenance) {
            return (1 - 0.1 * boiler.getNumMaintenanceProblems());
        } else return 1;
    }

    private int adjustEUtForThrottle(int rawEUt) {
        int throttle = boiler.getThrottle();
        return (int) Math.max(25, rawEUt * (throttle / 100.0));
    }

    public int getLastTickSteam() {
        return lastTickSteamOutput;
    }

    public void setLastTickSteam(int lastTickSteamOutput) {
        // if (lastTickSteamOutput != this.lastTickSteamOutput && !metaTileEntity.getWorld().isRemote) {
        // writeCustomData(BOILER_LAST_TICK_STEAM, b -> b.writeInt(lastTickSteamOutput));
        // }
        this.lastTickSteamOutput = lastTickSteamOutput;
    }

    public void invalidate() {
        setLastTickSteam(0);
    }

    @NotNull
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("Heat", chassisHeat);
        compound.setInteger("BurnTime", remainingBurnEU);
        return compound;
    }

    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.chassisHeat = compound.getInteger("Heat");
        this.remainingBurnEU = compound.getInteger("BurnTime");
    }

    // public void writeInitialSyncData(@NotNull PacketBuffer buf) {
    // super.writeInitialSyncData(buf);
    // buf.writeVarInt(chassisHeat);
    // buf.writeInt(lastTickSteamOutput);
    // }

    // @Override
    // public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
    // super.receiveInitialSyncData(buf);
    // this.currentHeat = buf.readVarInt();
    // this.lastTickSteamOutput = buf.readInt();
    // }

    // @Override
    // public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
    // super.receiveCustomData(dataId, buf);
    // if (dataId == BOILER_HEAT) {
    // this.currentHeat = buf.readVarInt();
    // } else if (dataId == BOILER_LAST_TICK_STEAM) {
    // this.lastTickSteamOutput = buf.readInt();
    // }
    // }
}
