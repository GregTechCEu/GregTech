package gregtech.api.recipes.builders;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.ResearchItemProperty;
import gregtech.api.util.ValidationResult;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AssemblyLineRecipeBuilder extends RecipeBuilder<AssemblyLineRecipeBuilder> {

    protected ItemStack researchItem;

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

    public AssemblyLineRecipeBuilder researchItem (ItemStack researchItem) {
        this.researchItem = researchItem;
        return this;
    }

    public AssemblyLineRecipeBuilder researchItem(MetaItem<?>.MetaValueItem researchItem) {
        return researchItem(researchItem.getStackForm(1));
    }

    @Override
    public AssemblyLineRecipeBuilder copy() {
        return new AssemblyLineRecipeBuilder(this);
    }

    public ValidationResult<Recipe> build() {

        //TODO: make stick needed for assline completion

        Recipe recipe = new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
                duration, EUt, hidden);

        if (researchItem != null && !researchItem.isEmpty()) {
            NBTTagCompound compound = generateResearchNBT(recipe);
            ItemStack stickStack = MetaItems.TOOL_DATA_STICK.getStackForm(1);
            stickStack.setTagCompound(compound);

            RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(researchItem)
                    .input(MetaItems.TOOL_DATA_STICK)
                    .outputs(stickStack)
                    .EUt(1920)
                    .duration(40)
                    .buildAndRegister();
        }

        return ValidationResult.newResult(finalizeAndValidate(), recipe);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ResearchItemProperty.getInstance().getKey(), researchItem)
                .toString();
    }

    public static NBTTagCompound generateResearchNBT(Recipe recipe) {
        NBTTagCompound compound = new NBTTagCompound();
        if (!recipe.getOutputs().isEmpty()) {
            compound.setTag("asslineOutput", recipe.getOutputs().get(0).serializeNBT());
        }
        return compound;
    }
}
