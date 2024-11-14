package gregtech.api.recipes.machines.util;

import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import net.minecraft.item.ItemStack;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Collections;

@Desugar
public record ExactMatchItemIngredient(ItemStack stack, int count) implements GTItemIngredient {

    public ExactMatchItemIngredient(@NotNull ItemStack stack, int count) {
        this.stack = stack;
        this.count = count;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ItemStack.areItemStacksEqual(this.stack, stack);
    }

    @Override
    public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
        return count;
    }

    @Override
    public @NotNull Collection<ItemStack> getMatchingStacksWithinContext(
                                                                         @NotNull ItemStackMatchingContext context) {
        if (context == ItemStackMatchingContext.ITEM_DAMAGE_NBT) return Collections.singletonList(stack);
        return Collections.emptyList();
    }

    @Override
    public @Nullable NBTMatcher getMatcher() {
        return null;
    }
}
