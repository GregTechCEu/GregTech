package gregtech.api.recipes.output;

import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface FluidOutputProvider {

    /**
     * Called on recipe setup to compute fluid outputs of the recipe. Should roll chance in this stage.
     * 
     * @param inputItems  input fluids on this recipe run, after rolling.
     * @param inputFluids input items on this recipe run, after rolling.
     * @param propertySet property set for this recipe run. Holds things like input power, ebf temperature, etc.
     * @param recipeTier  the recipe tier, for use in chanced outputs.
     * @param machineTier the machine tier, for use in chanced outputs.
     * @param parallel    the parallel level of this recipe run.
     * @return the fluid outputs for this recipe run.
     */
    @NotNull
    default List<FluidStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                            @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                            @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                            int machineTier, int parallel) {
        return computeOutputs(inputItems, inputFluids, propertySet, recipeTier, machineTier, parallel,
                Integer.MAX_VALUE);
    }

    /**
     * Called on recipe setup to compute fluid outputs of the recipe. Should roll chance in this stage.
     *
     * @param inputItems  input fluids on this recipe run, after rolling.
     * @param inputFluids input items on this recipe run, after rolling.
     * @param propertySet property set for this recipe run. Holds things like input power, ebf temperature, etc.
     * @param recipeTier  the recipe tier, for use in chanced outputs.
     * @param machineTier the machine tier, for use in chanced outputs.
     * @param parallel    the parallel level of this recipe run.
     * @param trimLimit   the most outputs allowed, before parallel.
     * @return the fluid outputs for this recipe run.
     */
    @NotNull
    List<FluidStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                    @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                    @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier, int machineTier,
                                    int parallel, int trimLimit);

    /**
     * Called to gather information about what this recipe can output.
     * Used for JEI display, output hatch fit calculations, etc.
     *
     * @param parallel    the parallel level for this simulated run.
     * @param trimLimit   the most outputs allowed, before parallel.
     * @param inputItems  unrolled input items for this simulated run.
     * @param inputFluids unrolled input fluids for this simulated run.
     * @return the maximum possible outputs of this provider.
     */
    @NotNull
    @UnmodifiableView
    List<FluidStack> getCompleteOutputs(int parallel, int trimLimit,
                                        @UnmodifiableView @NotNull List<ItemStack> inputItems,
                                        @UnmodifiableView @NotNull List<FluidStack> inputFluids);

    /**
     * @return the most FluidStacks {@link #computeOutputs(List, List, PropertySet, int, int, int)}
     *         will ever return.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    int getMaximumOutputs(@Range(from = 1, to = Integer.MAX_VALUE) int parallel);

    /**
     * should conduct an internal poll as to the validness of this output provider;
     * e.g. checking that all output stacks exist, for example.
     * 
     * @return whether this output provider is valid
     */
    boolean isValid();

    /**
     * Can provide a string that will be displayed beneath an output in JEI
     *
     * @param index the index of the output
     * @return the string to be displayed.
     */
    default @Nullable String addSmallDisplay(int index) {
        return null;
    }

    /**
     * Can provide a string that will be displayed in the tooltip for an output in JEI
     *
     * @param index the index of the rolled input/output
     * @return the string to be displayed.
     */
    default @Nullable String addTooltip(int index) {
        return null;
    }

    /**
     * Can provide a string that will be displayed underneath a recipe in JEI.
     *
     * @return the string to be displayed, or null if nothing should be displayed.
     */
    default @Nullable String addJEILine() {
        return null;
    }

    /**
     * Can provide a string that will be displayed when the string provided by {@link #addJEILine()} is hovered over.
     *
     * @return the string to be displayed, or null if nothing should be displayed.
     */
    default @Nullable String addJEITooltip() {
        return null;
    }
}
