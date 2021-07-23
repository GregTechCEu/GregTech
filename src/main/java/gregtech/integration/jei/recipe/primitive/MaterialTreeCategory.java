package gregtech.integration.jei.recipe.primitive;

import gregtech.api.gui.GuiTextures;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
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
		for (int i = 0; i < recipeWrapper.getPrefixListSize() + 1; i++) {
			itemStackGroup.init(i, true, (i % 8) * 20, (i / 8) * 20);
		}
		itemStackGroup.set(ingredients);

		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
		fluidStackGroup.init(0, true, 140, 120);
		fluidStackGroup.set(ingredients);
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(MaterialTree recipe) {
        return recipe;
    }

	@Override
	public void drawExtras(Minecraft minecraft) {
    	for (int i = 0; i < 158; i += 20){
    		for (int j = 0; j < 148; j += 20){
    			this.slot.draw(minecraft, i, j);
			}
		}
	}
}
