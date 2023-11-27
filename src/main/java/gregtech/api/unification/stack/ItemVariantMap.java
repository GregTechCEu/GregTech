package gregtech.api.unification.stack;

import gregtech.api.GTValues;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * An abstraction of dictionary-like collection with each item variant as keys.
 * <p>
 * Despite the name, the actual implementation of the class may be unrelated to maps.
 * Nonetheless, this class supports map-like access to whatever form of data they're
 * holding, with additional methods like {@link #hasNonWildcardEntry()} to help
 * certain operations special to item variants.
 *
 * @param <E> type of the elements
 * @see SingleItemVariantMap
 * @see MultiItemVariantMap
 */
public interface ItemVariantMap<E> {

    /**
     * @return {@code true} if there's any nonnull value associated with some item
     *         metadata, excluding metadata value of {@link GTValues#W} {@code (32767)}.
     * @see #hasWildcardEntry()
     */
    boolean hasNonWildcardEntry();

    /**
     * @param meta item metadata
     * @return {@code true} if there's a nonnull value associated with given item
     *         metadata, {@code false} otherwise.
     */
    boolean has(short meta);

    /**
     * Get value associated with given item metadata. The associated value is always
     * nonnull; if {@code null} is returned, it indicates the metadata is not associated
     * with any value at the moment.
     *
     * @param meta item metadata
     * @return value associated with given item metadata, or {@code null} if there's no
     *         values associated.
     */
    @Nullable
    E get(short meta);

    /**
     * @return {@code true} if there's no value associated with any item metadata.
     */
    default boolean isEmpty() {
        return !hasWildcardEntry() && !hasNonWildcardEntry();
    }

    /**
     * @return {@code true} if there's a nonnull value associated with item
     *         metadata {@link GTValues#W} {@code (32767)}.
     * @see #hasNonWildcardEntry()
     */
    default boolean hasWildcardEntry() {
        return has(GTValues.W);
    }

    /**
     * @param stack item stack
     * @return {@code true} if there's a nonnull value associated with item damage of
     *         the item, {@code false} otherwise.
     */
    default boolean has(@NotNull ItemStack stack) {
        return has((short) stack.getItemDamage());
    }

    /**
     * Get value associated with item damage of the item. The associated value is always
     * nonnull; if {@code null} is returned, it indicates the metadata is not associated
     * with any value at the moment.
     *
     * @param stack item stack
     * @return value associated with item damage of the item, or {@code null} if there's
     *         no values associated.
     */
    @Nullable
    default E get(@NotNull ItemStack stack) {
        return get((short) stack.getItemDamage());
    }

    /**
     * Returns an unmodifiable view of {@code map}. The returned variant map will return set of elements
     * wrapped with {@link java.util.Collections#unmodifiableSet(Set) Collections#unmodifiableSet}.
     *
     * @param map variant map to wrap
     * @param <E> type of the element
     * @return an unmodifiable view of {@code map} with {@link Set} of elements as values.
     */
    @NotNull
    static <E> ItemVariantMap<Set<E>> unmodifiableSetView(@NotNull ItemVariantMap<? extends Set<E>> map) {
        return new UnmodifiableSetViewVariantMap<>(map);
    }

    /**
     * @param <E> type of the element
     * @return an unmodifiable instance of variant map with no entries.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    static <E> ItemVariantMap<E> empty() {
        return (ItemVariantMap<E>) EmptyVariantMap.INSTANCE;
    }

    /**
     * {@link ItemVariantMap} with methods for modification.
     *
     * @param <E> type of the elements
     */
    interface Mutable<E> extends ItemVariantMap<E> {

        /**
         * Discard all associated value contained in this variant map.
         */
        void clear();

        /**
         * Associates the item metadata to given value; previous association will be
         * overwritten. If {@code null} was provided as value, the association is removed
         * instead.
         *
         * @param meta item metadata
         * @param e    new value, or {@code null} for entry removal
         * @return previous value associated with given item metadata, or {@code null} if
         *         there was no such value.
         */
        @Nullable
        E put(short meta, @Nullable E e);

        /**
         * Associates item damage of the item to given value; previous association will be
         * overwritten. If {@code null} was provided as value, the association is removed
         * instead.
         *
         * @param stack item stack
         * @param e     new value, or {@code null} for entry removal
         * @return previous value associated with given item metadata, or {@code null} if
         *         there was no such value.
         */
        @Nullable
        default E put(@NotNull ItemStack stack, @Nullable E e) {
            return put((short) stack.getItemDamage(), e);
        }
    }
}
