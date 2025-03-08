package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;

import java.util.Objects;

public interface FluidStackHashStrategy extends Hash.Strategy<FluidStack> {

    static FluidStackHashStrategyBuilder builder() {
        return new FluidStackHashStrategyBuilder();
    }

    static FluidStackHashStrategy comparingAll() {
        return builder().compareFluid(true)
                .compareAmount(true)
                .compareNBT(true)
                .build();
    }

    static FluidStackHashStrategy comparingAllButAmount() {
        return builder().compareFluid(true)
                .compareNBT(true)
                .build();
    }

    class FluidStackHashStrategyBuilder {

        private boolean fluid, amount, nbt;

        public FluidStackHashStrategyBuilder compareFluid(boolean choice) {
            this.fluid = choice;
            return this;
        }

        public FluidStackHashStrategyBuilder compareAmount(boolean choice) {
            this.amount = choice;
            return this;
        }

        public FluidStackHashStrategyBuilder compareNBT(boolean choice) {
            this.nbt = choice;
            return this;
        }

        public FluidStackHashStrategy build() {
            return new FluidStackHashStrategy() {

                @Override
                public int hashCode(FluidStack other) {
                    return other == null ? 0 : Objects.hash(
                            fluid ? other.getFluid() : null,
                            amount ? other.amount : null,
                            nbt ? other.tag : null);
                }

                @Override
                public boolean equals(FluidStack a, FluidStack b) {
                    if (a == null) return b == null;
                    if (b == null) return false;

                    return (!fluid || a.getFluid() == b.getFluid()) &&
                            (!amount || a.amount == b.amount) &&
                            (!nbt || Objects.equals(a.tag, b.tag));
                }
            };
        }
    }
}
