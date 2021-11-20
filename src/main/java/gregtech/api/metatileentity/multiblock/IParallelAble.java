package gregtech.api.metatileentity.multiblock;

import gregtech.api.recipes.RecipeBuilder;

public interface IParallelAble {

    default int getParallelLimit() {
        return 1;
    }

    default void applyParallelBonus(RecipeBuilder<?> builder) {
    }

    default void applyBuilderFeatures(RecipeBuilder<?> builder){
    }
}
