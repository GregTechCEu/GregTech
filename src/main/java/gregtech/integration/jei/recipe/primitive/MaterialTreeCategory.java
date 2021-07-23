package gregtech.integration.jei.recipe.primitive;

import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class MaterialTreeCategory extends PrimitiveRecipeCategory<MaterialTree, MaterialTree> {

	protected String materialName;
	protected String materialFormula;
	protected String materialBFTemp;
	protected String materialAvgM;
	protected String materialAvgP;
	protected String materialAvgN;
	protected final IDrawable slot;
	protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

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
			itemStackGroup.init(i, true, (i % 7) * 25 + 4, (i / 7) * 23 + 55);
		}
		itemStackGroup.set(ingredients);

		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
		fluidStackGroup.init(0, true, 140, 120);
		fluidStackGroup.set(ingredients);

		materialName = recipeWrapper.getMaterialName();
		materialFormula = recipeWrapper.getMaterialFormula();
		materialBFTemp = recipeWrapper.getBlastTemp();
		materialAvgM = I18n.format("gregtech.jei.materials.average_mass", recipeWrapper.getAvgM());
		materialAvgP = I18n.format("gregtech.jei.materials.average_protons", recipeWrapper.getAvgP());
		materialAvgN = I18n.format("gregtech.jei.materials.average_neutrons", recipeWrapper.getAvgN());
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(MaterialTree recipe) {
        return recipe;
    }

	@Override
	public void drawExtras(Minecraft minecraft) {
    	for (int i = 4; i < 158; i += 25){
    		for (int j = 55; j < 148; j += 23){
    			this.slot.draw(minecraft, i, j);
			}
		}
		minecraft.fontRenderer.drawString(materialName, 0, 0, 0x111111);
		minecraft.fontRenderer.drawString(materialFormula, 0, FONT_HEIGHT, 0x111111);
		if (materialBFTemp.equals("N/A")) {
			minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.blast_furnace_temperature",
					0, "N/A"), 0, 2 * (FONT_HEIGHT), 0x111111);
		}
		// don't think theres a good way to get the coil tier other than this
		else {
			BlastTemperatureProperty.getInstance().drawInfo(minecraft, 0, 2 * FONT_HEIGHT, 0x111111, Integer.valueOf(materialBFTemp));
		}
		minecraft.fontRenderer.drawString(materialAvgM, 0, 3 * (FONT_HEIGHT), 0x111111);
		minecraft.fontRenderer.drawString(materialAvgN, 0, 4 * (FONT_HEIGHT), 0x111111);
		minecraft.fontRenderer.drawString(materialAvgP, 0, 5 * (FONT_HEIGHT), 0x111111);
	}
}
