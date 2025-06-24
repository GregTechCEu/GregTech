package gregtech.api.capability.impl;

import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.RecipeBuilder;

import org.jetbrains.annotations.NotNull;

/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic {

    ParallelLogicType type;
    public SteamMultiWorkable(RecipeMapSteamMultiblockController tileEntity, double conversionRate,ParallelLogicType type) {
        super(tileEntity, tileEntity.recipeMap,tileEntity.getSteamFluidTank(), conversionRate);
        this.type = type;
    }

    @NotNull
    @Override
    public ParallelLogicType getParallelLogicType() {
        return type;
    }

    @Override
    public void applyParallelBonus(@NotNull RecipeBuilder<?> builder) {
        long currentRecipeEU = builder.getEUt();
        int currentRecipeDuration = builder.getDuration() / getParallelLimit();
        builder.EUt((long) Math.min(32, Math.ceil(currentRecipeEU * 1.33)))
                .duration((int) (currentRecipeDuration * 1.5));
    }

}
