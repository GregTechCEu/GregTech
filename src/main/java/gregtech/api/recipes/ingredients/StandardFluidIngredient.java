package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.FluidStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;
import gregtech.api.recipes.lookup.flag.ItemStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Set;

public final class StandardFluidIngredient implements IFluidIngredient {

    private final FluidIngredientBacker backer;
    private final @Nullable NBTMatcher matcher;
    private final long count;

    StandardFluidIngredient(@NotNull StandardFluidIngredient.FluidIngredientBacker backer, @Nullable NBTMatcher matcher,
                                   @Range(from = 1, to = Long.MAX_VALUE) long count) {
        this.backer = backer;
        this.matcher = matcher;
        this.count = count;
    }

    @Override
    public @Nullable Collection<FluidStack> getMatchingStacksWithinContext(@NotNull FluidStackMatchingContext context) {
        return backer.getMatchingStacksWithinContext(context);
    }

    @Override
    public @Nullable NBTMatcher getMatcher() {
        return matcher;
    }

    @Override
    public boolean matches(FluidStack stack) {
        return (matcher == null || matcher.matches(stack)) && backer.matches(stack);
    }

    @Override
    public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardFluidIngredient that = (StandardFluidIngredient) o;
        return Objects.equals(backer, that.backer) && Objects.equals(matcher, that.matcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backer, matcher);
    }

    public static class FluidIngredientBacker {

        protected final @NotNull EnumMap<FluidStackMatchingContext, Collection<FluidStack>> matching;

        protected FluidIngredientBacker(@NotNull EnumMap<FluidStackMatchingContext, Collection<FluidStack>> matching) {
            this.matching = matching;
        }

        public @Nullable Collection<FluidStack> getMatchingStacksWithinContext(
                @NotNull FluidStackMatchingContext context) {
            return matching.getOrDefault(context, Collections.emptyList());
        }

        public boolean matches(FluidStack stack) {
            for (var matchInformation : matching.entrySet()) {
                FluidStackMatchingContext context = matchInformation.getKey();
                for (FluidStack match : matchInformation.getValue()) {
                    if (match.getFluid() == stack.getFluid() &&
                            (!context.matchesNBT() || Objects.equals(match.tag, stack.tag))) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FluidIngredientBacker that = (FluidIngredientBacker) o;
            return Objects.equals(matching, that.matching);
        }

        @Override
        public int hashCode() {
            return Objects.hash(matching);
        }
    }

    @Contract(" -> new")
    public static @NotNull FluidIngredientBuilder builder() {
        return new FluidIngredientBuilder();
    }

    public static class FluidIngredientBuilder {

        private final EnumMap<FluidStackMatchingContext, Collection<FluidStack>> matching =
                new EnumMap<>(FluidStackMatchingContext.class);

        private Set<FluidStack> stacks = new ObjectOpenCustomHashSet<>(FluidStackApplicatorMap.FLUID_NBT);
        private @Nullable NBTMatcher matcher = null;

        private long count;

        public FluidIngredientBuilder setCount(long count) {
            this.count = count;
            return this;
        }

        public FluidIngredientBuilder setMatcher(NBTMatcher matcher) {
            this.matcher = matcher;
            return this;
        }

        public FluidIngredientBuilder addFluid(FluidStack fluid) {
            stacks.add(fluid);
            return this;
        }

        public FluidIngredientBuilder addFluid(Fluid fluid) {
            stacks.add(new FluidStack(fluid, 1));
            return this;
        }

        public FluidIngredientBuilder addFluid(Fluid fluid, NBTTagCompound tag) {
            FluidStack stack = new FluidStack(fluid, 1);
            stack.tag = tag;
            stacks.add(stack);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public FluidIngredientBuilder clearToContext(@NotNull FluidStackMatchingContext context) {
            matching.put(context, stacks);
            stacks = new ObjectOpenHashSet<>();
            matcher = null;
            return this;
        }

        public StandardFluidIngredient clearToContextAndBuild(@NotNull FluidStackMatchingContext context) {
            clearToContext(context);
            return IngredientCache.getFluidIngredient(matching, count, matcher);
        }
    }
}
