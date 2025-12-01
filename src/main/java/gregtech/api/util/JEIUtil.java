package gregtech.api.util;

import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.mixins.jei.GuiIngredientGroupAccessor;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class JEIUtil {

    /**
     * Collects all the item stacks from the input slots of a {@link IGuiItemStackGroup}.
     *
     * @param stackGroup    the group to collect {@link ItemStack}s from
     * @param getEmptySlots if true, empty slots will be entered into the map as {@link ItemStack#EMPTY}
     * @return a map of collected item stacks to their slot index
     */
    public static @NotNull Int2ObjectMap<ItemStack> getDisplayedInputItemStacks(@NotNull IGuiItemStackGroup stackGroup,
                                                                                boolean getEmptySlots,
                                                                                boolean copyStacks) {
        var originalMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = getInputIndexes(stackGroup);
        if (inputIndexes.isEmpty()) return Int2ObjectMaps.emptyMap();

        Int2ObjectMap<ItemStack> stackMap = new Int2ObjectOpenHashMap<>(inputIndexes.size());
        for (var index : originalMap.keySet()) {
            if (inputIndexes.contains(index)) {
                ItemStack displayStack = originalMap.get(index).getDisplayedIngredient();
                if (displayStack == null && getEmptySlots) {
                    stackMap.put(index, ItemStack.EMPTY);
                } else if (displayStack != null && !displayStack.isEmpty()) {
                    stackMap.put(index, copyStacks ? displayStack.copy() : displayStack);
                }
            }
        }

        return stackMap;
    }

    /**
     * Collects all the item stacks from the input slots of a {@link IGuiItemStackGroup}.
     *
     * @param stackGroup    the group to collect {@link ItemStack}s from
     * @param getEmptySlots if true, empty slots will be entered into the map as {@link ItemStack#EMPTY}
     * @return a map of collected item stacks to their slot index
     */
    public static @NotNull Int2ObjectMap<ItemStack> getDisplayedOutputItemStacks(@NotNull IGuiItemStackGroup stackGroup,
                                                                                 boolean getEmptySlots,
                                                                                 boolean copyStacks) {
        var originalMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = getInputIndexes(stackGroup);
        int outputCount = originalMap.size() - inputIndexes.size();
        if (outputCount < 1) return Int2ObjectMaps.emptyMap();

        Int2ObjectMap<ItemStack> stackMap = new Int2ObjectOpenHashMap<>(outputCount);
        for (var index : originalMap.keySet()) {
            if (!inputIndexes.contains(index)) {
                ItemStack displayStack = originalMap.get(index).getDisplayedIngredient();
                if (displayStack == null && getEmptySlots) {
                    stackMap.put(index, ItemStack.EMPTY);
                } else if (displayStack != null && !displayStack.isEmpty()) {
                    stackMap.put(index, copyStacks ? displayStack.copy() : displayStack);
                }
            }
        }

        return stackMap;
    }

    /**
     * Collects all the fluid stacks from the input slots of a {@link IGuiFluidStackGroup}.
     *
     * @param stackGroup    the group to collect {@link FluidStack}s from
     * @param getEmptySlots if true, empty slots will be entered into the map as null
     * @return a map of collected fluid stacks
     */
    public static @NotNull Int2ObjectMap<FluidStack> getDisplayedInputFluidStacks(@NotNull IGuiFluidStackGroup stackGroup,
                                                                                  boolean getEmptySlots,
                                                                                  boolean copyStacks) {
        var originalMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = getInputIndexes(stackGroup);
        if (inputIndexes.isEmpty()) return Int2ObjectMaps.emptyMap();

        Int2ObjectMap<FluidStack> stackMap = new Int2ObjectOpenHashMap<>(inputIndexes.size());
        for (var index : originalMap.keySet()) {
            if (inputIndexes.contains(index)) {
                FluidStack displayStack = originalMap.get(index).getDisplayedIngredient();
                if (displayStack == null && getEmptySlots) {
                    stackMap.put(index, null);
                } else if (displayStack != null) {
                    stackMap.put(index, copyStacks ? displayStack.copy() : displayStack);
                }
            }
        }

        return stackMap;
    }

    /**
     * Collects all the fluid stacks from the output slots of a {@link IGuiFluidStackGroup}.
     *
     * @param stackGroup    the group to collect {@link FluidStack}s from
     * @param getEmptySlots if true, empty slots will be entered into the map as null
     * @return a map of collected fluid stacks
     */
    public static @NotNull Int2ObjectMap<FluidStack> getDisplayedOutputFluidStacks(@NotNull IGuiFluidStackGroup stackGroup,
                                                                                   boolean getEmptySlots,
                                                                                   boolean copyStacks) {
        var originalMap = stackGroup.getGuiIngredients();
        Set<Integer> inputIndexes = getInputIndexes(stackGroup);
        int outputCount = originalMap.size() - inputIndexes.size();
        if (outputCount < 1) return Int2ObjectMaps.emptyMap();

        Int2ObjectMap<FluidStack> stackMap = new Int2ObjectOpenHashMap<>(outputCount);
        for (var index : originalMap.keySet()) {
            if (!inputIndexes.contains(index)) {
                FluidStack displayStack = originalMap.get(index).getDisplayedIngredient();
                if (displayStack == null && getEmptySlots) {
                    stackMap.put(index, null);
                } else if (displayStack != null) {
                    stackMap.put(index, copyStacks ? displayStack.copy() : displayStack);
                }
            }
        }

        return stackMap;
    }

    private static Set<Integer> getInputIndexes(IGuiIngredientGroup<?> ingredientGroup) {
        return ((GuiIngredientGroupAccessor) ingredientGroup).getInputSlotIndexes();
    }

    /**
     * Check if the player is currently hovering over a valid ingredient for this slot. <br/>
     * Will always return false is JEI is not installed.
     */
    public static boolean hoveringOverIngredient(RecipeViewerGhostIngredientSlot<?> jeiGhostIngredientSlot) {
        if (!Mods.JustEnoughItems.isModLoaded()) return false;
        return ModularUIJeiPlugin.hoveringOverIngredient(jeiGhostIngredientSlot);
    }

    public static ItemStack getActualStack(Object ingredient) {
        if (!Mods.JustEnoughItems.isModLoaded()) return ItemStack.EMPTY;
        return JustEnoughItemsModule.ingredientRegistry
                .getIngredientHelper(ingredient)
                .getCheatItemStack(ingredient);
    }
}
