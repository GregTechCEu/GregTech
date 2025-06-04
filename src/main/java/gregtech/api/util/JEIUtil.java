package gregtech.api.util;

import gregtech.mixins.jei.GuiIngredientGroupAccessor;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JEIUtil {

    /**
     * Collects all the item stacks from the input slots of a {@link IGuiItemStackGroup}. Empty slots will be set as
     * {@link ItemStack#EMPTY}.
     * 
     * @param stackGroup the group to collect {@link ItemStack}s from
     * @return the list of collected item stacks
     */
    public static @NotNull List<ItemStack> getDisplayedInputItemStacks(@NotNull IGuiItemStackGroup stackGroup) {
        var stackMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = ((GuiIngredientGroupAccessor) stackGroup).getInputSlotIndexes();
        if (inputIndexes.isEmpty()) return Collections.emptyList();

        List<ItemStack> itemStacks = new ArrayList<>(inputIndexes.size());
        for (var index : stackMap.keySet()) {
            if (inputIndexes.contains(index)) {
                ItemStack displayStack = stackMap.get(index).getDisplayedIngredient();
                itemStacks.add(displayStack == null ? ItemStack.EMPTY : displayStack);
            }
        }

        return itemStacks;
    }

    /**
     * Collects all the item stacks from the output slots of a {@link IGuiItemStackGroup}. Empty slots will be set as
     * {@link ItemStack#EMPTY}.
     * 
     * @param stackGroup the group to collect {@link ItemStack}s from
     * @return the list of collected item stacks
     */
    public static @NotNull List<ItemStack> getDisplayedOutputItemStacks(@NotNull IGuiItemStackGroup stackGroup) {
        var stackMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = ((GuiIngredientGroupAccessor) stackGroup).getInputSlotIndexes();
        int outputCount = stackMap.size() - inputIndexes.size();
        if (outputCount < 1) return Collections.emptyList();

        List<ItemStack> itemStacks = new ArrayList<>(outputCount);
        for (var index : stackMap.keySet()) {
            if (!inputIndexes.contains(index)) {
                itemStacks.add(stackMap.get(index).getDisplayedIngredient());
            }
        }

        return itemStacks;
    }

    /**
     * Collects all the fluid stacks from the input slots of a {@link IGuiFluidStackGroup}. Empty slots will be set as
     * null.
     * 
     * @param stackGroup the group to collect {@link FluidStack}s from
     * @return the list of collected fluid stacks
     */
    public static @NotNull List<FluidStack> getDisplayedInputFluidStacks(@NotNull IGuiFluidStackGroup stackGroup) {
        var stackMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = ((GuiIngredientGroupAccessor) stackGroup).getInputSlotIndexes();
        if (inputIndexes.isEmpty()) return Collections.emptyList();

        List<FluidStack> fluidStacks = new ArrayList<>(inputIndexes.size());
        for (var index : stackMap.keySet()) {
            if (inputIndexes.contains(index)) {
                fluidStacks.add(stackMap.get(index).getDisplayedIngredient());
            }
        }

        return fluidStacks;
    }

    /**
     * Collects all the fluid stacks from the output slots of a {@link IGuiFluidStackGroup}. Empty slots will be set as
     * null.
     * 
     * @param stackGroup the group to collect {@link FluidStack}s from
     * @return the list of collected fluid stacks
     */
    public static @NotNull List<FluidStack> getDisplayedOutputFluidStacks(@NotNull IGuiFluidStackGroup stackGroup) {
        var stackMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = ((GuiIngredientGroupAccessor) stackGroup).getInputSlotIndexes();
        int outputCount = stackMap.size() - inputIndexes.size();
        if (outputCount < 1) return Collections.emptyList();

        List<FluidStack> fluidStacks = new ArrayList<>(outputCount);
        for (var index : stackMap.keySet()) {
            if (!inputIndexes.contains(index)) {
                fluidStacks.add(stackMap.get(index).getDisplayedIngredient());
            }
        }

        return fluidStacks;
    }
}
