package gregtech.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.google.common.base.CaseFormat;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VirtualizedRecipeMap extends VirtualizedRegistry<Recipe> {

    private final RecipeMap<?> recipeMap;

    public VirtualizedRecipeMap(RecipeMap<?> recipeMap) {
        super(false, generateAliases(recipeMap.unlocalizedName));
        this.recipeMap = recipeMap;
        GroovyScriptCompat.getInstance().addRegistry(this);
    }

    public static String[] generateAliases(String name) {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add(name);
        aliases.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name));
        if (name.split("_").length > 2) {
            aliases.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
        }

        return aliases.toArray(new String[0]);
    }

    @Override
    public void onReload() {
        removeScripted().forEach(recipeMap::removeRecipe);
        restoreFromBackup().forEach(recipeMap::compileRecipe);
    }

    public RecipeMap<?> getRecipeMap() {
        return recipeMap;
    }

    public RecipeBuilder<?> recipeBuilder() {
        return this.recipeMap.recipeBuilder();
    }

    public String getName() {
        return this.recipeMap.unlocalizedName;
    }

    public SimpleObjectStream<Recipe> streamRecipes() {
        return new SimpleObjectStream<>(this.recipeMap.getRecipeList())
                .setRemover(this.recipeMap::removeRecipe);
    }

    public Recipe find(long voltage, List<ItemStack> items, List<FluidStack> fluids) {
        if (items == null || items.isEmpty()) items = Collections.emptyList();
        if (fluids == null || fluids.isEmpty()) fluids = Collections.emptyList();
        return this.recipeMap.findRecipe(voltage, items, fluids, Integer.MAX_VALUE, true);
    }

    public boolean removeByInput(long voltage, List<ItemStack> items, List<FluidStack> fluids) {
        Recipe recipe = find(voltage, items, fluids);
        if (recipe == null) {
            if (GroovyScriptCompat.isCurrentlyRunning()) {
                GroovyLog.msg("Error removing GregTech " + getName() + " recipe")
                        .add("could not find recipe for: voltage {}, items {}, fluids {}", voltage, items, fluids)
                        .error()
                        .post();
            }
            return false;
        }
        this.recipeMap.removeRecipe(recipe);
        return true;
    }
}
