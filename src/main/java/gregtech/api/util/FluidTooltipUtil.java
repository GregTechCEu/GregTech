package gregtech.api.util;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public class FluidTooltipUtil {

    /**
     * Registry Mapping of <Fluid, Tooltip>
     */
    private static final Map<Fluid, String> tooltips = new HashMap<>();

    /**
     * Used to register a tooltip to a Fluid. A Fluid can only have one tooltip, on one line.
     * <p>
     * Ignores a tooltip applied for Water, so that it will be handled correctly for the chemical formula.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     * @return False if either parameter is null or if tooltip is empty, true otherwise.
     */
    public static boolean registerTooltip(Fluid fluid, String tooltip) {
        if (fluid != null && tooltip != null && !tooltip.isEmpty()) {
            tooltips.put(fluid, tooltip);
            return true;
        }
        return false;
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluid The Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static String getFluidTooltip(Fluid fluid) {
        if (fluid == null)
            return null;

        return tooltips.get(fluid);
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param stack A FluidStack, containing the Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static String getFluidTooltip(FluidStack stack) {
        if (stack == null)
            return null;

        return getFluidTooltip(stack.getFluid());
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluidName A String representing a Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static String getFluidTooltip(String fluidName) {
        if (fluidName == null || fluidName.isEmpty())
            return null;

        return getFluidTooltip(FluidRegistry.getFluid(fluidName));
    }

    /**
     * A simple helper method to get the tooltip for Water, since it is an edge case of fluids.
     *
     * @return "H₂O"
     */
    public static String getWaterTooltip() {
        // Done like this to not return parenthesis around the tooltip
        return (new MaterialStack(Materials.Hydrogen, 2)).toString() + "O";
    }
}
