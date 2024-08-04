package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of ItemStacks when
 * considering equality.
 */
public interface FluidStackHashStrategy extends Hash.Strategy<FluidStack> {

    /**
     * @return a builder object for producing a custom ItemStackHashStrategy.
     */
    static FluidStackHashStrategyBuilder builder() {
        return new FluidStackHashStrategyBuilder();
    }

    /**
     * Generates an ItemStackHash configured to compare every aspect of ItemStacks.
     *
     * @return the ItemStackHashStrategy as described above.
     */
    static FluidStackHashStrategy comparingAll() {
        return builder().compareFluid(true)
                .compareCount(true)
                .compareTag(true)
                .build();
    }

    /**
     * Generates an ItemStackHash configured to compare every aspect of ItemStacks except the number
     * of items in the stack.
     *
     * @return the ItemStackHashStrategy as described above.
     */
    static FluidStackHashStrategy comparingAllButCount() {
        return builder().compareFluid(true)
                .compareTag(true)
                .build();
    }

    static FluidStackHashStrategy comparingItemDamageCount() {
        return builder().compareFluid(true)
                .compareCount(true)
                .build();
    }

    /**
     * Builder pattern class for generating customized ItemStackHashStrategy
     */
    class FluidStackHashStrategyBuilder {

        private boolean item, count, tag;

        /**
         * Defines whether the Item type should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategyBuilder compareFluid(boolean choice) {
            item = choice;
            return this;
        }

        /**
         * Defines whether stack size should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategyBuilder compareCount(boolean choice) {
            count = choice;
            return this;
        }

        /**
         * Defines whether NBT Tags should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategyBuilder compareTag(boolean choice) {
            tag = choice;
            return this;
        }

        /**
         * @return the ItemStackHashStrategy as configured by "compare" methods.
         */
        public FluidStackHashStrategy build() {
            return new FluidStackHashStrategy() {

                @Override
                public int hashCode(@Nullable FluidStack o) {
                    return o == null || o.amount == 0 ? 0 : Objects.hash(
                            item ? o.getFluid() : null,
                            count ? o.amount : null,
                            tag ? o.tag : null);
                }

                @Override
                public boolean equals(@Nullable FluidStack a, @Nullable FluidStack b) {
                    if (a == null || a.amount == 0) return b == null || b.amount == 0;
                    if (b == null || b.amount == 0) return false;

                    return (!item || a.getFluid() == b.getFluid()) &&
                            (!count || a.amount == b.amount) &&
                            (!tag || Objects.equals(a.tag, b.tag));
                }
            };
        }
    }
}
