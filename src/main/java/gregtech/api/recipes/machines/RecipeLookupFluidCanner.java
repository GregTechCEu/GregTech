package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.RecipeLookup;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.machines.util.ExactMatchFluidIngredient;
import gregtech.api.recipes.machines.util.ExactMatchItemIngredient;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RecipeLookupFluidCanner extends RecipeLookup {

    public static final int VOLTAGE = 4;
    public static final int MAX_DURATION = 16;
    public static final int AMOUNT_FACTOR = 64;

    private final RecipeBuilder<?> builder;

    public static <T extends RecipeBuilder<T>> RecipeMap<T> createMap(@NotNull String unlocalizedName,
                                                                      @NotNull T defaultRecipeBuilder,
                                                                      @NotNull RecipeMapUIFunction recipeMapUI) {
        return new RecipeMap<>(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 2, 2, 1, 1,
                new RecipeLookupFluidCanner(defaultRecipeBuilder)).setSound(GTSoundEvents.BATH);
    }

    public RecipeLookupFluidCanner(RecipeBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public @NotNull CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                            @NotNull List<FluidStack> fluids,
                                                            @Nullable PropertySet properties) {
        return new CompactibleCannerIterator(super.findRecipes(items, fluids, properties), items, fluids);
    }

    @Nullable
    public static Recipe attemptFillingRecipe(RecipeBuilder<?> builderPrototype, ItemStack containerIn,
                                              FluidStack fluidIn) {
        IFluidHandlerItem handler = containerIn.copy()
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) return null;
        int filled = handler.fill(fluidIn, true);
        if (filled <= 0) return null;
        return builderPrototype.copy()
                .ingredient(new ExactMatchItemIngredient(containerIn, 1))
                .ingredient(new ExactMatchFluidIngredient(fluidIn, filled))
                .outputs(handler.getContainer())
                .duration(Math.max(MAX_DURATION, fluidIn.amount / AMOUNT_FACTOR)).volts(VOLTAGE).build().getResult();
    }

    @Nullable
    public static Recipe attemptDrainingRecipe(RecipeBuilder<?> builderPrototype, ItemStack containerIn) {
        IFluidHandlerItem handler = containerIn.copy()
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) return null;
        FluidStack drain = handler.drain(Integer.MAX_VALUE, true);
        if (drain == null || drain.amount <= 0) return null;
        return builderPrototype.copy()
                .ingredient(new ExactMatchItemIngredient(containerIn, 1))
                .fluidOutputs(drain)
                .outputs(handler.getContainer())
                .duration(Math.max(MAX_DURATION, drain.amount / AMOUNT_FACTOR)).volts(VOLTAGE).build().getResult();
    }

    private final class CompactibleCannerIterator implements CompactibleIterator<Recipe> {

        private final CompactibleIterator<Recipe> backer;
        private final List<ItemStack> containerCandidates;
        private final List<FluidStack> fluids;
        private int pointer;

        private @Nullable Iterator<Recipe> top;

        public CompactibleCannerIterator(CompactibleIterator<Recipe> backer, List<ItemStack> containerCandidates,
                                         List<FluidStack> fluids) {
            this.backer = backer;
            this.containerCandidates = containerCandidates;
            this.fluids = fluids;
        }

        @Override
        public @NotNull Iterator<Recipe> compact() {
            List<ItemStack> containers = new ObjectArrayList<>();
            for (int i = pointer; i < containerCandidates.size(); i++) {
                ItemStack stack = containers.get(i);
                if (stack.getCount() > 0 &&
                        stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null) {
                    containers.add(stack);
                }
            }
            return new CannerIterator(backer.compact(), containers, fluids, top);
        }

        @Override
        public boolean hasNext() {
            if (backer.hasNext()) return true;
            if (top != null && top.hasNext()) return true;
            while (pointer != containerCandidates.size() && (top == null || !top.hasNext())) {
                List<Recipe> list = computeRecipes(containerCandidates.get(pointer), fluids);
                if (!list.isEmpty()) top = list.iterator();
                pointer++;
            }
            return top != null && top.hasNext();
        }

        @Override
        public Recipe next() {
            if (backer.hasNext()) return backer.next();
            if (top != null && top.hasNext()) return top.next();
            if (!hasNext()) throw new NoSuchElementException();
            assert top != null;
            return top.next();
        }
    }

    private final class CannerIterator implements Iterator<Recipe> {

        private final Iterator<Recipe> backer;
        private final List<ItemStack> containers;
        private final List<FluidStack> fluids;
        private int pointer;

        private @Nullable Iterator<Recipe> top;

        public CannerIterator(Iterator<Recipe> backer, List<ItemStack> containers, List<FluidStack> fluids,
                              @Nullable Iterator<Recipe> top) {
            this.backer = backer;
            this.containers = containers;
            this.fluids = fluids;
            this.top = top;
        }

        @Override
        public boolean hasNext() {
            if (backer.hasNext()) return true;
            if (top != null && top.hasNext()) return true;
            while (pointer != containers.size() && (top == null || !top.hasNext())) {
                List<Recipe> list = computeRecipes(containers.get(pointer), fluids);
                if (!list.isEmpty()) top = list.iterator();
                pointer++;
            }
            return top != null && top.hasNext();
        }

        @Override
        public Recipe next() {
            if (backer.hasNext()) return backer.next();
            if (top != null && top.hasNext()) return top.next();
            if (!hasNext()) throw new NoSuchElementException();
            assert top != null;
            return top.next();
        }
    }

    private List<Recipe> computeRecipes(ItemStack next, List<FluidStack> fluids) {
        List<Recipe> list = new ObjectArrayList<>(1 + fluids.size());
        Recipe drain = attemptDrainingRecipe(builder, next);
        if (drain != null) list.add(drain);
        fluids.stream().map(f -> attemptFillingRecipe(builder, next, f)).filter(Objects::nonNull).forEach(list::add);
        return list;
    }
}
