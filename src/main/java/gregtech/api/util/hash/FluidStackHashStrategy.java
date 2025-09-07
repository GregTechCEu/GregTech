package gregtech.api.util.hash;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;

import java.util.Objects;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of
 * {@link FluidStack}s when considering equality.
 */
public interface FluidStackHashStrategy extends Hash.Strategy<FluidStack> {

    static Builder builder() {
        return new Builder();
    }

    FluidStackHashStrategy comparingAll = builder()
            .compareFluid()
            .compareAmount()
            .compareNBT()
            .build();

    FluidStackHashStrategy comparingAllButAmount = builder()
            .compareFluid()
            .compareNBT()
            .build();

    class Builder {

        private boolean fluid, amount, nbt = false;

        public Builder compareFluid() {
            this.fluid = true;
            return this;
        }

        public Builder compareAmount() {
            this.amount = true;
            return this;
        }

        public Builder compareNBT() {
            this.nbt = true;
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
