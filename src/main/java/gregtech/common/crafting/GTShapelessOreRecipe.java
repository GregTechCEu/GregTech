package gregtech.common.crafting;

import com.google.common.collect.Lists;

import gregtech.api.crafting.IToolbeltSupportingRecipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GTShapelessOreRecipe extends ShapelessOreRecipe implements IToolbeltSupportingRecipe {

    boolean isClearing;
    boolean toolbeltHandling;

    public GTShapelessOreRecipe(boolean isClearing, ResourceLocation group, @NotNull ItemStack result,
                                Object... recipe) {
        super(group, result);
        initNeedsToolbeltHandlingHelper.set(false);

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
        this.toolbeltHandling = initNeedsToolbeltHandlingHelper.get();
        initNeedsToolbeltHandlingHelper.set(false);
    }

    @Override
    public boolean matches(@NotNull InventoryCrafting inv, @NotNull World world) {
        if (this.toolbeltHandling) {
            // I can't wrap my head around the 'simple' shapeless logic, so no simple toolbelt handling.
            int ingredientCount = 0;
            List<ItemStack> items = Lists.newArrayList();

            for (int i = 0; i < inv.getSizeInventory(); ++i)
            {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (!itemstack.isEmpty())
                {
                    ++ingredientCount;
                    items.add(itemstack);
                }
            }

            if (ingredientCount != this.input.size())
                return false;

            int[] matches = RecipeMatcher.findMatches(items, this.input);
            if (matches != null) {
                for (int i = 0; i < items.size(); i++) {
                    ItemStack stack = items.get(i);
                    Ingredient ingredient = this.input.get(matches[i]);
                    if (!IToolbeltSupportingRecipe.toolbeltIngredientCheck(ingredient, stack)) return false;
                }
                return true;
            } else return false;

        } else return super.matches(inv, world);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        if (isClearing) {
            return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        } else {
            return IToolbeltSupportingRecipe.super.getRemainingItems(inv);
        }
    }
}
