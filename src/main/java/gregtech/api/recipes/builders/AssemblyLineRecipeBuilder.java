package gregtech.api.recipes.builders;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ResearchItemProperty;
import gregtech.api.util.ValidationResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AssemblyLineRecipeBuilder extends RecipeBuilder<AssemblyLineRecipeBuilder> {

    public static final String RESEARCH_NBT_TAG_NAME = "asslineOutput";

    protected ItemStack researchItem;

    protected int scanningVoltage = GTValues.VA[GTValues.HV];
    protected int scanningDuration = 40;

    public AssemblyLineRecipeBuilder() {
    }

    public AssemblyLineRecipeBuilder(Recipe recipe, RecipeMap<AssemblyLineRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
        this.researchItem = recipe.getProperty(ResearchItemProperty.getInstance(), ItemStack.EMPTY);
    }

    public AssemblyLineRecipeBuilder(AssemblyLineRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
        this.researchItem = recipeBuilder.researchItem;
    }

    @Override
    public boolean applyProperty(String key, ItemStack researchItem) {
        if (key.equals(ResearchItemProperty.KEY)) {
            this.researchItem = researchItem;
            this.researchItem.setCount(1);
            return true;
        }
        return false;
    }

    public AssemblyLineRecipeBuilder researchItem(ItemStack researchItem, int voltage, int duration) {
        this.scanningVoltage = voltage;
        this.scanningDuration = duration;
        return researchItem(researchItem);
    }

    public AssemblyLineRecipeBuilder researchItem(MetaItem<?>.MetaValueItem researchItem, int voltage, int duration) {
        return researchItem(researchItem.getStackForm(), voltage, duration);
    }

    public AssemblyLineRecipeBuilder researchItem(ItemStack researchItem) {
        this.researchItem = researchItem;
        return this;
    }

    public AssemblyLineRecipeBuilder researchItem(MetaItem<?>.MetaValueItem researchItem) {
        return researchItem(researchItem.getStackForm());
    }

    @Override
    public AssemblyLineRecipeBuilder copy() {
        return new AssemblyLineRecipeBuilder(this);
    }

    public ValidationResult<Recipe> build() {
        Recipe recipe = new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden);
        if (this.researchItem != null)
            recipe.setProperty(ResearchItemProperty.getInstance(), this.researchItem);
        return ValidationResult.newResult(finalizeAndValidate(), recipe);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ResearchItemProperty.getInstance().getKey(), researchItem)
                .toString();
    }

    public static NBTTagCompound generateResearchNBT(RecipeBuilder<?> recipeBuilder) {
        NBTTagCompound compound = new NBTTagCompound();
        if (!recipeBuilder.getOutputs().isEmpty()) {
            compound.setTag(RESEARCH_NBT_TAG_NAME, recipeBuilder.getOutputs().get(0).serializeNBT());
        }
        return compound;
    }

    public ItemStack getResearchItem() {
        return researchItem;
    }

    public int getScanningVoltage() {
        return scanningVoltage;
    }

    public int getScanningDuration() {
        return scanningDuration;
    }
}
