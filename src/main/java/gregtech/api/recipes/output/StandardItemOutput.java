package gregtech.api.recipes.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.lookup.property.PropertySet;

import gregtech.api.recipes.roll.ListWithRollInformation;

import gregtech.api.util.GTUtility;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public final class StandardItemOutput implements ItemOutputProvider {

    private final @NotNull ListWithRollInformation<ItemStack> outputs;

    public StandardItemOutput(@NotNull ListWithRollInformation<ItemStack> outputs) {
        this.outputs = outputs;
    }

    public @NotNull ListWithRollInformation<ItemStack> getOutputs() {
        return outputs;
    }

    @Override
    public @NotNull List<ItemStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                   @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                                   @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                                   int machineTier, @NotNull ChanceBoostFunction boostFunction,
                                                   int parallel, int trimLimit) {
        List<ItemStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size()) * parallel / 2);
        for (int i = 0; i < parallel; i++) {
            long[] roll = this.outputs.comprehensiveRoll(machineTier - recipeTier, trimLimit);
            for (int j = 0; j < roll.length; j++) {
                addStackToList(outputs, this.outputs.get(j), GTUtility.safeCastLongToInt(roll[j]));
            }
        }
        return outputs;
    }

    public static void addStackToList(List<ItemStack> list, ItemStack stack, int count) {
        for (ItemStack stackInList : list) {
            int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
            if (insertable > 0 && ItemHandlerHelper.canItemStacksStack(stackInList, stack)) {
                if (insertable >= count) {
                    stackInList.grow(count);
                    return;
                } else {
                    stackInList.grow(insertable);
                    count -= insertable;
                }
            }
        }
        if (count > 0) {
            stack = stack.copy();
            stack.setCount(count);
            list.add(stack);
        }
    }

    @Override
    public @NotNull @UnmodifiableView List<ItemStack> getCompleteOutputs(int parallel, int trimLimit,
                                                                         @UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                                         @UnmodifiableView @NotNull List<FluidStack> inputFluids) {
        List<ItemStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size()) * parallel / 2);
        int limit = Math.min(this.outputs.size(), trimLimit);
        for (int i = 0; i < parallel; i++) {
            for (int j = 0; j < limit; j++) {
                ItemStack stack = this.outputs.get(j);
                addStackToList(outputs, stack, stack.getCount());
            }
        }
        return outputs;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaximumOutputs(
            @Range(from = 1, to = Integer.MAX_VALUE) int parallel) {
        return this.outputs.size() * parallel;
    }

    @Override
    public boolean isValid() {
        for (ItemStack stack : getOutputs()) {
            if (stack == null || stack.getItem() == Items.AIR || stack.isEmpty()) return false;
        }
        return true;
    }
}
