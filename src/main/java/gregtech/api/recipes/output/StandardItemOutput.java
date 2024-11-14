package gregtech.api.recipes.output;

import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.roll.ListWithRollInformation;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

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
                                                   int machineTier,
                                                   int parallel, int trimLimit) {
        List<ItemStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size()) * parallel / 2);
        long[] roll = this.outputs.comprehensiveRoll(machineTier - recipeTier, trimLimit, parallel);
        for (int j = 0; j < roll.length; j++) {
            addStackToList(outputs, this.outputs.get(j), roll[j]);
        }
        return outputs;
    }

    public static void addStackToList(List<ItemStack> list, ItemStack stack, long count) {
        for (ItemStack stackInList : list) {
            int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
            if (insertable > 0 && ItemHandlerHelper.canItemStacksStack(stackInList, stack)) {
                if (insertable >= count) {
                    stackInList.grow((int) count);
                    return;
                } else {
                    stackInList.grow(insertable);
                    count -= insertable;
                }
            }
        }
        if (count > 0) {
            int max = stack.getMaxStackSize();
            while (count > 0) {
                ItemStack s = stack.copy();
                int c = (int) Math.min(max, count);
                s.setCount(c);
                list.add(s);
                count -= c;
            }
        }
    }

    @Override
    public @NotNull @UnmodifiableView List<ItemStack> getCompleteOutputs(int parallel, int trimLimit,
                                                                         @UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                                         @UnmodifiableView @NotNull List<FluidStack> inputFluids) {
        List<ItemStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size()) * parallel / 2);
        int limit = Math.min(this.outputs.size(), trimLimit);
        for (int j = 0; j < limit; j++) {
            ItemStack stack = this.outputs.get(j);
            addStackToList(outputs, stack, (long) stack.getCount() * parallel);
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
        for (ItemStack stack : getOutputs()) {
            if (stack == null || stack.getItem() == Items.AIR || stack.isEmpty()) return false;
        }
        return true;
    }
}
