package gregtech.api.recipes.crafttweaker;


import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.recipes.CountableIngredient;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;

@ZenExpansion("mods.gregtech.recipe.CountableIngredient")
@ZenRegister
public class CountableIngredientExpansion{

    @ZenCaster
    public static IItemStack asIItemStack(CountableIngredient ingredient) {
        return CraftTweakerMC.getIItemStack(ingredient.getIngredient().getMatchingStacks()[0]);
    }
}
