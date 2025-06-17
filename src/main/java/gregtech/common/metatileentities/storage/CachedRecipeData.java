package gregtech.common.metatileentities.storage;

import gregtech.api.util.GTLog;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CachedRecipeData {

    private IRecipe recipe;
    private IRecipe previousRecipe;
    private final List<Ingredient> recipeIngredients = new ArrayList<>();

    public CachedRecipeData() {
        this(null);
    }

    public CachedRecipeData(@Nullable IRecipe recipe) {
        this.recipe = recipe;
    }

    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        if (recipe == null) {
            return false;
        }
        return recipe.matches(inventoryCrafting, world);
    }

    public void setRecipe(IRecipe newRecipe) {
        this.previousRecipe = this.recipe;
        this.recipe = newRecipe;
        this.recipeIngredients.clear();
        if (newRecipe != null) {
            this.recipeIngredients.addAll(newRecipe.getIngredients());
            this.recipeIngredients.removeIf(ing -> ing == Ingredient.EMPTY);
        }
    }

    public IRecipe getRecipe() {
        return recipe;
    }

    public IRecipe getPreviousRecipe() {
        return previousRecipe;
    }

    public boolean canIngredientApply(int index, ItemStack stack) {
        if (this.recipeIngredients.isEmpty()) return false;
        if (index < 0 || index >= this.recipeIngredients.size()) {
            GTLog.logger.warn("Compacted index \"{}\" is out of bounds for list size \"{}\"", index,
                    this.recipeIngredients.size());
            return false;
        }
        return this.recipeIngredients.get(index).apply(stack);
    }
}
