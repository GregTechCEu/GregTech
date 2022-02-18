package gregtech.api.recipes.map;

public abstract class AbstractMapIngredient {

    private final Class<? extends AbstractMapIngredient> objClass;
    private final boolean insideMap;

    private int hash;
    private boolean hashed = false;

    protected AbstractMapIngredient(boolean insideMap) {
        this.objClass = getClass();
        this.insideMap = insideMap;
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
        if (obj instanceof AbstractMapIngredient) {
            AbstractMapIngredient ing = (AbstractMapIngredient) obj;
            if (ing.insideMap && this.insideMap) {
                return this.objClass == ing.objClass;
            } else {
                return true;
            }
        }
        return false;
    }

}
