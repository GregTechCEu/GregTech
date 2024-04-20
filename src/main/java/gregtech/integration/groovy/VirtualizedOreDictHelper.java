package gregtech.integration.groovy;

import gregtech.api.recipes.ingredients.GTRecipeOreInput;

import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

/**
 * Subscribe to After Script Load, refresh Ore Dict Ingredients if JEI is not loaded/enabled.
 * JEI is refreshed after script load, thus if JEI module is enabled, this is not needed.
 */
public class VirtualizedOreDictHelper extends VirtualizedRegistry<String> {

    @Override
    public void onReload() {}

    @Override
    public void afterScriptLoad() {
        GTRecipeOreInput.refreshStackCache();
    }
}
