package gregtech.api.util;

import gregtech.api.unification.material.Materials;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class FluidTooltipUtil {

    /**
     * Registry Mapping of <Fluid, Tooltip>
     */
    private static final Map<Fluid, List<String>> tooltips = new HashMap<>();

    /**
     * Used to register a tooltip to a Fluid.
     * <p>
     * Ignores a tooltip applied for Water, so that it will be handled correctly for the chemical formula.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     * @return False if either parameter is null or if tooltip is empty, true otherwise.
     */
    public static boolean registerTooltip(Fluid fluid, String tooltip) {
        if (fluid != null && tooltip != null && !tooltip.trim().isEmpty()) {
            tooltips.computeIfAbsent(fluid, k -> new ArrayList<>()).add(tooltip);
            return true;
        }
        return false;
    }

    /**
     * Used to register a tooltip to a Fluid.
     * <p>
     * Ignores a tooltip applied for Water, so that it will be handled correctly for the chemical formula.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     * @return False if either parameter is null or if tooltip is empty, true otherwise.
     */
    public static boolean registerTooltip(Fluid fluid, List<String> tooltip) {
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
    public static List<String> getFluidTooltip(Fluid fluid) {
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
    public static List<String> getFluidTooltip(FluidStack stack) {
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
    public static List<String> getFluidTooltip(String fluidName) {
        if (fluidName == null || fluidName.isEmpty())
            return null;

        return getFluidTooltip(FluidRegistry.getFluid(fluidName));
    }

    /**
     * A simple helper method to get the tooltip for Water, since it is an edge case of fluids.
     *
     * @return "H₂O"
     */
    public static List<String> getWaterTooltip() {
        List<String> tooltip = new ArrayList<>();

        tooltip.add(Materials.Water.getChemicalFormula());
        tooltip.add(String.valueOf(Materials.Water.getFluid().getTemperature()));
        tooltip.add(String.valueOf(Materials.Water.getFluid().isGaseous()));

        return tooltip;
    }
}
