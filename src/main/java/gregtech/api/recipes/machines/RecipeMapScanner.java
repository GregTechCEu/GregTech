package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.RecipeLookup;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@ApiStatus.Internal
public class RecipeMapScanner extends RecipeMap<SimpleRecipeBuilder> implements IScannerRecipeMap {

    private static final List<ICustomScannerLogic> CUSTOM_SCANNER_LOGICS = new ArrayList<>();

    public RecipeMapScanner(@NotNull String unlocalizedName, @NotNull SimpleRecipeBuilder defaultRecipeBuilder,
                            @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 2, 1, 1, 0, new ScannerLookup());
        setSound(GTSoundEvents.ELECTROLYZER);
    }

    @Override
    public @NotNull List<Recipe> getRepresentativeRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        for (ICustomScannerLogic logic : CUSTOM_SCANNER_LOGICS) {
            List<Recipe> logicRecipes = logic.getRepresentativeRecipes();
            if (logicRecipes != null && !logicRecipes.isEmpty()) {
                recipes.addAll(logicRecipes);
            }
        }
        return recipes;
    }

    /**
     *
     * @param logic A function which is passed the normal findRecipe() result. Returns null if no valid recipe for
     *              the custom logic is found,
     */
    public static void registerCustomScannerLogic(ICustomScannerLogic logic) {
        CUSTOM_SCANNER_LOGICS.add(logic);
    }

    private static final class ScannerLookup extends RecipeLookup {

        @Override
        public @NotNull CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                                @NotNull List<FluidStack> fluids,
                                                                @Nullable PropertySet properties) {
            return new CompactibleScannerIterator(super.findRecipes(items, fluids, properties), properties, items,
                    fluids);
        }

        private static final class CompactibleScannerIterator implements CompactibleIterator<Recipe> {

            private final @Nullable PropertySet properties;
            private final @NotNull List<ItemStack> items;
            private final @NotNull List<FluidStack> fluids;

            private final CompactibleIterator<Recipe> backer;
            private int pointer;
            private @Nullable Recipe next;

            public CompactibleScannerIterator(CompactibleIterator<Recipe> backer, @Nullable PropertySet properties,
                                              @NotNull List<ItemStack> items,
                                              @NotNull List<FluidStack> fluids) {
                this.backer = backer;
                this.properties = properties;
                this.items = items;
                this.fluids = fluids;
            }

            @Override
            public @NotNull Iterator<Recipe> compact() {
                return new ScannerIterator(backer.compact(), pointer, properties, items, fluids);
            }

            @Override
            public boolean hasNext() {
                if (backer.hasNext()) return true;
                while (pointer != CUSTOM_SCANNER_LOGICS.size() && next == null) {
                    next = CUSTOM_SCANNER_LOGICS.get(pointer).createCustomRecipe(properties, items, fluids);
                    pointer++;
                }
                return next != null;
            }

            @Override
            public Recipe next() {
                if (backer.hasNext()) return backer.next();
                if (!hasNext()) throw new NoSuchElementException();
                Recipe recipe = next;
                next = null;
                return recipe;
            }
        }

        private static final class ScannerIterator implements Iterator<Recipe> {

            private final @Nullable PropertySet properties;
            private final @NotNull List<ItemStack> items;
            private final @NotNull List<FluidStack> fluids;

            private final Iterator<Recipe> backer;
            private int pointer;
            private @Nullable Recipe next;

            public ScannerIterator(Iterator<Recipe> backer, int pointer, @Nullable PropertySet properties,
                                   @NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids) {
                this.backer = backer;
                this.pointer = pointer;
                this.properties = properties;
                this.items = items;
                this.fluids = fluids;
            }

            @Override
            public boolean hasNext() {
                if (backer.hasNext()) return true;
                while (pointer != CUSTOM_SCANNER_LOGICS.size() && next == null) {
                    next = CUSTOM_SCANNER_LOGICS.get(pointer).createCustomRecipe(properties, items, fluids);
                    pointer++;
                }
                return next != null;
            }

            @Override
            public Recipe next() {
                if (backer.hasNext()) return backer.next();
                if (!hasNext()) throw new NoSuchElementException();
                assert next != null;
                Recipe recipe = next;
                next = null;
                return recipe;
            }
        }
    }
}
