package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.api.util.GTLog;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.ArrayList;

public class MaterialTreeCategory extends PrimitiveRecipeCategory<MaterialTree, MaterialTree> {

	protected String materialName;
	protected String materialFormula;
	protected String materialBFTemp;
	protected String materialAvgM;
	protected String materialAvgP;
	protected String materialAvgN;
	protected final IDrawable slot;
	protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

    protected List<Boolean> itemExists = new ArrayList<>();
    protected List<Boolean> fluidExists = new ArrayList<>();
	protected ImmutableList<Integer> ITEM_LOCATIONS = ImmutableList.of(
	        // corresponds pair-to-one with PREFIXES in MaterialTree.java
            4, 101,   // dust
            4, 67,    // dustSmall
            4, 135,   // dustTiny
            29, 67,   // ingotHot
            29, 101,  // ingot
            29, 101,  // gem (override)
            29, 135,  // block
            54, 55,   // stick
            79, 67,   // stickLong
            79, 101,  // spring
            79, 101,  // gemFlawless (override)
            104, 67,  // bolt
            104, 101, // screw
            104, 101, // gemExquisite (override)
            129, 55,  // ring
            129, 85,  // gear
            129, 117, // frameGt
            54, 85,   // nugget
            54, 117,  // round
            54, 147,  // plate
            79, 135,  // plateDense
            79, 135,  // gemChipped (override)
            104, 135, // gearSmall
            104, 135, // gemFlawed (override)
            129, 147, // foil
            129, 147, // lens (override)
            154, 147, // wireFine
            154, 117, // wireGtSingle
            154, 85   //cableGtSingle
    );
	protected ImmutableList<Integer> FLUID_LOCATIONS = ImmutableList.of(
	        154, 55    // fluid
    );

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
		List<List<ItemStack>> itemInputs = ingredients.getInputs(ItemStack.class);
		itemExists.clear();
		for (int i = 0; i < ITEM_LOCATIONS.size(); i+=2) {
			itemStackGroup.init(i, true, ITEM_LOCATIONS.get(i), ITEM_LOCATIONS.get(i + 1));
            itemExists.add(itemInputs.get(i / 2).size() > 0);
		}
		itemStackGroup.set(ingredients);

		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
		List<List<FluidStack>> fluidInputs = ingredients.getInputs(FluidStack.class);
		fluidExists.clear();
        for (int i = 0; i < FLUID_LOCATIONS.size(); i+=2) {
    		fluidStackGroup.init(0, true, FLUID_LOCATIONS.get(i) + 1, FLUID_LOCATIONS.get(i + 1) + 1);
    		fluidExists.add(fluidInputs.get(i / 2).size() > 0);
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
    	for (int i = 0; i < ITEM_LOCATIONS.size(); i+=2){
    	    if(itemExists.get(i / 2))
        		this.slot.draw(minecraft, ITEM_LOCATIONS.get(i), ITEM_LOCATIONS.get(i + 1));
		}
    	for (int i = 0; i < FLUID_LOCATIONS.size(); i+=2){
    	    if(fluidExists.get(i / 2))
        		this.slot.draw(minecraft, FLUID_LOCATIONS.get(i), FLUID_LOCATIONS.get(i + 1));
		}
		minecraft.fontRenderer.drawString(materialName, 0, 0, 0x111111);
		minecraft.fontRenderer.drawString(materialFormula, 0, FONT_HEIGHT, 0x111111);
		// don't think theres a good way to get the coil tier other than this
		if (!materialBFTemp.equals("N/A")) {
            BlastTemperatureProperty.getInstance().drawInfo(minecraft, 0, 2 * FONT_HEIGHT, 0x111111, Integer.valueOf(materialBFTemp));
        }
		minecraft.fontRenderer.drawString(materialAvgM, 0, 3 * (FONT_HEIGHT), 0x111111);
		minecraft.fontRenderer.drawString(materialAvgN, 0, 4 * (FONT_HEIGHT), 0x111111);
		minecraft.fontRenderer.drawString(materialAvgP, 0, 5 * (FONT_HEIGHT), 0x111111);
	}
}
