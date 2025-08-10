package gregtech.mixins.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;

import mezz.jei.util.ErrorUtil;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemStackHelper.class, remap = false)
public abstract class ItemStackHelperMixin implements IIngredientHelper<ItemStack> {

    @Override
    public @NotNull String getDisplayName(ItemStack ingredient) {
        return ingredient.getDisplayName();
    }

}
