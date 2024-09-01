package gregtech.api.recipes.tree.property;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public final class PropertySet extends ObjectOpenCustomHashSet<IRecipeProperty> {

    public static PropertySet voltage(long voltage) {
        PropertySet set = new PropertySet();
        set.add(new VoltageProperty(voltage));
        return set;
    }

    private static final Hash.Strategy<IRecipeProperty> STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(IRecipeProperty o) {
            return o.propertyHash();
        }

        @Override
        public boolean equals(IRecipeProperty a, IRecipeProperty b) {
            if (a == null) {
                if (b == null) return true;
                else return b.propertyEquals(null);
            } else return a.propertyEquals(b);
        }
    };

    public PropertySet() {
        super(STRATEGY);
    }

}
