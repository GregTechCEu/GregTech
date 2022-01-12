package gregtech.api.capability.impl;

import gregtech.api.capability.IResearchDataProvider;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.ResearchItemProperty;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventortyResearchDataProvider implements IResearchDataProvider {
    private final List<ItemStack> validOutputs = new ArrayList<>();
    @Override
    public boolean canRecipeProcess(Recipe recipe) {
        if (recipe.hasProperty(ResearchItemProperty.getInstance())) {
            boolean canRun = false;
            for (ItemStack itemStack : recipe.getOutputs()) {
                if (validOutputs.contains(itemStack)) {
                    canRun = true;
                    break;
                }
            }
            return canRun;
        } else {
            return true;
        }
    }

    @Override
    public List<ItemStack> getValidOutputs() {
        return validOutputs;
    }

    @Override
    public void addValidOutput(ItemStack newOutput) {
        validOutputs.add(newOutput);
    }

    @Override
    public void clearValidOutputs() {
        validOutputs.clear();
    }
}
