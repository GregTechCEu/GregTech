package gregtech.api.recipes.builders;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.recipes.recipeproperties.ResearchProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;

public class AssemblyLineRecipeBuilder extends RecipeBuilder<AssemblyLineRecipeBuilder> {

    private ItemStack researchItem = ItemStack.EMPTY;
    private int scanDuration = -1;
    private int scanEUt = -1;
    private boolean shouldAddResearchRecipe = true;

    public AssemblyLineRecipeBuilder() {
    }

    public AssemblyLineRecipeBuilder(Recipe recipe, RecipeMap<AssemblyLineRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AssemblyLineRecipeBuilder(AssemblyLineRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
        this.researchItem = recipeBuilder.researchItem;
        this.scanDuration = recipeBuilder.scanDuration;
        this.scanEUt = recipeBuilder.scanEUt;
        this.shouldAddResearchRecipe = recipeBuilder.shouldAddResearchRecipe;
    }

    @Override
    public AssemblyLineRecipeBuilder copy() {
        return new AssemblyLineRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@Nonnull String key, Object value) {
        if (key.equals(ResearchProperty.KEY)) {
            this.research(value.toString());
            return true;
        }
        return super.applyProperty(key, value);
    }

    /**
     * Do not use! Look for {@link AssemblyLineRecipeBuilder#research(ItemStack, boolean)}
     */
    private AssemblyLineRecipeBuilder research(@Nonnull String researchId) {
        this.applyProperty(ResearchProperty.getInstance(), researchId);
        return this;
    }

    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchItem, boolean generateRecipe) {
        this.shouldAddResearchRecipe = generateRecipe;
        return research(researchItem);
    }

    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchItem) {
        return research(researchItem, 144000, GTValues.VA[GTValues.LV]);
    }

    public AssemblyLineRecipeBuilder research(@Nonnull ItemStack researchItem, int scanDuration, int scanEUt) {
        if (researchItem.isEmpty()) {
            GTLog.logger.error("Assemblyline Research Items cannot be empty", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            this.researchItem = researchItem;
            research(researchItem.getTranslationKey());
        }
        if (scanDuration <=0) {
            GTLog.logger.error("Assemblyline Research Duration must be greater than 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            this.scanDuration = scanDuration;
        }
        if (scanEUt <= 0) {
            GTLog.logger.error("Assemblyline Research EUt must be greater than 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            this.scanEUt = scanEUt;
        }

        return this;
    }

    @Override
    public ValidationResult<Recipe> build() {
        ValidationResult<Recipe> result = super.build();
        if (result.getType() == EnumValidationResult.VALID && !this.outputs.isEmpty() && !this.outputs.get(0).isEmpty()) {
            // only apply the research property if an item was specified
            if (!this.researchItem.isEmpty() && !recipePropertyStorage.hasRecipeProperty(ResearchProperty.getInstance())) {
                applyProperty(ResearchProperty.KEY, this.outputs.get(0).getTranslationKey());
            }
        }

        return result;
    }

    @Nonnull
    public ItemStack getResearchItem() {
        return researchItem;
    }

    public int getScanDuration() {
        return scanDuration;
    }

    public int getScanEUt() {
        return scanEUt;
    }

    public boolean shouldAddResearchRecipe() {
        return shouldAddResearchRecipe;
    }

    @Nonnull
    public String getResearchId() {
        return this.recipePropertyStorage == null ? "" :
                this.recipePropertyStorage.getRecipePropertyValue(ResearchProperty.getInstance(), "");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ResearchProperty.getInstance().getKey(), getResearchId())
                .toString();
    }

    @Nonnull
    public static NBTTagCompound generateResearchNBT(@Nonnull String researchId) {
        if (researchId.isEmpty()) throw new IllegalArgumentException("Assemblyline researchId cannot be empty");
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(IResearchRecipeMap.RESEARCH_ID_NBT_TAG, researchId);
        return compound;
    }
}
