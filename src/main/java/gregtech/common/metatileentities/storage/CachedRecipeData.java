package gregtech.common.metatileentities.storage;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class CachedRecipeData {

    private IRecipe recipe;

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
        this.recipe = newRecipe;
    }

    public IRecipe getRecipe() {
        return recipe;
    }

    public ItemStack getRecipeOutput() {
        return recipe == null ? ItemStack.EMPTY : recipe.getRecipeOutput();
    }
}
