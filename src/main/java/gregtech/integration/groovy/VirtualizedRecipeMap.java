package gregtech.integration.groovy;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.helper.Alias;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.google.common.base.CaseFormat;

import java.util.Collections;
import java.util.List;

public class VirtualizedRecipeMap extends VirtualizedRegistry<Recipe> {

    private final RecipeMap<?> recipeMap;

    public VirtualizedRecipeMap(RecipeMap<?> recipeMap) {
        super(Alias.generateOf(recipeMap.unlocalizedName, CaseFormat.LOWER_UNDERSCORE));
        this.recipeMap = recipeMap;
        GroovyScriptModule.getInstance().getVirtualizedRegistrar().addRegistry(this);
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
        return this.recipeMap.findRecipe(voltage, items, fluids, true);
    }

    public boolean removeByInput(long voltage, List<ItemStack> items, List<FluidStack> fluids) {
        Recipe recipe = find(voltage, items, fluids);
        if (recipe == null) {
            if (GroovyScriptModule.isCurrentlyRunning()) {
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
