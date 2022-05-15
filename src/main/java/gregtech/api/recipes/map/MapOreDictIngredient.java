package gregtech.api.recipes.map;

public class MapOreDictIngredient extends AbstractMapIngredient {

    int ore;

    public MapOreDictIngredient(int ore) {
        this.ore = ore;
    }

    @Override
    protected int hash() {
        return ore;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MapOreDictIngredient) {
            return ore == ((MapOreDictIngredient)other).ore;
        }
        return false;
    }
}
