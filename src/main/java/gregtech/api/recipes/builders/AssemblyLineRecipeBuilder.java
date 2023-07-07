package gregtech.api.recipes.builders;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ResearchProperty;
import gregtech.api.recipes.recipeproperties.ResearchPropertyData;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTStringUtils;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.UnaryOperator;

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
        return researchWithoutRecipe(researchId, AssemblyLineManager.getDefaultScannerItem());
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @param dataStack     the stack to hold the data. Must have the {@link IDataItem} behavior.
     * @return this
     */
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@Nonnull String researchId, @Nonnull ItemStack dataStack) {
        applyResearchProperty(new ResearchPropertyData.ResearchEntry(researchId, dataStack));
        this.generatingRecipes = false;
        return this;
    }

    public AssemblyLineRecipeBuilder research(UnaryOperator<ResearchBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchBuilder()).build();
        if (applyResearchProperty(new ResearchPropertyData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.recipeEntries.add(entry);
        }
        return this;
    }

    /**
     * Generates a research recipe.
     *
     * @param researchStack the stack to use for research
     * @return this
     */
    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchStack) {
        return research(b -> new ResearchBuilder().researchStack(researchStack));
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
        private final int CWUt;

        /**
         * @param researchId the id of the research to store
         * @param researchStack the stack to scan for research
         * @param dataStack the stack to contain the data
         * @param duration the duration of the recipe
         * @param EUt the EUt of the recipe
         * @param CWUt how much computation per tick this recipe needs if in Research Station
         */
        public ResearchRecipeEntry(@Nonnull String researchId, @Nonnull ItemStack researchStack, @Nonnull ItemStack dataStack, int duration, int EUt, int CWUt) {
            this.researchId = researchId;
            this.researchStack = researchStack;
            this.dataStack = dataStack;
            this.duration = duration;
            this.EUt = EUt;
            this.CWUt = CWUt;
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

        public int getCWUt() {
            return CWUt;
        }
    }

    public static class ResearchBuilder {

        private ItemStack researchStack;
        private ItemStack dataStack;
        private String researchId;
        private int duration;
        private int eut;
        private int cwut;

        private ResearchBuilder() {/**/}

        public ResearchBuilder researchStack(@Nonnull ItemStack researchStack) {
            if (!researchStack.isEmpty()) {
                this.researchStack = researchStack;
            }
            return this;
        }

        public ResearchBuilder dataStack(@Nonnull ItemStack dataStack) {
            if (!dataStack.isEmpty()) {
                this.dataStack = dataStack;
            }
            return this;
        }

        public ResearchBuilder researchId(String researchId) {
            this.researchId = researchId;
            return this;
        }

        public ResearchBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public ResearchBuilder EUt(int eut) {
            this.eut = eut;
            return this;
        }

        /** If this value is greater than zero, then this recipe will be done in the Research Station instead of the Scanner */
        public ResearchBuilder CWUt(int cwut) {
            this.cwut = cwut;
            return this;
        }

        private ResearchRecipeEntry build() {
            if (researchStack == null) {
                throw new IllegalArgumentException("Research stack cannot be null or empty!");
            }

            if (researchId == null) {
                researchId = GTStringUtils.itemStackToString(researchStack);
            }

            if (dataStack == null) {
                dataStack = cwut > 0
                        ? AssemblyLineManager.getDefaultResearchStationItem()
                        : AssemblyLineManager.getDefaultScannerItem();
            }

            boolean foundBehavior = false;
            if (dataStack.getItem() instanceof MetaItem<?> metaItem) {
                for (IItemBehaviour behaviour : metaItem.getBehaviours(dataStack)) {
                    if (behaviour instanceof IDataItem) {
                        foundBehavior = true;
                        dataStack = dataStack.copy();
                        dataStack.setCount(1);
                        break;
                    }
                }
            }
            if (!foundBehavior) {
                throw new IllegalArgumentException("Data ItemStack must have the IDataItem behavior");
            }

            if (duration <= 0) {
                duration = DEFAULT_DURATION;
            }

            if (eut <= 0) {
                eut = DEFAULT_EUT;
            }
            return new ResearchRecipeEntry(researchId, researchStack, dataStack, duration, eut, cwut);
        }
    }
}
