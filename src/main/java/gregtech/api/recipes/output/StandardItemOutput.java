package gregtech.api.recipes.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.lookup.property.PropertySet;

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

    private final @NotNull List<ItemStack> outputs;
    private final @Nullable ChancedOutputList<ItemStack, ChancedItemOutput> chanced;

    private final @NotNull Pair<List<ItemStack>, List<ChancedItemOutput>> pair;

    public StandardItemOutput(@Nullable List<ItemStack> outputs,
                              @Nullable ChancedOutputList<ItemStack, ChancedItemOutput> chanced) {
        this.outputs = outputs == null ? Collections.emptyList() : outputs;
        this.chanced = chanced;
        this.pair = Pair.of(this.outputs, chanced == null ? Collections.emptyList() : chanced.getChancedEntries());
    }

    public @NotNull List<ItemStack> getOutputs() {
        return outputs;
    }

    public @Nullable ChancedOutputList<ItemStack, ChancedItemOutput> getChanced() {
        return chanced;
    }

    public @NotNull List<ChancedItemOutput> getChancedEntries() {
        return pair.getRight();
    }

    @Override
    public @NotNull List<ItemStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                   @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                                   @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                                   int machineTier, @NotNull ChanceBoostFunction boostFunction,
                                                   int parallel, int trimLimit) {
        List<ItemStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size() + this.getChancedEntries().size()) * (parallel + 1) / 2);
        int items = Math.min(this.outputs.size(), trimLimit);
        int chanceds = Math.min(this.getChancedEntries().size(), trimLimit - items);
        for (int i = 0; i < parallel; i++) {
            for (int j = 0; j < items; j++) {
                ItemStack stack = this.outputs.get(j);
                addStackToList(outputs, stack);
            }
            if (chanced != null && chanceds > 0) {
                List<ChancedItemOutput> out = chanced.roll(boostFunction, recipeTier, machineTier, chanceds);
                if (out != null) {
                    for (ChancedItemOutput chance : out) {
                        addStackToList(outputs, chance.getIngredient());
                    }
                }
            }
        }
        return outputs;
    }

    public static void addStackToList(List<ItemStack> list, ItemStack stack) {
        int stackCount = stack.getCount();
        for (ItemStack stackInList : list) {
            int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
            if (insertable > 0 && ItemHandlerHelper.canItemStacksStack(stackInList, stack)) {
                if (insertable >= stackCount) {
                    stackInList.grow(stackCount);
                    return;
                } else {
                    stackInList.grow(insertable);
                    stackCount -= insertable;
                }
            }
        }
        if (stackCount > 0) {
            stack = stack.copy();
            stack.setCount(stackCount);
            list.add(stack);
        }
    }

    @Override
    public @NotNull Pair<@UnmodifiableView @NotNull List<ItemStack>, @UnmodifiableView @NotNull List<ChancedItemOutput>> getCompleteOutputs(
                                                                                                                                            @UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                                                                                                            @UnmodifiableView @NotNull List<FluidStack> inputFluids) {
        return pair;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaximumOutputs() {
        return pair.getLeft().size() + pair.getRight().size();
    }

    @Override
    public boolean isValid() {
        for (ItemStack stack : getOutputs()) {
            if (stack == null || stack.getItem() == Items.AIR || stack.isEmpty()) return false;
        }
        for (ChancedItemOutput output : getChancedEntries()) {
            if (output == null || output.getIngredient().getItem() == Items.AIR || output.getIngredient().isEmpty())
                return false;
        }
        return true;
    }
}
