package gregtech.api.recipes.ingredients;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nullable;

public class NBTIngredient extends Ingredient {

    private final ItemStack stack;

    public NBTIngredient(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    @Override
    public boolean apply(@Nullable ItemStack input) {
        if (input == null)
            return false;
        return this.stack.getItem() == input.getItem() &&
                this.stack.getItemDamage() == input.getItemDamage() &&
                ItemStack.areItemStackShareTagsEqual(this.stack, input);
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
