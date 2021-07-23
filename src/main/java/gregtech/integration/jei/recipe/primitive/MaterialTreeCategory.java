package gregtech.integration.jei.recipe.primitive;

import gregtech.api.gui.GuiTextures;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

public class MaterialTreeCategory extends PrimitiveRecipeCategory<MaterialTree, MaterialTree> {

	protected final IDrawable slot;

    public MaterialTreeCategory(IGuiHelper guiHelper) {
        super("material_tree",
                "recipemap.materialtree.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.createDrawable(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18, 18, 18);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MaterialTree recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		for (int i = 0; i < recipeWrapper.getPrefixListSize(); i++) {
			itemStackGroup.init(i, true, (i % 9) * 20, (i / 9) * 20);
		}
		itemStackGroup.set(ingredients);

    }

    @Override
    public IRecipeWrapper getRecipeWrapper(MaterialTree recipe) {
        return recipe;
    }

	@Override
	public void drawExtras(Minecraft minecraft) {
    	for (int i = 0; i < 176; i += 20){
    		for (int j = 0; j < 166; j += 20){
    			this.slot.draw(minecraft, i, j);
			}
		}
	}
}
