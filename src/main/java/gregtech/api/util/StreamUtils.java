package gregtech.api.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Various Quality-of-Life methods for working with Java 8 Streams.
 */
public final class StreamUtils {

    /**
     * Creates a stream view of the actual contents of the inventory.
     * Caller must not modify the contents of the inventory returned by this stream.
     *
     * @param inventory the target inventory
     * @return a stream over the contents of the target inventory
     */
    public static Stream<ItemStack> streamFrom(IItemHandler inventory) {
        return StreamSupport.stream(iterableFrom(inventory).spliterator(),
                false);
    }

    /**
     * Simple Iterable supplier to permit iterating over an inventory using for-each semantics.
     *
     * @param inventory the target inventory
     * @return an Iterable over the slots of the specified inventory.
     * @throws UnsupportedOperationException if {@link Iterator#remove()} is called on the iterator returned by
     *                                       {@link Iterable#iterator()}
     * @see Iterator
     * @see Iterable
     */
    public static Iterable<ItemStack> iterableFrom(IItemHandler inventory) {
        return new Iterable<ItemStack>() {
            @Override
            public Iterator<ItemStack> iterator() {
                return new Iterator<ItemStack>() {
                    private int cursor = 0;

                    @Override
                    public boolean hasNext() {
                        return cursor < inventory.getSlots();
                    }

                    @Override
                    public ItemStack next() {
                        if (!hasNext())
                            throw new NoSuchElementException();

                        ItemStack next = inventory.getStackInSlot(cursor);
                        cursor++;
                        return next;
                    }
                };
            }

            @Override
            public Spliterator<ItemStack> spliterator() {
                return Spliterators.spliterator(iterator(), inventory.getSlots(), 0);
            }
        };
    }
}
