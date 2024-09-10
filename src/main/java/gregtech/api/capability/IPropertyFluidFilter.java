package gregtech.api.capability;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;

/**
 * Fluid filter based on fluid properties; i.e. temperature, fluid state, and various material flags such as acid
 * and plasma.
 *
 * @see FluidAttribute
 * @see gregtech.api.fluids.attribute.FluidAttributes
 * @see AttributedFluid
 */
public interface IPropertyFluidFilter extends IFilter<FluidStack> {

    @Override
    default boolean test(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        if (fluid.getTemperature() < getMinFluidTemperature()) return false;

        FluidState state = FluidState.inferState(stack);
        if (!canContain(state)) return false;

        if (fluid instanceof AttributedFluid attributedFluid) {
            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!canContain(attribute)) {
                    return false;
                }
            }
        }

        // plasma ignores temperature requirements
        if (state == FluidState.PLASMA) return true;
        return fluid.getTemperature() <= getMaxFluidTemperature();
    }

    @Override
    default int getPriority() {
        return IFilter.blacklistLikePriority();
    }

    /**
     * @param state the state to check
     * @return if the state can be contained
     */
    boolean canContain(@NotNull FluidState state);

    /**
     * @param attribute the attribute to check
     * @return if the attribute can be contained
     */
    boolean canContain(@NotNull FluidAttribute attribute);

    @NotNull
    @UnmodifiableView
    Collection<@NotNull FluidAttribute> getContainedAttributes();

    /**
     * Append tooltips about containment info
     *
     * @param tooltip the tooltip to append to
     */
    default void appendTooltips(@NotNull List<String> tooltip) {
        tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", getMaxFluidTemperature()));
        tooltip.add(I18n.format("gregtech.fluid_pipe.min_temperature", getMinFluidTemperature()));
        if (isGasProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
        else tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof"));
        if (isPlasmaProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
        getContainedAttributes().forEach(a -> a.appendContainerTooltips(tooltip));
    }

    /**
     * This is always checked, regardless of the contained fluid being a {@link AttributedFluid} or not
     *
     * @return the maximum allowed temperature for a fluid
     */
    int getMaxFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link AttributedFluid} or not
     *
     * @return the minimum allowed temperature for a fluid
     */
    int getMinFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link AttributedFluid} or not
     *
     * @return whether this filter allows gases
     */
    default boolean isGasProof() {
        return canContain(FluidState.GAS);
    }

    /**
     * @return whether this filter allows plasmas
     */
    default boolean isPlasmaProof() {
        return canContain(FluidState.PLASMA);
    }
}
