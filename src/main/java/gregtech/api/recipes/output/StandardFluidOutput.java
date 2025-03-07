package gregtech.api.recipes.output;

import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.roll.ListWithRollInformation;
import gregtech.api.recipes.roll.RollInterpreterApplication;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public final class StandardFluidOutput implements FluidOutputProvider {

    private final @NotNull ListWithRollInformation<FluidStack> outputs;

    public StandardFluidOutput(@NotNull ListWithRollInformation<FluidStack> outputs) {
        this.outputs = outputs;
    }

    public @NotNull ListWithRollInformation<FluidStack> getOutputs() {
        return outputs;
    }

    @Override
    public @NotNull List<FluidStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                    @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                                    @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                                    int machineTier,
                                                    int parallel, int trimLimit) {
        List<FluidStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size() * parallel / 2));
        long[] roll = this.outputs.comprehensiveRoll(machineTier - recipeTier, trimLimit, parallel);
        for (int j = 0; j < roll.length; j++) {
            addStackToList(outputs, this.outputs.get(j), roll[j]);
        }
        return outputs;
    }

    public static void addStackToList(List<FluidStack> list, FluidStack stack, long amount) {
        for (FluidStack stackInList : list) {
            int insertable = Integer.MAX_VALUE - stackInList.amount;
            if (insertable > 0 && stack.isFluidEqual(stackInList)) {
                if (insertable >= amount) {
                    stackInList.amount += amount;
                    return;
                } else {
                    stackInList.amount += insertable;
                    amount -= insertable;
                }
            }
        }
        if (amount > 0) {
            while (amount > 0) {
                FluidStack s = stack.copy();
                int c = (int) Math.min(Integer.MAX_VALUE, amount);
                s.amount = c;
                list.add(s);
                amount -= c;
            }
        }
    }

    @Override
    public @NotNull @UnmodifiableView List<FluidStack> getCompleteOutputs(int parallel, int trimLimit) {
        List<FluidStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size()) * parallel / 2);
        int limit = Math.min(this.outputs.size(), trimLimit);
        for (int j = 0; j < limit; j++) {
            FluidStack stack = this.outputs.get(j);
            addStackToList(outputs, stack, (long) stack.amount * parallel);
        }
        return outputs;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaximumOutputs(
                                                                          @Range(from = 1,
                                                                                 to = Integer.MAX_VALUE) int parallel) {
        return this.outputs.size() * parallel;
    }

    @Override
    public boolean isValid() {
        for (FluidStack stack : getOutputs()) {
            if (stack == null || stack.getFluid() == null || stack.amount == 0) return false;
        }
        return true;
    }

    @Override
    public @Nullable String addSmallDisplay(int index) {
        if (outputs.isRolled(index)) {
            return outputs.getInterpreter().interpretSmallDisplay(index, RollInterpreterApplication.FLUID_OUTPUT,
                    outputs.getMaxYield(index), outputs.getRollValue(index), outputs.getRollBoost(index));
        }
        return null;
    }

    @Override
    public @Nullable String addTooltip(int index) {
        if (outputs.isRolled(index)) {
            return outputs.getInterpreter().interpretTooltip(index, RollInterpreterApplication.FLUID_OUTPUT,
                    outputs.getMaxYield(index), outputs.getRollValue(index), outputs.getRollBoost(index));
        }
        return null;
    }
}
