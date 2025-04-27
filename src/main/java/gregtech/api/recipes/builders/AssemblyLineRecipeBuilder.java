package gregtech.api.recipes.builders;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.ResearchProperty;
import gregtech.api.recipes.properties.impl.ResearchPropertyData;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.UnaryOperator;

public class AssemblyLineRecipeBuilder extends RecipeBuilder<AssemblyLineRecipeBuilder> {

    private final Collection<ResearchRecipeEntry> recipeEntries = new ArrayList<>();

    private boolean generatingRecipes = true;

    public AssemblyLineRecipeBuilder() {}

    @SuppressWarnings("unused")
    public AssemblyLineRecipeBuilder(Recipe recipe, RecipeMap<AssemblyLineRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AssemblyLineRecipeBuilder(@NotNull AssemblyLineRecipeBuilder builder) {
        super(builder);
        this.recipeEntries.addAll(builder.getRecipeEntries());
        this.generatingRecipes = builder.generatingRecipes;
    }

    @Override
    public AssemblyLineRecipeBuilder copy() {
        return new AssemblyLineRecipeBuilder(this);
    }

    @Override
    public boolean applyPropertyCT(@NotNull String key, @NotNull Object value) {
        if (key.equals(ResearchProperty.KEY)) {
            if (value instanceof ItemStack itemStack) {
                scannerResearch(itemStack);
                return true;
            }
            return false;
        }
        return super.applyPropertyCT(key, value);
    }

    private boolean applyResearchProperty(ResearchPropertyData.ResearchEntry researchEntry) {
        if (!ConfigHolder.machines.enableResearch) return false;
        if (researchEntry == null) {
            GTLog.logger.error("Assembly Line Research Entry cannot be empty.", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return false;
        }

        if (!generatingRecipes) {
            GTLog.logger.error("Cannot generate recipes when using researchWithoutRecipe()",
                    new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return false;
        }

        ResearchPropertyData property = recipePropertyStorage.get(ResearchProperty.getInstance(), null);
        if (property != null) {
            property.add(researchEntry);
            return true;
        }

        property = new ResearchPropertyData();
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
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@NotNull String researchId) {
        return researchWithoutRecipe(researchId, AssemblyLineManager.getDefaultScannerItem());
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @param dataStack  the stack to hold the data. Must have the {@link IDataItem} behavior.
     * @return this
     */
    public AssemblyLineRecipeBuilder researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
        applyResearchProperty(new ResearchPropertyData.ResearchEntry(researchId, dataStack));
        this.generatingRecipes = false;
        return this;
    }

    /**
     * Generates a research recipe for the Scanner.
     */
    public AssemblyLineRecipeBuilder scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder()).build();
        if (applyResearchProperty(new ResearchPropertyData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.recipeEntries.add(entry);
        }
        return this;
    }

    /**
     * Generates a research recipe for the Scanner. All values are defaults other than the research stack.
     *
     * @param researchStack the stack to use for research
     * @return this
     */
    public AssemblyLineRecipeBuilder scannerResearch(@NotNull ItemStack researchStack) {
        return scannerResearch(b -> b.researchStack(researchStack));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull MetaItem<?>.MetaValueItem metaItem) {
        return scannerResearch(b -> b.researchStack(metaItem));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull MetaItem<?>.MetaValueItem metaItem, int amount) {
        return scannerResearch(b -> b.researchStack(metaItem, amount));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull MetaItem<?>.MetaValueItem metaItem, int amount,
                                                     boolean ignoreNBT) {
        return scannerResearch(b -> b.researchStack(metaItem, amount, ignoreNBT));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull MetaTileEntity mte) {
        return scannerResearch(b -> b.researchStack(mte));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull MetaTileEntity mte, int amount) {
        return scannerResearch(b -> b.researchStack(mte, amount));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull MetaTileEntity mte, int amount, boolean ignoreNBT) {
        return scannerResearch(b -> b.researchStack(mte, amount, ignoreNBT));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull OrePrefix prefix, @NotNull Material material) {
        return scannerResearch(b -> b.researchStack(prefix, material));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull OrePrefix prefix, @NotNull Material material,
                                                     int amount) {
        return scannerResearch(b -> b.researchStack(prefix, material, amount));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull OrePrefix prefix, @NotNull Material material, int amount,
                                                     boolean ignoreNBT) {
        return scannerResearch(b -> b.researchStack(prefix, material, amount, ignoreNBT));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull Block block) {
        return scannerResearch(b -> b.researchStack(block));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull Block block, int amount) {
        return scannerResearch(b -> b.researchStack(block, amount));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull Block block, int amount, int meta) {
        return scannerResearch(b -> b.researchStack(block, amount, meta));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull Block block, int amount, boolean ignoreNBT) {
        return scannerResearch(b -> b.researchStack(block, amount, ignoreNBT));
    }

    public AssemblyLineRecipeBuilder scannerResearch(@NotNull Block block, int amount, int meta, boolean ignoreNBT) {
        return scannerResearch(b -> b.researchStack(block, amount, meta, ignoreNBT));
    }

    /**
     * Generates a research recipe for the Research Station.
     */
    public AssemblyLineRecipeBuilder stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.StationRecipeBuilder()).build();
        if (applyResearchProperty(new ResearchPropertyData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.recipeEntries.add(entry);
        }
        return this;
    }

    @NotNull
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
        private final boolean ignoreNBT;
        private final int duration;
        private final long EUt;
        private final int CWUt;

        /**
         * @param researchId    the id of the research to store
         * @param researchStack the stack to scan for research
         * @param dataStack     the stack to contain the data
         * @param duration      the duration of the recipe
         * @param EUt           the EUt of the recipe
         * @param CWUt          how much computation per tick this recipe needs if in Research Station
         *                      <p>
         *                      By default, will ignore NBT on researchStack input. If NBT matching is desired, see
         *                      {@link #ResearchRecipeEntry(String, ItemStack, ItemStack, boolean, int, long, int)}
         */
        public ResearchRecipeEntry(@NotNull String researchId, @NotNull ItemStack researchStack,
                                   @NotNull ItemStack dataStack, int duration, long EUt, int CWUt) {
            this.researchId = researchId;
            this.researchStack = researchStack;
            this.dataStack = dataStack;
            this.duration = duration;
            this.EUt = EUt;
            this.CWUt = CWUt;
            this.ignoreNBT = true;
        }

        /**
         * @param researchId    the id of the research to store
         * @param researchStack the stack to scan for research
         * @param dataStack     the stack to contain the data
         * @param duration      the duration of the recipe
         * @param EUt           the EUt of the recipe
         * @param CWUt          how much computation per tick this recipe needs if in Research Station
         */
        public ResearchRecipeEntry(@NotNull String researchId, @NotNull ItemStack researchStack,
                                   @NotNull ItemStack dataStack, boolean ignoreNBT, int duration, long EUt, int CWUt) {
            this.researchId = researchId;
            this.researchStack = researchStack;
            this.dataStack = dataStack;
            this.ignoreNBT = ignoreNBT;
            this.duration = duration;
            this.EUt = EUt;
            this.CWUt = CWUt;
        }

        @NotNull
        public String getResearchId() {
            return this.researchId;
        }

        @NotNull
        public ItemStack getResearchStack() {
            return researchStack;
        }

        @NotNull
        public ItemStack getDataStack() {
            return dataStack;
        }

        public boolean getIgnoreNBT() {
            return ignoreNBT;
        }

        public int getDuration() {
            return duration;
        }

        public long getEUt() {
            return EUt;
        }

        public int getCWUt() {
            return CWUt;
        }
    }
}
