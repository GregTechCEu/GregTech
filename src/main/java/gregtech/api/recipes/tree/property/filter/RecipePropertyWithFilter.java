package gregtech.api.recipes.tree.property.filter;

import gregtech.api.recipes.properties.RecipeProperty;

public abstract class RecipePropertyWithFilter<T> extends RecipeProperty<T> implements IPropertyFilter<T> {

    protected RecipePropertyWithFilter(String key, Class<T> type) {
        super(key, type);
    }
}
