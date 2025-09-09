package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.match.MatchCalculation;

import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrimmedRecipeView extends StandardRecipeView {

    protected final int maxItems;
    protected final int maxFluids;

    public TrimmedRecipeView(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                             @NotNull MatchCalculation<FluidStack> fluidMatch, double voltageDiscount,
                             int initialParallel, int maxItems, int maxFluids) {
        super(recipe, itemMatch, fluidMatch, voltageDiscount, initialParallel);
        this.maxItems = maxItems;
        this.maxFluids = maxFluids;
    }

    @Override
    public @NotNull List<ItemStack> rollItems(int recipeTier, int machineTier) {
        computeConsumptions(machineTier - recipeTier);
        var out = recipe.getItemAndChanceOutputs(maxItems);
        List<ItemStack> outputs = new ArrayList<>(out.getLeft());
        ChanceBoostFunction function = recipe.getRecipeCategory().getRecipeMap().getChanceFunction();
        List<ChancedItemOutput> chancedOutputsList = recipe.getChancedOutputs().getChancedOutputLogic()
                .roll(out.getRight(), function, recipeTier, machineTier);

        if (chancedOutputsList == null) return outputs;

        Collection<ItemStack> resultChanced = new ArrayList<>();
        for (ChancedItemOutput chancedOutput : chancedOutputsList) {
            ItemStack stackToAdd = chancedOutput.getIngredient().copy();
            for (ItemStack stackInList : resultChanced) {
                int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
                if (insertable > 0 && ItemHandlerHelper.canItemStacksStack(stackInList, stackToAdd)) {
                    if (insertable >= stackToAdd.getCount()) {
                        stackInList.grow(stackToAdd.getCount());
                        stackToAdd = ItemStack.EMPTY;
                        break;
                    } else {
                        stackInList.grow(insertable);
                        stackToAdd.shrink(insertable);
                    }
                }
            }
            if (!stackToAdd.isEmpty()) {
                resultChanced.add(stackToAdd);
            }
        }

        outputs.addAll(resultChanced);

        return outputs;
    }

    @Override
    public @NotNull List<FluidStack> rollFluids(int recipeTier, int machineTier) {
        computeConsumptions(machineTier - recipeTier);
        var out = recipe.getFluidAndChanceOutputs(maxFluids);
        List<FluidStack> outputs = GTUtility.copyFluidList(out.getLeft());
        ChanceBoostFunction function = recipe.getRecipeCategory().getRecipeMap().getChanceFunction();
        List<ChancedFluidOutput> chancedOutputsList = recipe.getChancedFluidOutputs().getChancedOutputLogic()
                .roll(out.getRight(), function, recipeTier, machineTier);

        if (chancedOutputsList == null) return outputs;

        Collection<FluidStack> resultChanced = new ArrayList<>();
        for (ChancedFluidOutput chancedOutput : chancedOutputsList) {
            FluidStack stackToAdd = chancedOutput.getIngredient().copy();
            for (FluidStack stackInList : resultChanced) {
                int insertable = stackInList.amount;
                if (insertable > 0 && stackInList.getFluid() == stackToAdd.getFluid()) {
                    stackInList.amount += stackToAdd.amount;
                    stackToAdd = null;
                    break;
                }
            }
            if (stackToAdd != null) {
                resultChanced.add(stackToAdd);
            }
        }

        outputs.addAll(resultChanced);

        return outputs;
    }

    @Override
    public List<ItemStack> getMaximumItems() {
        if (iOut != null) return iOut;
        computeMatches();
        var out = recipe.getItemAndChanceOutputs(maxItems);
        iOut = new ArrayList<>(out.getLeft());

        for (ChancedItemOutput entry : out.getRight()) {
            iOut.add(entry.getIngredient().copy());
        }

        return iOut;
    }

    @Override
    public List<FluidStack> getMaximumFluids() {
        if (fOut != null) return fOut;
        computeMatches();
        var out = recipe.getFluidAndChanceOutputs(maxFluids);
        fOut = new ArrayList<>(out.getLeft());

        for (ChancedFluidOutput entry : out.getRight()) {
            fOut.add(entry.getIngredient().copy());
        }
        return fOut;
    }
}
