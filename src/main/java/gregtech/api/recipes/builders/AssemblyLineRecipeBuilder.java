package gregtech.api.recipes.builders;

import gregtech.api.GTValues;
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
import java.util.ArrayList;
import java.util.Collection;

public class AssemblyLineRecipeBuilder extends RecipeBuilder<AssemblyLineRecipeBuilder> {

    public static final int DEFAULT_DURATION = 144000;
    public static final int DEFAULT_EUT = GTValues.VA[GTValues.LV];

    private final Collection<ResearchRecipeEntry> recipeEntries = new ArrayList<>();

    private boolean generatingRecipes = true;

    public AssemblyLineRecipeBuilder() {}

    @SuppressWarnings("unused")
    public AssemblyLineRecipeBuilder(Recipe recipe, RecipeMap<AssemblyLineRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AssemblyLineRecipeBuilder(@Nonnull AssemblyLineRecipeBuilder builder) {
        super(builder);
        this.recipeEntries.addAll(builder.getRecipeEntries());
        this.generatingRecipes = builder.generatingRecipes;
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

    private boolean applyResearchProperty(ResearchPropertyData.ResearchEntry researchEntry) {
        if (!ConfigHolder.machines.enableResearch) return false;
        if (researchEntry == null) {
            GTLog.logger.error("Assembly Line Research Entry cannot be empty.", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return false;
        }

        if (!generatingRecipes) {
            GTLog.logger.error("Cannot generate recipes when using researchWithoutRecipe()", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return false;
        }

        if (recipePropertyStorage != null && recipePropertyStorage.hasRecipeProperty(ResearchProperty.getInstance())) {
            ResearchPropertyData property = recipePropertyStorage.getRecipePropertyValue(ResearchProperty.getInstance(), null);
            if (property == null) throw new IllegalStateException("Property storage has a null property");
            property.add(researchEntry);
            return true;
        }

        ResearchPropertyData property = new ResearchPropertyData();
        if (applyProperty(ResearchProperty.getInstance(), property)) {
            property.add(researchEntry);
            return true;
        }

        return false;
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @return this
     */
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@Nonnull String researchId) {
        return researchWithoutRecipe(researchId, AssemblyLineManager.getDefaultDataItem());
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @param dataStack     the stack to hold the data. Must have the {@link gregtech.api.items.metaitem.stats.IDataStick} behavior.
     * @return this
     */
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@Nonnull String researchId, @Nonnull ItemStack dataStack) {
        applyResearchProperty(new ResearchPropertyData.ResearchEntry(researchId, dataStack));
        this.generatingRecipes = false;
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
        }

        if (!dataStack.isEmpty()) {
            boolean foundBehavior = false;
            if (dataStack.getItem() instanceof MetaItem<?> metaItem) {
                for (IItemBehaviour behaviour : metaItem.getBehaviours(dataStack)) {
                    if (behaviour instanceof IDataStick) {
                        foundBehavior = true;
                        dataStack = dataStack.copy();
                        dataStack.setCount(1);
                        break;
                    }
                }
            }
            if (!foundBehavior) {
                GTLog.logger.error("Data ItemStack must have the IDataStick behavior", new IllegalArgumentException());
                recipeStatus = EnumValidationResult.INVALID;
                return this;
            }
        } else {
            dataStack = AssemblyLineManager.getDefaultDataItem();
        }

        if (duration <= 0) {
            GTLog.logger.error("Research recipe Duration must be > 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        }

        if (EUt <= 0) {
            GTLog.logger.error("Research recipe EUt must be > 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        }

        if (applyResearchProperty(new ResearchPropertyData.ResearchEntry(researchId, dataStack))) {
            this.recipeEntries.add(new ResearchRecipeEntry(researchId, researchStack, dataStack, duration, EUt));
        }

        return this;
    }

    @Nonnull
    public Collection<ResearchRecipeEntry> getRecipeEntries() {
        return this.recipeEntries;
    }

    /**
     * An entry for an autogenerated research recipe for producing a data item containing research data.
     */
    public static class ResearchRecipeEntry {

        private final String researchId;
        private final ItemStack researchStack;
        private final ItemStack dataStack;
        private final int duration;
        private final int EUt;

        /**
         * @param researchId the id of the research to store
         * @param researchStack the stack to scan for research
         * @param dataStack the stack to contain the data
         * @param duration the duration of the recipe
         * @param EUt the EUt of the recipe
         */
        public ResearchRecipeEntry(@Nonnull String researchId, @Nonnull ItemStack researchStack, @Nonnull ItemStack dataStack, int duration, int EUt) {
            this.researchId = researchId;
            this.researchStack = researchStack;
            this.dataStack = dataStack;
            this.duration = duration;
            this.EUt = EUt;
        }

        @Nonnull
        public String getResearchId() {
            return this.researchId;
        }

        @Nonnull
        public ItemStack getResearchStack() {
            return researchStack;
        }

        @Nonnull
        public ItemStack getDataStack() {
            return dataStack;
        }

        public int getDuration() {
            return duration;
        }

        public int getEUt() {
            return EUt;
        }
    }
}
