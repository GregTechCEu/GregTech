package gregtech.api.capability.impl;

import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;

import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.RecipeView;

import gregtech.api.recipes.logic.StandardRecipeView;
import gregtech.api.recipes.logic.TrimmedRecipeView;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic {

    public SteamMultiWorkable(RecipeMapSteamMultiblockController tileEntity, double conversionRate) {
        super(tileEntity, tileEntity.recipeMap, tileEntity.getSteamFluidTank(), conversionRate);
        setDistributing(true);
    }

    @Override
    protected @NotNull StandardRecipeView getTrimmedRecipeView(@NotNull Recipe recipe,
                                                               @NotNull MatchCalculation<ItemStack> itemMatch,
                                                               @NotNull MatchCalculation<FluidStack> fluidMatch) {
        return new SteamRecipeView(recipe, itemMatch, fluidMatch, getEUtDiscount(), 1,
                metaTileEntity.getItemOutputLimit(), metaTileEntity.getFluidOutputLimit());
    }

    protected class SteamRecipeView extends TrimmedRecipeView {

        public SteamRecipeView(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                                      @NotNull MatchCalculation<FluidStack> fluidMatch, double voltageDiscount,
                                      int initialParallel, int maxItems, int maxFluids) {
            super(recipe, itemMatch, fluidMatch, voltageDiscount, initialParallel, maxItems, maxFluids);
        }

        @Override
        public int getActualDuration() {
            return (int) (1.5 * super.getActualDuration() * getParallel() / getBaseParallelLimit());
        }

        @Override
        public long getActualVoltage() {
            return (long) Math.min(32, Math.ceil(super.getActualVoltage() * 1.33));
        }
    }
}
