package gregtech.api.unification.stack;

import org.jetbrains.annotations.Nullable;

/**
 * An unmodifiable {@link ItemVariantMap} instance with no elements.
 *
 * @see ItemVariantMap#empty()
 */
final class EmptyVariantMap implements ItemVariantMap<Object> {

    static final EmptyVariantMap INSTANCE = new EmptyVariantMap();

    @Override
    public boolean hasNonWildcardEntry() {
        return false;
    }

    @Override
    public boolean has(short meta) {
        return false;
    }

    @Nullable
    @Override
    public Object get(short meta) {
        return null;
    }

    @Override
    public String toString() {
        return "EmptyEntry";
    }
}
