package gregtech.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.jetbrains.annotations.NotNull;

public class GTShapelessOreRecipe extends ShapelessOreRecipe {

    boolean isClearing;

    public GTShapelessOreRecipe(boolean isClearing, ResourceLocation group, @NotNull ItemStack result,
                                Object... recipe) {
        super(group, result);
        this.isClearing = isClearing;
        for (Object in : recipe) {
            Ingredient ing = GTShapedOreRecipe.getIngredient(isClearing, in);
            if (ing != null) {
                input.add(ing);
                this.isSimple = this.isSimple && ing.isSimple();
            } else {
                StringBuilder ret = new StringBuilder("Invalid shapeless ore recipe: ");
                for (Object tmp : recipe) {
                    ret.append(tmp).append(", ");
                }
                ret.append(output);
                throw new RuntimeException(ret.toString());
            }
        }
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        if (isClearing) {
            return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        } else {
            return super.getRemainingItems(inv);
        }
    }
}
