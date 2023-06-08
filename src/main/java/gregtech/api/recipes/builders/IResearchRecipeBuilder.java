package gregtech.api.recipes.builders;

import gregtech.api.GTValues;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IResearchRecipeBuilder {

    int DEFAULT_DURATION = 144000;
    int DEFAULT_EUT = GTValues.VA[GTValues.LV];

    /**
     * @return if a research recipe should be added.
     */
    boolean shouldAddResearchRecipe();

    /**
     * @return the research ID used for the recipe.
     */
    @Nullable
    String getResearchId();

    /**
     * @return the ItemStack used for research. Can be {@link ItemStack#EMPTY}.
     */
    @Nonnull
    ItemStack getResearchStack();

    /**
     * @return the ItemStack used to hold the research data. Can be {@link ItemStack#EMPTY}
     */
    @Nonnull
    ItemStack getDataItem();

    /**
     * @return the duration of the research recipe to generate.
     */
    int getResearchDuration();

    /**
     * @return the EUt of the research recipe to generate.
     */
    int getResearchEUt();
}
