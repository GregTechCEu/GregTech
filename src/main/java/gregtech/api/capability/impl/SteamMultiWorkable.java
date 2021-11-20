package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import net.minecraftforge.items.IItemHandlerModifiable;


/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic {

    public SteamMultiWorkable(RecipeMapSteamMultiblockController tileEntity, double conversionRate) {
        super(tileEntity, tileEntity.recipeMap, tileEntity.getSteamFluidTank(), conversionRate);
    }

    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.APPEND;
    }

    @Override
    public void applyParallelBonus(RecipeBuilder<?> builder) {
        builder.EUt(5).duration(192);
    }
}
