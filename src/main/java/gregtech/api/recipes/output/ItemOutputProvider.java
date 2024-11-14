package gregtech.api.recipes.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface ItemOutputProvider {

    /**
     * Called on recipe setup to compute item outputs of the recipe. Should roll chance in this stage.
     * 
     * @param inputItems    input fluids on this recipe run.
     * @param inputFluids   input items on this recipe run.
     * @param propertySet   property set for this recipe run. Holds things like input power, ebf temperature, etc.
     * @param recipeTier    the recipe tier, for use in chanced outputs.
     * @param machineTier   the machine tier, for use in chanced outputs.
     * @param boostFunction the boost function, for use in chanced outputs.
     * @param parallel the parallel level of this recipe run.
     * @return the item outputs for this recipe run.
     */
    @NotNull
    default List<ItemStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                   @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                   @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                   int machineTier, @NotNull ChanceBoostFunction boostFunction, int parallel) {
        return computeOutputs(inputItems, inputFluids, propertySet, recipeTier, machineTier, boostFunction, parallel, Integer.MAX_VALUE);
    }

    /**
     * Called on recipe setup to compute item outputs of the recipe. Should roll chance in this stage.
     *
     * @param inputItems    input fluids on this recipe run.
     * @param inputFluids   input items on this recipe run.
     * @param propertySet   property set for this recipe run. Holds things like input power, ebf temperature, etc.
     * @param recipeTier    the recipe tier, for use in chanced outputs.
     * @param machineTier   the machine tier, for use in chanced outputs.
     * @param boostFunction the boost function, for use in chanced outputs.
     * @param parallel the parallel level of this recipe run.
     * @param trimLimit the most outputs allowed, before parallel.
     * @return the item outputs for this recipe run.
     */
    @NotNull
    List<ItemStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                   @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                   @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier, int machineTier,
                                   @NotNull ChanceBoostFunction boostFunction, int parallel, int trimLimit);

    /**
     * Called to gather information about what this recipe can output.
     * Used for JEI display, output bus fit calculations, etc.
     *
     * @param parallel the parallel level for this simulated run.
     * @param trimLimit the most outputs allowed, before parallel.
     * @param inputItems  input items for this simulated run.
     * @param inputFluids input fluids for this simulated run.
     * @return the maximum possible outputs of this provider.
     */
    @NotNull
    @UnmodifiableView List<ItemStack> getCompleteOutputs(int parallel, int trimLimit,
            @UnmodifiableView @NotNull List<ItemStack> inputItems,
            @UnmodifiableView @NotNull List<FluidStack> inputFluids);

    /**
     * @return the most ItemStacks {@link #computeOutputs(List, List, PropertySet, int, int, ChanceBoostFunction, int)}
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
}
