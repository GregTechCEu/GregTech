package gregtech.api.recipes.map;

import net.minecraftforge.oredict.OreDictionary;

public class MapOreDictIngredient extends AbstractMapIngredient {

    int ore;

    public MapOreDictIngredient(String ore) {
        this.ore = OreDictionary.getOreID(ore);
    }


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

    @Override
    public boolean oreDict() {
        return true;
    }
}
