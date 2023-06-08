package gregtech.api.recipes.builders;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataStick;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ResearchProperty;
import gregtech.api.recipes.recipeproperties.ResearchPropertyData;
import gregtech.api.util.AssemblyLineManager;
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
    private ItemStack dataStack = ItemStack.EMPTY;
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
        this.dataStack = builder.dataStack;
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
        if (dataStack.isEmpty()) dataStack = AssemblyLineManager.getDefaultDataItem();
        if (applyProperty(ResearchProperty.getInstance(), new ResearchPropertyData(researchId, dataStack))) {
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
        return research(researchStack, ItemStack.EMPTY);
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @param dataStack     the stack to hold the data. Must have the {@link gregtech.api.items.metaitem.stats.IDataStick} behavior.
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull ItemStack dataStack) {
        return research(researchStack, dataStack, researchStack.toString());
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
     * @param dataStack     the stack to hold the data. Must have the {@link gregtech.api.items.metaitem.stats.IDataStick} behavior.
     * @param researchId    the research id for the recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull ItemStack dataStack, @Nonnull String researchId) {
        return research(researchStack, dataStack, researchId, DEFAULT_DURATION, DEFAULT_EUT);
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
     * @param dataStack     the stack to hold the data. Must have the {@link gregtech.api.items.metaitem.stats.IDataStick} behavior.
     * @param duration      the duration of the research recipe
     * @param EUt           the EUt of the research recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull ItemStack dataStack, int duration, int EUt) {
        return research(researchStack, dataStack, researchStack.toString(), duration, EUt);
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @param duration      the duration of the research recipe
     * @param EUt           the EUt of the research recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull String researchId, int duration, int EUt) {
        return research(researchStack, ItemStack.EMPTY, researchId, duration, EUt);
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @param dataStack     the stack to hold the data. Must have the {@link gregtech.api.items.metaitem.stats.IDataStick} behavior.
     * @param researchId    the research id for the recipe
     * @param duration      the duration of the research recipe
     * @param EUt           the EUt of the research recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack, @Nonnull ItemStack dataStack, @Nonnull String researchId, int duration, int EUt) {
        if (researchStack.isEmpty()) {
            GTLog.logger.error("Research ItemStack must not be empty", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        } else {
            this.researchStack = researchStack;
        }

        if (!dataStack.isEmpty()) {
            boolean foundBehavior = false;
            if (dataStack.getItem() instanceof MetaItem<?> metaItem) {
                for (IItemBehaviour behaviour : metaItem.getBehaviours(dataStack)) {
                    if (behaviour instanceof IDataStick) {
                        foundBehavior = true;
                        this.dataStack = dataStack.copy();
                        this.dataStack.setCount(1);
                        break;
                    }
                }
            }
            if (!foundBehavior) {
                GTLog.logger.error("Data ItemStack must have the IDataStick behavior", new IllegalArgumentException());
                recipeStatus = EnumValidationResult.INVALID;
                return this;
            }
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

    @Nonnull
    @Override
    public ItemStack getDataItem() {
        return this.dataStack;
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
