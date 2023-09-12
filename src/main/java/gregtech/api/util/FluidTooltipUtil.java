package gregtech.api.util;

import gregtech.api.fluids.GTFluid;
import gregtech.api.unification.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.fluids.FluidConstants.CRYOGENIC_FLUID_THRESHOLD;

public class FluidTooltipUtil {

    /**
     * Registry Mapping of <Fluid, Tooltip>
     */
    private static final Map<Fluid, Supplier<List<String>>> tooltips = new HashMap<>();

    /**
     * Used to register a tooltip to a Fluid.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     */
    public static void registerTooltip(@NotNull Fluid fluid, @NotNull Supplier<List<String>> tooltip) {
        tooltips.put(fluid, tooltip);
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

        var supplier = tooltips.get(fluid);
        return supplier == null ? Collections.emptyList() : supplier.get();
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

    public static Supplier<List<String>> createGTFluidTooltip(GTFluid fluid) {
        return () -> {
            List<String> tooltip = new ArrayList<>();
            if (fluid instanceof GTFluid.GTMaterialFluid materialFluid) {
                Material material = materialFluid.getMaterial();
                if (!material.getChemicalFormula().isEmpty()) {
                    tooltip.add(TextFormatting.YELLOW + material.getChemicalFormula());
                }
            }

            tooltip.add(I18n.format("gregtech.fluid.temperature", fluid.getTemperature()));
            tooltip.add(I18n.format(fluid.getState().getTranslationKey()));
            fluid.getAttributes().forEach(a -> a.appendFluidTooltips(tooltip));

            if (fluid.getTemperature() < CRYOGENIC_FLUID_THRESHOLD) {
                tooltip.add(I18n.format("gregtech.fluid.temperature.cryogenic"));
            }

            return tooltip;
        };
    }
}
