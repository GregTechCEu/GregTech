package gregtech.api.recipes.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public final class StandardFluidOutput implements FluidOutputProvider {

    private final @NotNull List<FluidStack> outputs;
    private final @Nullable ChancedOutputList<FluidStack, ChancedFluidOutput> chanced;

    private final @NotNull Pair<List<FluidStack>, List<ChancedFluidOutput>> pair;

    public StandardFluidOutput(@Nullable List<FluidStack> outputs,
                               @Nullable ChancedOutputList<FluidStack, ChancedFluidOutput> chanced) {
        this.outputs = outputs == null ? Collections.emptyList() : outputs;
        this.chanced = chanced;
        this.pair = Pair.of(this.outputs, chanced == null ? Collections.emptyList() : chanced.getChancedEntries());
    }

    public @NotNull List<FluidStack> getOutputs() {
        return outputs;
    }

    public @Nullable ChancedOutputList<FluidStack, ChancedFluidOutput> getChanced() {
        return chanced;
    }

    public @NotNull List<ChancedFluidOutput> getChancedEntries() {
        return pair.getRight();
    }

    @Override
    public @NotNull List<FluidStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                    @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                                    @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                                    int machineTier, @NotNull ChanceBoostFunction boostFunction,
                                                    int parallel, int trimLimit) {
        List<FluidStack> outputs = new ObjectArrayList<>(Math.min(trimLimit, this.outputs.size() + this.getChancedEntries().size()) * (parallel + 1) / 2);
        int fluids = Math.min(this.outputs.size(), trimLimit);
        int chanceds = Math.min(this.getChancedEntries().size(), trimLimit - fluids);
        for (int i = 0; i < parallel; i++) {
            for (int j = 0; j < fluids; j++) {
                FluidStack stack = this.outputs.get(j);
                addStackToList(outputs, stack);
            }
            if (chanced != null && chanceds > 0) {
                List<ChancedFluidOutput> out = chanced.roll(boostFunction, recipeTier, machineTier, chanceds);
                if (out != null) {
                    for (ChancedFluidOutput chance : out) {
                        addStackToList(outputs, chance.getIngredient());
                    }
                }
            }
        }
        return outputs;
    }

    public static void addStackToList(List<FluidStack> list, FluidStack stack) {
        for (FluidStack stackInList : list) {
            int insertable = stackInList.amount;
            if (insertable > 0 && stack.isFluidEqual(stackInList)) {
                stackInList.amount += stack.amount;
                return;
            }
        }
        list.add(stack.copy());
    }

    @Override
    public @NotNull Pair<@UnmodifiableView @NotNull List<FluidStack>, @UnmodifiableView @NotNull List<ChancedFluidOutput>> getCompleteOutputs(
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
        for (FluidStack stack : getOutputs()) {
            if (stack == null || stack.getFluid() == null || stack.amount == 0) return false;
        }
        for (ChancedFluidOutput output : getChancedEntries()) {
            if (output == null || output.getIngredient().getFluid() == null || output.getIngredient().amount == 0)
                return false;
        }
        return true;
    }
}
