package gregtech.api.recipes.map;

public abstract class AbstractMapIngredient {

    protected final Class<? extends AbstractMapIngredient> objClass;

    private int hash;
    private boolean hashed = false;

    protected AbstractMapIngredient() {
        this.objClass = getClass();
    }

    protected abstract int hash();

    @Override
    public final int hashCode() {
        if (!hashed) {
            hash = hash();
            hashed = true;
        }
        return hash;
    }

    protected final void invalidate() {
        this.hashed = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof AbstractMapIngredient) {
            return this.objClass == ((AbstractMapIngredient) obj).objClass;
        }
        return false;
    }

    public boolean isSpecialIngredient() {
        return false;
    }
}
