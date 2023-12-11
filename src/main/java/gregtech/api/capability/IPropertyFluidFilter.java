package gregtech.api.capability;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;

import static gregtech.api.fluids.FluidConstants.CRYOGENIC_FLUID_THRESHOLD;

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
        if (fluid.getTemperature() < CRYOGENIC_FLUID_THRESHOLD && !isCryoProof()) return false;

        if (fluid instanceof AttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (!canContain(state)) return false;

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!canContain(attribute)) {
                    return false;
                }
            }

            // plasma ignores temperature requirements
            if (state == FluidState.PLASMA) return true;
        } else {
            if (fluid.isGaseous() && !canContain(FluidState.GAS)) {
                return false;
            }
            if (!canContain(FluidState.LIQUID)) {
                return false;
            }
        }

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

    /**
     * Set the container as able to contain an attribute
     *
     * @param attribute  the attribute to change containment status for
     * @param canContain whether the attribute can be contained
     */
    void setCanContain(@NotNull FluidAttribute attribute, boolean canContain);

    @NotNull
    @UnmodifiableView
    Collection<@NotNull FluidAttribute> getContainedAttributes();

    /**
     * Append tooltips about containment info
     *
     * @param tooltip             the tooltip to append to
     * @param showToolsInfo       if the "hold shift" line should mention tool info
     * @param showTemperatureInfo if the temperature information should be displayed
     */
    default void appendTooltips(@NotNull List<String> tooltip, boolean showToolsInfo, boolean showTemperatureInfo) {
        if (TooltipHelper.isShiftDown()) {
            if (showTemperatureInfo)
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", getMaxFluidTemperature()));
            if (isGasProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
            else tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof"));
            if (isPlasmaProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
            if (isCryoProof()) tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
            getContainedAttributes().forEach(a -> a.appendContainerTooltips(tooltip));
        } else if (isGasProof() || isCryoProof() || isPlasmaProof() || !getContainedAttributes().isEmpty()) {
            if (showToolsInfo) {
                tooltip.add(I18n.format("gregtech.tooltip.tool_fluid_hold_shift"));
            } else {
                tooltip.add(I18n.format("gregtech.tooltip.fluid_pipe_hold_shift"));
            }
        }
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
     * @return whether this filter allows gases
     */
    boolean isGasProof();

    /**
     * @return whether this filter allows cryogenic fluids
     */
    boolean isCryoProof();

    /**
     * @return whether this filter allows plasmas
     */
    boolean isPlasmaProof();
}
