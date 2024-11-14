package gregtech.api.capability.impl;

import gregtech.api.capability.IRotorHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiblockFuelRecipeLogic extends MultiblockRecipeLogic {

    private boolean blockConsumption = false;

    protected long totalContinuousRunningTime;

    public MultiblockFuelRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    protected boolean canSubtick() {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (workingEnabled && isActive && currentRecipe != null) {
            totalContinuousRunningTime++;
        } else {
            totalContinuousRunningTime = 0;
        }
    }

    @Override
    public int getBaseParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }

    /**
     * Boost the energy production.
     * Should not change the state of the workable logic. Only read current values.
     *
     * @param production the energy amount to boost
     * @return the boosted energy amount
     */
    protected long boostProduction(long production) {
        return production;
    }

    @Override
    protected boolean produceEnergy(long eu, boolean simulate) {
        return super.produceEnergy(boostProduction(eu), simulate);
    }

    @Override
    public long getInfoProviderEUt() {
        return boostProduction(super.getInfoProviderEUt());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        totalContinuousRunningTime = 0;
    }

    @Override
    protected boolean performConsumption(@NotNull MatchCalculation<ItemStack> itemMatch,
                                         @NotNull MatchCalculation<FluidStack> fluidMatch, @NotNull RecipeView view,
                                         @NotNull RecipeRun run, @NotNull List<ItemStack> items,
                                         @NotNull List<FluidStack> fluids) {
        if (!blockConsumption) return super.performConsumption(itemMatch, fluidMatch, view, run, items, fluids);
        else return true;
    }

    @Nullable
    public String getRecipeFluidInputInfo() {
        IRotorHolder rotorHolder = null;

        if (metaTileEntity instanceof MultiblockWithDisplayBase multiblockWithDisplayBase) {
            List<IRotorHolder> abilities = multiblockWithDisplayBase.getAbilities(MultiblockAbility.ROTOR_HOLDER);
            rotorHolder = abilities.size() > 0 ? abilities.get(0) : null;
        }

        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        Recipe recipe = null;
        if (previousRecipe == null) {
            PropertySet set = computePropertySet();
            blockConsumption = true;
            for (DistinctInputGroup group : getInputGroups()) {
                Pair<RecipeRun, Recipe> pair = findRecipeRun(group.itemInventoryView(), group.fluidInventoryView(),
                        set);
                if (pair != null) {
                    recipe = pair.getRight();
                    break;
                }
            }
            blockConsumption = false;
            if (recipe == null) return null;
        } else {
            recipe = previousRecipe;
        }
        FluidStack requiredFluidInput = recipe.getFluidIngredients().get(0).getAllMatchingStacks().get(0);

        int parallel = GTUtility.safeCastLongToInt(getMaxAmperageOut() / recipe.getAmperage());
        int neededAmount = parallel * requiredFluidInput.amount;
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            neededAmount /= (rotorHolder.getTotalEfficiency() / 100.0);
        } else if (rotorHolder != null && !rotorHolder.hasRotor()) {
            return null;
        }
        return TextFormatting.RED + TextFormattingUtil.formatNumbers(neededAmount) + "L";
    }

    public FluidStack getInputFluidStack() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        PropertySet set = computePropertySet();
        blockConsumption = true;
        FluidStack input = null;
        for (DistinctInputGroup group : getInputGroups()) {
            RecipeRun recipe = null;
            if (previousRecipe != null) {
                Pair<RecipeRun, Recipe> pair = matchRecipe(previousRecipe, group.itemInventoryView(),
                        group.fluidInventoryView(), set);
                if (pair != null) recipe = pair.getLeft();
            }
            if (recipe == null) {
                Pair<RecipeRun, Recipe> pair = findRecipeRun(group.itemInventoryView(), group.fluidInventoryView(),
                        set);
                if (pair != null) recipe = pair.getLeft();
            }
            if (recipe == null) continue;

            input = recipe.getFluidsConsumed().get(0).copy();
            input.amount = 0;
            for (FluidStack stack : group.fluidInventoryView()) {
                if (input.isFluidEqual(stack)) {
                    input.amount += stack.amount;
                }
            }
            break;
        }
        blockConsumption = false;
        return input;
    }

    @Override
    public long getMaxOverclockVoltage(boolean generatingRecipe) {
        return 0;
    }
}
