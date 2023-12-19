package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.common.ConfigHolder;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

/**
 * Common fluid filter implementations.
 */
public enum CommonFluidFilters implements IFilter<FluidStack> {

    ALLOW_ALL {

        @Override
        public boolean test(@NotNull FluidStack fluid) {
            return true;
        }

        @Override
        public int getPriority() {
            return IFilter.noPriority();
        }

        @Override
        public IFilter<FluidStack> negate() {
            return DISALLOW_ALL;
        }
    },
    DISALLOW_ALL {

        @Override
        public boolean test(@NotNull FluidStack fluid) {
            return false;
        }

        @Override
        public int getPriority() {
            return IFilter.noPriority();
        }

        @Override
        public IFilter<FluidStack> negate() {
            return ALLOW_ALL;
        }
    },
    BOILER_FLUID {

        @Override
        public boolean test(@NotNull FluidStack fluid) {
            if (matchesFluid(fluid, FluidRegistry.WATER) || matchesFluid(fluid, Materials.DistilledWater)) {
                return true;
            }

            for (String fluidName : ConfigHolder.machines.boilerFluids) {
                Fluid boilerFluid = FluidRegistry.getFluid(fluidName);
                if (boilerFluid != null && matchesFluid(fluid, boilerFluid)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistLikePriority();
        }
    },
    STEAM {

        @Override
        public boolean test(@NotNull FluidStack fluid) {
            return matchesFluid(fluid, Materials.Steam);
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistPriority(1);
        }
    },
    LIGHTER_FUEL {

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            return matchesFluid(fluidStack, Materials.Butane) || matchesFluid(fluidStack, Materials.Propane);
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistPriority(2);
        }
    };

    /**
     * Comparison logic identical to {@link FluidStack#isFluidEqual}, without instantiation of FluidStack instance
     *
     * @param fluidStack    fluid stack
     * @param fluidMaterial material with fluid
     * @return whether the fluid in fluid stack and fluid associated with the material are equal
     */
    public static boolean matchesFluid(@NotNull FluidStack fluidStack, @NotNull Material fluidMaterial) {
        return fluidStack.tag == null && fluidStack.getFluid() == fluidMaterial.getFluid();
    }

    /**
     * Comparison logic identical to {@link FluidStack#isFluidEqual}, without instantiation of FluidStack instance
     *
     * @param fluidStack fluid stack
     * @param fluid      fluid
     * @return whether the fluid in fluid stack and fluid parameter are equal
     */
    public static boolean matchesFluid(@NotNull FluidStack fluidStack, @NotNull Fluid fluid) {
        return fluidStack.tag == null && fluidStack.getFluid() == fluid;
    }
}
