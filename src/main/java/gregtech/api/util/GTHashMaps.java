package gregtech.api.util;

import gregtech.api.recipes.FluidKey;
import gregtech.api.recipes.KeySharedStack;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static gregtech.api.util.Predicates.not;

public class GTHashMaps {
    /**
     * Maps all items in the {@link IItemHandler} into a {@link ItemStackKey}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStackKey} and {@link Integer} as amount on the inventory
     */
    public static Map<ItemStackKey, Integer> fromItemHandler(IItemHandler inputs) {
        final Map<ItemStackKey, Integer> map = new Object2IntLinkedOpenHashMap<>();

        // Create a single stack of the combined count for each item

        for (int i = 0; i < inputs.getSlots(); i++) {
            ItemStack stack = inputs.getStackInSlot(i);
            if (!stack.isEmpty()) {
                map.computeIfPresent(KeySharedStack.getRegisteredStack(stack), (k, v) -> v + stack.getCount());
                map.computeIfAbsent(KeySharedStack.getRegisteredStack(stack), (v) -> stack.getCount());
            }
        }

        return map;
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStackKey}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStackKey} and {@link Integer} as amount on the inventory
     */
    public static Map<ItemStackKey, Integer> fromItemStackCollection(Collection<ItemStack> inputs) {
        final Map<ItemStackKey, Integer> map = new Object2IntLinkedOpenHashMap<>();

        // Create a single stack of the combined count for each item

        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                map.computeIfPresent(KeySharedStack.getRegisteredStack(stack), (k, v) -> v + stack.getCount());
                map.computeIfAbsent(KeySharedStack.getRegisteredStack(stack), (v) -> stack.getCount());
            }
        }

        return map;
    }

    /**
     * Maps all fluids in the {@link IFluidHandler} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static Map<FluidKey, Integer> fromFluidHandler(IFluidHandler fluidInputs) {
        final Map<FluidKey, Integer> map = new Object2IntLinkedOpenHashMap<>();

        // Create a single stack of the combined count for each item

        for (int i = 0; i < fluidInputs.getTankProperties().length; i++) {
            FluidStack fluidStack = fluidInputs.getTankProperties()[i].getContents();
            if (fluidStack != null && fluidStack.amount > 0) {
                map.computeIfPresent(new FluidKey(fluidStack), (k, v) -> v + fluidStack.amount);
                map.computeIfAbsent(new FluidKey(fluidStack), (v) -> fluidStack.amount);
            }
        }

        return map;
    }

    /**
     * Maps all fluids in the {@link FluidStack} {@link Collection} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static Map<FluidKey, Integer> fromFluidCollection(Collection<FluidStack> fluidInputs) {
        final Map<FluidKey, Integer> map = new Object2IntLinkedOpenHashMap<>();

        // Create a single stack of the combined count for each item

        for (FluidStack fluidStack : fluidInputs) {
            if (fluidStack != null && fluidStack.amount > 0) {
                map.computeIfPresent(new FluidKey(fluidStack), (k, v) -> v + fluidStack.amount);
                map.computeIfAbsent(new FluidKey(fluidStack), (v) -> fluidStack.amount);
            }
        }

        return map;
    }
}
