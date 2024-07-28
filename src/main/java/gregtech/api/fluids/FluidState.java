package gregtech.api.fluids;

import gregtech.api.fluids.attribute.AttributedFluid;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public enum FluidState {

    LIQUID("gregtech.fluid.state_liquid"),
    GAS("gregtech.fluid.state_gas"),
    PLASMA("gregtech.fluid.state_plasma");

    private final String translationKey;

    FluidState(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    public @NotNull String getTranslationKey() {
        return this.translationKey;
    }

    public static FluidState inferState(FluidStack stack) {
        if (stack.getFluid() instanceof AttributedFluid fluid) return fluid.getState();
        else return stack.getFluid().isGaseous(stack) ? GAS : LIQUID;
    }
}
