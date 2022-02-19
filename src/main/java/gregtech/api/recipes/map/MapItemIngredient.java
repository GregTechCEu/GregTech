package gregtech.api.recipes.map;

import net.minecraft.item.Item;

public class MapItemIngredient extends AbstractMapIngredient {

    public Item stack;

    public MapItemIngredient(Item stack, boolean insideMap) {
        super(insideMap);
        this.stack = stack;
    }

    @Override
    protected int hash() {
        return stack.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        //if (o instanceof MapTagIngredient) {
        //    return stack.getTags().contains(((MapTagIngredient) o).loc);
        //}
        if (o instanceof MapItemIngredient) {
            return ((MapItemIngredient) o).stack.equals(stack);
        }
        return false;
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
