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
        final Supplier<Map<ItemStackKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return StreamUtils.streamFrom(inputs)
                // keep only non-empty item stacks
                .filter(not(ItemStack::isEmpty))
                // Track the number of identical items
                .collect(Collectors.toMap(KeySharedStack::getRegisteredStack,
                        ItemStack::getCount,
                        Math::addExact,
                        mapSupplier));
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStackKey}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStackKey} and {@link Integer} as amount on the inventory
     */
    public static Map<ItemStackKey, Integer> fromItemStackCollection(Collection<ItemStack> inputs) {
        final Supplier<Map<ItemStackKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return inputs.stream()
                // keep only non-empty item stacks
                .filter(not(ItemStack::isEmpty))
                // Track the number of identical items
                .collect(Collectors.toMap(KeySharedStack::getRegisteredStack,
                        ItemStack::getCount,
                        Math::addExact,
                        mapSupplier));
    }

    /**
     * Maps all fluids in the {@link IFluidHandler} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static Map<FluidKey, Integer> fromFluidHandler(IFluidHandler fluidInputs) {
        final Supplier<Map<FluidKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return StreamUtils.streamFrom(fluidInputs)
                // keep only non-empty item stacks
                .filter(Objects::nonNull)
                // Track the number of identical items
                .collect(Collectors.toMap(FluidKey::new,
                        (fluidStack -> fluidStack.amount),
                        Math::addExact,
                        mapSupplier));
    }

    /**
     * Maps all fluids in the {@link FluidStack} {@link Collection} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static Map<FluidKey, Integer> fromFluidCollection(Collection<FluidStack> fluidInputs) {
        final Supplier<Map<FluidKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return fluidInputs.stream()
                // keep only non-empty item stacks
                .filter(Objects::nonNull)
                // Track the number of identical items
                .collect(Collectors.toMap(FluidKey::new,
                        (fluidStack -> fluidStack.amount),
                        Math::addExact,
                        mapSupplier));
    }
}
