package gregtech.api.util;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidTooltipUtil {

    /**
     * Registry Mapping of <Fluid, Tooltip>
     */
    private static final Map<Fluid, List<String>> tooltips = new HashMap<>();

    /**
     * Used to register a tooltip to a Fluid.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     */
    public static void registerTooltip(Fluid fluid, String tooltip) {
        if (fluid != null && tooltip != null) {
            tooltips.computeIfAbsent(fluid, k -> new ArrayList<>()).add(tooltip);
        }
    }

    /**
     * Used to register a tooltip to a Fluid.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     */
    public static void registerTooltip(Fluid fluid, List<String> tooltip) {
        if (fluid != null && tooltip != null && !tooltip.isEmpty()) {
            tooltips.put(fluid, tooltip);
        }
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluid The Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static List<String> getFluidTooltip(Fluid fluid) {
        if (fluid == null) {
            return null;
        }

        return tooltips.get(fluid);
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param stack A FluidStack, containing the Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static List<String> getFluidTooltip(FluidStack stack) {
        if (stack == null) {
            return null;
        }

        return getFluidTooltip(stack.getFluid());
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluidName A String representing a Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static List<String> getFluidTooltip(String fluidName) {
        if (fluidName == null || fluidName.isEmpty()) {
            return null;
        }

        return getFluidTooltip(FluidRegistry.getFluid(fluidName));
    }

    /**
     * A simple helper method to get the tooltip for Water, since it is an edge case of fluids.
     */
    public static List<String> getWaterTooltip() {
        return getMaterialTooltip(Materials.Water);
    }

    /**
     * A simple helper method to get the tooltip for Lava, since it is an edge case of fluids.
     */
    public static List<String> getLavaTooltip() {
        return getMaterialTooltip(Materials.Lava);
    }

    private static List<String> getMaterialTooltip(Material m) {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(m.getChemicalFormula());
        tooltip.add(String.valueOf(m.getProperty(PropertyKey.FLUID).getFluidTemperature()));
        tooltip.add(String.valueOf(m.getProperty(PropertyKey.FLUID).getFluidType().getName()));
        return tooltip;
    }
}
