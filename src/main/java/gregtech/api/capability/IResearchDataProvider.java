package gregtech.api.capability;

import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IResearchDataProvider {

    /**
     * @param recipe Recipe to be processed
     * @return true if the recipe can process
     */
    boolean canRecipeProcess(Recipe recipe);

    /**
     * @return a list of ItemStacks that an assembly line can craft
     */
    List<ItemStack> getValidOutputs();

    /**
     * @param newOutput the ItemStack that will be added to the list
     */
    void addValidOutput(ItemStack newOutput);

    /**
     * fuction to clear the list of validOutputs
     */
    void clearValidOutputs();
}
