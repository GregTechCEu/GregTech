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
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return ore == ((MapOreDictIngredient) obj).ore;
        }
        return false;
    }

}
