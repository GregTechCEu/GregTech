package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.util.ResourceLocation;

public class MaterialTreeCategory extends PrimitiveRecipeCategory<MaterialTree, MaterialTree> {

	protected String materialName;
	protected String materialFormula;
	protected String materialBFTemp;
	protected String materialAvgM;
	protected String materialAvgP;
	protected String materialAvgN;
	protected final IDrawable slot;
	protected final IDrawable arrows;
	protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

	protected ImmutableList<Integer> ITEM_LOCATIONS = ImmutableList.of(
	        // corresponds one-to-one with PREFIXES in MaterialTree.java
	        4, 55,    // ore
            4, 101,   // crushed
            29, 55,   // crushedCentrifuged
            29, 85,   // crushedPurified
            29, 117,  // dustPure
            29, 147,  // dustImpure
            54, 101,  // dust
            54, 67,   // dustSmall
            54, 135,  // dustTiny
            79, 85,   // ingotHot
            79, 117,  // ingot
            79, 117,  // gem (mutually exclusive with ingot)
            79, 147,  // nugget
            104, 124, // block
            104, 147, // round
            104, 101, // plate,
            129, 147, // plateDense
            129, 117, // lens
            154, 147, // foil
            104, 78,  // stick
            129, 85,  // stickLong
            129, 55,  // ring
            104, 55,  // bolt
            79, 55,   // screw
            154, 117, // wireFine
            154, 85,  // wireGtSingle
            154, 55   // cableGtSingle
    );
	protected ImmutableList<Integer> FLUID_LOCATIONS = ImmutableList.of(
	        4, 147    // fluid
    );

    public MaterialTreeCategory(IGuiHelper guiHelper) {
        super("material_tree",
                "recipemap.materialtree.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.createDrawable(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18, 18, 18);
        this.arrows = guiHelper.createDrawable(new ResourceLocation("gregtech:textures/gui/base/material_tree_arrows.png"),
                0, 0, 168, 110, 168, 110);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MaterialTree recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		for (int i = 0; i < ITEM_LOCATIONS.size() / 2; i++) {
			itemStackGroup.init(i, true, ITEM_LOCATIONS.get(2 * i), ITEM_LOCATIONS.get(2 * i + 1));
		}
		itemStackGroup.set(ingredients);

		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
        for (int i = 0; i < FLUID_LOCATIONS.size() / 2; i++) {
    		fluidStackGroup.init(0, true, FLUID_LOCATIONS.get(2 * i) + 1, FLUID_LOCATIONS.get(2 * i + 1) + 1);
        }
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
    	for (int i = 0; i < ITEM_LOCATIONS.size() / 2; i++){
    		this.slot.draw(minecraft, ITEM_LOCATIONS.get(2 * i), ITEM_LOCATIONS.get(2 * i + 1));
		}
    	for (int i = 0; i < FLUID_LOCATIONS.size() / 2; i++){
    		this.slot.draw(minecraft, FLUID_LOCATIONS.get(2 * i), FLUID_LOCATIONS.get(2 * i + 1));
		}
    	this.arrows.draw(minecraft, 4, 55);
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
