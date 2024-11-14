package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.RecipeLookup;
import gregtech.api.recipes.lookup.property.PowerSupplyProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class RecipeLookupFormingPress extends RecipeLookup {

    public static final int VOLTAGE = 4;
    public static final int DURATION = 40;

    private static ItemStack NAME_MOLD = ItemStack.EMPTY;

    private final RecipeBuilder<?> builder;

    public static <T extends RecipeBuilder<T>> RecipeMap<T> createMap(@NotNull String unlocalizedName,
                                                                      @NotNull T defaultRecipeBuilder,
                                                                      @NotNull RecipeMapUIFunction recipeMapUI) {
        return new RecipeMap<>(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 6, 1, 0, 0,
                new RecipeLookupFormingPress(defaultRecipeBuilder)).setSound(GTSoundEvents.COMPRESSOR);
    }

    public RecipeLookupFormingPress(RecipeBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public @NotNull CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                            @NotNull List<FluidStack> fluids,
                                                            @Nullable PropertySet properties) {
        return new CompactiblePressIterator(super.findRecipes(items, fluids, properties),
                (properties == null || properties.getDefaultable(PowerSupplyProperty.EMPTY).voltage() >= VOLTAGE) ?
                        items : null);
    }

    @Nullable
    public static Recipe attemptRecipe(RecipeBuilder<?> builderPrototype, List<ItemStack> stacks) {
        // Item Mold renaming - min of 2 inputs required
        if (stacks.size() > 1) {
            // cache name mold target comparison stack so a new one is not made every lookup
            // cannot statically initialize as RecipeMaps are registered before items, throwing a NullPointer
            if (NAME_MOLD.isEmpty()) {
                NAME_MOLD = MetaItems.SHAPE_MOLD_NAME.getStackForm();
            }

            // find the mold and the stack to rename
            ItemStack moldStack = ItemStack.EMPTY;
            ItemStack itemStack = ItemStack.EMPTY;
            for (ItemStack inputStack : stacks) {
                // early exit
                if (!moldStack.isEmpty() && !itemStack.isEmpty()) break;

                if (moldStack.isEmpty() && inputStack.isItemEqual(NAME_MOLD)) {
                    // only valid if the name mold has a name, which is stored in the "display" sub-compound
                    if (inputStack.getTagCompound() != null &&
                            inputStack.getTagCompound().hasKey("display", Constants.NBT.TAG_COMPOUND)) {
                        moldStack = inputStack;
                    }
                } else if (itemStack.isEmpty()) {
                    itemStack = inputStack;
                }
            }

            // make the mold recipe if the two required inputs were found
            if (!moldStack.isEmpty() && moldStack.getTagCompound() != null && !itemStack.isEmpty()) {
                ItemStack output = GTUtility.copy(1, itemStack);
                output.setStackDisplayName(moldStack.getDisplayName());
                return builderPrototype.copy()
                        .notConsumable(moldStack) // recipe is reusable as long as mold stack matches
                        .inputs(GTUtility.copy(1, itemStack))
                        .outputs(output)
                        .duration(VOLTAGE).volts(DURATION)
                        .build().getResult();
            }
            return null;
        }
        return null;
    }

    private final class CompactiblePressIterator implements CompactibleIterator<Recipe> {

        private final CompactibleIterator<Recipe> backer;
        private @Nullable List<ItemStack> stacks;

        private @Nullable Recipe recipe;

        public CompactiblePressIterator(CompactibleIterator<Recipe> backer, @Nullable List<ItemStack> stacks) {
            this.backer = backer;
            this.stacks = stacks;
        }

        @Override
        public @NotNull Iterator<Recipe> compact() {
            return new PressIterator(backer.compact(), stacks, recipe);
        }

        @Override
        public boolean hasNext() {
            if (backer.hasNext()) return true;
            if (stacks != null) {
                recipe = attemptRecipe(builder, stacks);
                stacks = null;
            }
            return recipe != null;
        }

        @Override
        public Recipe next() {
            if (backer.hasNext()) return backer.next();
            if (!hasNext()) throw new NoSuchElementException();
            return recipe;
        }
    }

    private final class PressIterator implements Iterator<Recipe> {

        private final Iterator<Recipe> backer;
        private @Nullable List<ItemStack> stacks;

        private @Nullable Recipe recipe;

        public PressIterator(Iterator<Recipe> backer, @Nullable List<ItemStack> stacks, @Nullable Recipe recipe) {
            this.backer = backer;
            this.stacks = stacks;
            this.recipe = recipe;
        }

        @Override
        public boolean hasNext() {
            if (backer.hasNext()) return true;
            if (stacks != null) {
                recipe = attemptRecipe(builder, stacks);
                stacks = null;
            }
            return recipe != null;
        }

        @Override
        public Recipe next() {
            if (backer.hasNext()) return backer.next();
            if (!hasNext()) throw new NoSuchElementException();
            return recipe;
        }
    }
}
