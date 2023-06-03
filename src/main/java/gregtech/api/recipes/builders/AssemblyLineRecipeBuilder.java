package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ResearchProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssemblyLineRecipeBuilder extends RecipeBuilder<AssemblyLineRecipeBuilder> implements IResearchRecipeBuilder {

    private boolean shouldAddResearchRecipe = false;
    private String researchId;
    private ItemStack researchStack = ItemStack.EMPTY;
    private int researchDuration;
    private int researchEUt;

    public AssemblyLineRecipeBuilder() {}

    @SuppressWarnings("unused")
    public AssemblyLineRecipeBuilder(Recipe recipe, RecipeMap<AssemblyLineRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AssemblyLineRecipeBuilder(@Nonnull AssemblyLineRecipeBuilder builder) {
        super(builder);
        this.shouldAddResearchRecipe = builder.shouldAddResearchRecipe;
        this.researchId = builder.researchId;
        this.researchStack = builder.researchStack;
        this.researchDuration = builder.researchDuration;
        this.researchEUt = builder.researchEUt;
    }

    @Override
    public AssemblyLineRecipeBuilder copy() {
        return new AssemblyLineRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@Nonnull String key, @Nullable Object value) {
        if (key.equals(ResearchProperty.KEY)) {
            if (value instanceof ItemStack itemStack) {
                research(itemStack);
                return true;
            }
        }
        return super.applyProperty(key, value);
    }

    private boolean applyResearchProperty(@Nonnull String researchId) {
        if (!ConfigHolder.machines.enableResearch) return false;
        if (researchId.isEmpty()) {
            GTLog.logger.error("Assembly Line Research Id cannot be empty.", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return false;
        }
        if (applyProperty(ResearchProperty.getInstance(), researchId)) {
            this.researchId = researchId;
            return true;
        }
        return false;
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchStack the stack to use for the researchId
     * @return this
     */
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@Nonnull ItemStack researchStack) {
        return researchWithoutRecipe(researchStack.toString());
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@Nonnull String researchId) {
        applyResearchProperty(researchId);
        return this;
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack) {
        return research(researchStack, researchStack.toString());
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @param researchId    the research id for the recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull String researchId) {
        return research(researchStack, researchId, DEFAULT_DURATION, DEFAULT_EUT);
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @param duration      the duration of the research recipe
     * @param EUt           the EUt of the research recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, int duration, int EUt) {
        return research(researchStack, researchStack.toString(), duration, EUt);
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @param researchId    the research id for the recipe
     * @param duration      the duration of the research recipe
     * @param EUt           the EUt of the research recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull String researchId, int duration, int EUt) {
        if (researchStack.isEmpty()) {
            GTLog.logger.error("Research ItemStack must not be empty", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        } else {
            this.researchStack = researchStack;
        }

        if (duration > 0) {
            this.researchDuration = duration;
        } else {
            GTLog.logger.error("Research recipe Duration must be > 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        }

        if (EUt > 0) {
            this.researchEUt = EUt;
        } else {
            GTLog.logger.error("Research recipe EUt must be > 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        }

        if (applyResearchProperty(researchId)) {
            this.shouldAddResearchRecipe = true;
        }
        return this;
    }

    @Override
    public boolean shouldAddResearchRecipe() {
        return this.shouldAddResearchRecipe;
    }

    @Override
    @Nullable
    public String getResearchId() {
        return this.researchId;
    }

    @Override
    @Nonnull
    public ItemStack getResearchStack() {
        return this.researchStack;
    }

    @Override
    public int getResearchDuration() {
        return this.researchDuration;
    }

    @Override
    public int getResearchEUt() {
        return this.researchEUt;
    }
}
