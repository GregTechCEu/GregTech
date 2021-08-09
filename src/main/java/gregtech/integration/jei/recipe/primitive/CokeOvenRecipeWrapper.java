package gregtech.integration.jei.recipe.primitive;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.recipes.CokeOvenRecipe;
import gregtech.api.unification.OreDictUnifier;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CokeOvenRecipeWrapper implements IRecipeWrapper {

    private final CokeOvenRecipe recipe;
    private final List<List<ItemStack>> matchingInputs = new ArrayList<>();
    private final List<ItemStack> outputs = new ArrayList<>();
    private final List<FluidStack> fluidOutputs = new ArrayList<>();

    public CokeOvenRecipeWrapper(CokeOvenRecipe recipe) {
        this.recipe = recipe;
        CountableIngredient ingredient = recipe.getInput();

        List<ItemStack> ingredientValues = Arrays.stream(ingredient.getIngredient().getMatchingStacks())
                .map(ItemStack::copy)
                .sorted(OreDictUnifier.getItemStackComparator())
                .collect(Collectors.toList());
        ingredientValues.forEach(stack -> stack.setCount(ingredient.getCount()));

        this.matchingInputs.add(ingredientValues);
        this.outputs.add(recipe.getOutput());
        this.fluidOutputs.add(recipe.getFluidOutput());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, this.matchingInputs);
        ingredients.setOutputs(VanillaTypes.ITEM, this.outputs);
        ingredients.setOutputs(VanillaTypes.FLUID, this.fluidOutputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.duration", this.recipe.getDuration() / 20f), 0, 60, 0x111111);
    }

}
