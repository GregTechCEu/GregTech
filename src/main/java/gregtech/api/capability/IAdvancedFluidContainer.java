package gregtech.api.capability;

import gregtech.api.fluids.fluid.IExtendedFluid;
import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidTag;
import gregtech.api.fluids.info.FluidTags;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.TooltipHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Interface defining the abilities of a GT fluid container
 */
@FunctionalInterface
public interface IAdvancedFluidContainer {

    default boolean canHoldFluid(@Nullable FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return false;

        Fluid fluid = stack.getFluid();
        if (fluid instanceof IExtendedFluid) {
            IExtendedFluid advanced = (IExtendedFluid) fluid;
            FluidState state = advanced.getState();
            if (!this.canHandleState(state)) return false;
            if (!this.canHandleTemperature(state, fluid.getTemperature(stack))) return false;

            for (FluidTag tag : advanced.getTags()) {
                if (tag.requiresContainmentCheck() && !this.canHandleTag(tag)) return false;
            }
        } else {
            FluidState state = FluidState.LIQUID;
            if (fluid.getUnlocalizedName().contains("plasma")) state = FluidState.PLASMA;
            else if (fluid.isGaseous()) state = FluidState.GAS;

            if (!this.canHandleState(state)) return false;
            //noinspection RedundantIfStatement
            if (!this.canHandleTemperature(state, fluid.getTemperature(stack))) return false;
        }
        return true;
    }

    /**
     * @return the containment info for this container
     */
    @Nonnull
    FluidContainmentInfo getContainmentInfo();

    /**
     * @param state the state to test
     * @return if the container can handle a fluid of this state
     */
    default boolean canHandleState(@Nonnull FluidState state) {
        switch (state) {
            case LIQUID: return getContainmentInfo().canHoldLiquids();
            case GAS: return getContainmentInfo().canHoldGases();
            case PLASMA: return getContainmentInfo().canHoldPlasmas();
            default: throw new IllegalStateException("State was in an impossible configuration");
        }
    }

    /**
     * @param state       the state to test
     * @param temperature the temperature to test
     * @return if the container can handle a fluid of this state and temperature
     */
    default boolean canHandleTemperature(@Nonnull FluidState state, int temperature) {
        FluidContainmentInfo info = getContainmentInfo();
        final int maxTemperature = info.getMaxTemperature();
        if (state == FluidState.PLASMA && temperature > maxTemperature) {
            return info.canHoldPlasmas();
        }

        if (GTUtility.isTemperatureCryogenic(temperature)) return info.canHoldCryogenics();

        return temperature <= maxTemperature;
    }

    /**
     * @param tag the tag to test
     * @return if the container can handle a fluid with this tag
     */
    default boolean canHandleTag(@Nonnull FluidTag tag) {
        FluidContainmentInfo info = getContainmentInfo();
        if (tag == FluidTags.ACID) return info.canHoldAcids();
        if (tag == FluidTags.SUPERACID) return info.canHoldSuperacids();

        Collection<FluidTag> allowedTags = info.getAllowedTags();
        return allowedTags != null && allowedTags.contains(tag);
    }

    /**
     * @param tooltip the tooltip to append to
     * @param withShiftInfo whether to include the "hold shift" information
     */
    default void appendTooltips(@Nonnull List<String> tooltip, boolean withShiftInfo) {
        FluidContainmentInfo info = getContainmentInfo();
        if (!withShiftInfo || TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", info.getMaxTemperature()));
            if (info.canHoldGases()) tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
            else tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof"));
            if (info.canHoldSuperacids()) tooltip.add(I18n.format("gregtech.fluid_pipe.superacid_proof"));
            else if (info.canHoldAcids()) tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
            if (info.canHoldCryogenics()) tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
            if (info.canHoldPlasmas()) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
        } else if (info.canHoldGases() || info.canHoldAcids() || info.canHoldSuperacids() || info.canHoldCryogenics() ||
                info.canHoldPlasmas()) {
            tooltip.add(I18n.format("gregtech.tooltip.fluid_pipe_hold_shift"));
        }
    }
}
