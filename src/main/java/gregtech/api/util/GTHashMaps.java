package gregtech.api.util;

import gregtech.api.recipes.FluidKey;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class GTHashMaps {

    private GTHashMaps() {}

    /**
     * Maps all items in the {@link IItemHandler} into a {@link ItemStack}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @Nonnull
    public static Object2IntMap<ItemStack> fromItemHandler(@Nonnull IItemHandler inputs) {
        return fromItemHandler(inputs, false);
    }

    /**
     * Maps all items in the {@link IItemHandler} into a {@link ItemStack}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @param linked If the Map should be a Linked Map to preserve insertion order
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @Nonnull
    public static Object2IntMap<ItemStack> fromItemHandler(@Nonnull IItemHandler inputs, boolean linked) {
        final Object2IntMap<ItemStack> map = createItemStackMap(linked);

        // Create a single stack of the combined count for each item

        for (int i = 0; i < inputs.getSlots(); i++) {
            ItemStack stack = inputs.getStackInSlot(i);
            if (!stack.isEmpty()) {
                map.put(stack.copy(), map.getInt(stack) + stack.getCount());
            }
        }

        return map;
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStack}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @Nonnull
    public static Object2IntMap<ItemStack> fromItemStackCollection(@Nonnull Iterable<ItemStack> inputs) {
        return fromItemStackCollection(inputs, false);
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStack}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @param linked If the Map should be a Linked Map to preserve insertion order
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @Nonnull
    public static Object2IntMap<ItemStack> fromItemStackCollection(@Nonnull Iterable<ItemStack> inputs, boolean linked) {
        final Object2IntMap<ItemStack> map = createItemStackMap(linked);

        // Create a single stack of the combined count for each item

        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                map.put(stack.copy(), map.getInt(stack) + stack.getCount());
            }
        }

        return map;
    }

    @Nonnull
    private static Object2IntMap<ItemStack> createItemStackMap(boolean linked) {
        ItemStackHashStrategy strategy = ItemStackHashStrategy.comparingAllButCount();
        return linked ? new Object2IntLinkedOpenCustomHashMap<>(strategy) : new Object2IntOpenCustomHashMap<>(strategy);
    }

    /**
     * Maps all fluids in the {@link IFluidHandler} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static Map<FluidKey, Integer> fromFluidHandler(IFluidHandler fluidInputs) {
        final Object2IntMap<FluidKey> map = new Object2IntLinkedOpenHashMap<>();

        // Create a single stack of the combined count for each item

        for (int i = 0; i < fluidInputs.getTankProperties().length; i++) {
            FluidStack fluidStack = fluidInputs.getTankProperties()[i].getContents();
            if (fluidStack != null && fluidStack.amount > 0) {
                FluidKey key = new FluidKey(fluidStack);
                map.put(key, map.getInt(key) + fluidStack.amount);
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
        final Object2IntMap<FluidKey> map = new Object2IntLinkedOpenHashMap<>();

        // Create a single stack of the combined count for each item

        for (FluidStack fluidStack : fluidInputs) {
            if (fluidStack != null && fluidStack.amount > 0) {
                FluidKey key = new FluidKey(fluidStack);
                map.put(key, map.getInt(key) + fluidStack.amount);
            }
        }

        return map;
    }
}
