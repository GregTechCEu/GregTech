package gregtech.integration.jei.utils;

import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;

import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;

public class MultiblockInfoRecipeFocusShower implements IShowsRecipeFocuses {

    @Override
    public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
        if (MultiblockInfoRecipeWrapper.getHoveredItemStack() != null)
            return ClickedIngredient.create(MultiblockInfoRecipeWrapper.getHoveredItemStack(), null);
        return null;
    }

    @Override
    public boolean canSetFocusWithMouse() {
        return false;
    }
}
