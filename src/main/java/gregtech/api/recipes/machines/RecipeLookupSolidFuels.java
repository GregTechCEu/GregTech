package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.RecipeLookup;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.machines.util.ExactMatchItemIngredient;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class RecipeLookupSolidFuels extends RecipeLookup {

    public static final int VOLTAGE = 4;

    private final RecipeBuilder<?> fuelBuilder;

    public static <T extends RecipeBuilder<T>> RecipeMapBuilder<T> mapBuilder(@NotNull String unlocalizedName,
                                                                              @NotNull T defaultRecipeBuilder) {
        return new RecipeMapBuilder<>(unlocalizedName, defaultRecipeBuilder)
                .lookup(new RecipeLookupSolidFuels(defaultRecipeBuilder));
    }

    public RecipeLookupSolidFuels(RecipeBuilder<?> fuelBuilder) {
        this.fuelBuilder = fuelBuilder;
    }

    @Override
    public @NotNull CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                            @NotNull List<FluidStack> fluids,
                                                            @Nullable PropertySet properties) {
        return new CompactibleFuelIterator(super.findRecipes(items, fluids, properties), items);
    }

    @Nullable
    public static Recipe attemptRecipe(RecipeBuilder<?> builderPrototype, ItemStack burnable) {
        int burntime = TileEntityFurnace.getItemBurnTime(burnable);
        if (burntime <= 0) return null;
        RecipeBuilder<?> builder = builderPrototype.copy()
                .ingredient(new ExactMatchItemIngredient(burnable, 1));
        ItemStack output = burnable.getItem().getContainerItem(burnable);
        if (output.getCount() > 0) {
            builder.outputs(output);
        }
        return builder.duration(burntime).volts(VOLTAGE).build().getResult();
    }

    private final class CompactibleFuelIterator implements CompactibleIterator<Recipe> {

        private final CompactibleIterator<Recipe> backer;
        private final List<ItemStack> burnableCandidates;
        private int pointer;
        private @Nullable ItemStack next;

        public CompactibleFuelIterator(CompactibleIterator<Recipe> backer, List<ItemStack> burnableCandidates) {
            this.backer = backer;
            this.burnableCandidates = burnableCandidates;
        }

        @Override
        public @NotNull Iterator<Recipe> compact() {
            List<ItemStack> burnables = new ObjectArrayList<>();
            for (int i = pointer; i < burnableCandidates.size(); i++) {
                ItemStack stack = burnableCandidates.get(i);
                if (stack.getCount() > 0 && TileEntityFurnace.isItemFuel(stack)) {
                    burnables.add(stack);
                }
            }
            return new FuelIterator(backer.compact(), burnables);
        }

        @Override
        public boolean hasNext() {
            if (backer.hasNext()) return true;
            next = null;
            while (pointer != burnableCandidates.size() && (next == null || !TileEntityFurnace.isItemFuel(next))) {
                next = burnableCandidates.get(pointer);
                pointer++;
            }
            return next != null;
        }

        @Override
        public Recipe next() {
            if (backer.hasNext()) return backer.next();
            if (!hasNext()) throw new NoSuchElementException();
            assert next != null;
            Recipe recipe = attemptRecipe(fuelBuilder, next);
            next = null;
            return recipe;
        }
    }

    private final class FuelIterator implements Iterator<Recipe> {

        private final Iterator<Recipe> backer;
        private final List<ItemStack> burnables;
        private int pointer;
        private @Nullable ItemStack next;

        public FuelIterator(Iterator<Recipe> backer, List<ItemStack> burnables) {
            this.backer = backer;
            this.burnables = burnables;
        }

        @Override
        public boolean hasNext() {
            if (backer.hasNext()) return true;
            if (pointer == burnables.size()) return false;
            next = burnables.get(pointer);
            pointer++;
            return true;
        }

        @Override
        public Recipe next() {
            if (backer.hasNext()) return backer.next();
            if (!hasNext()) throw new NoSuchElementException();
            assert next != null;
            return attemptRecipe(fuelBuilder, next);
        }
    }
}