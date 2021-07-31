package gregtech.integration.jei.recipe.primitive;

import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.recipes.CokeOvenRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

public class CokeOvenRecipeCategory extends PrimitiveRecipeCategory<CokeOvenRecipe, CokeOvenRecipeWrapper> {

    protected final IDrawable slot;
    protected final IDrawable progressBar;
	protected final IDrawable fluidTank;
	protected final IDrawable fluidTankOverlay;

	
	public CokeOvenRecipeCategory(IGuiHelper guiHelper) {
		super("coke_oven",
            "gregtech.machine.coke_oven.name",
            guiHelper.createBlankDrawable(176, 166), guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
        this.progressBar = guiHelper.drawableBuilder(GuiTextures.BRONZE_BLAST_FURNACE_PROGRESS_BAR.imageLocation, 0, 0, 20, 15).setTextureSize(20, 30).build();
		this.fluidTank = guiHelper.drawableBuilder(GuiTextures.FLUID_TANK_BACKGROUND.imageLocation, 0, 0, 20, 58).setTextureSize(20, 58).build();
		this.fluidTankOverlay = guiHelper.drawableBuilder(GuiTextures.FLUID_TANK_OVERLAY.imageLocation, 0, 0, 20, 58).setTextureSize(20, 58).build();
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CokeOvenRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
		itemStackGroup.init(0, true, 32, 19);
		itemStackGroup.init(1, false, 84, 19);
		itemStackGroup.set(ingredients);
		fluidStackGroup.init(0, false, 133, 3, 20, 58, 32000, true, this.fluidTankOverlay);
		fluidStackGroup.set(ingredients);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(CokeOvenRecipe recipe) {
		return new CokeOvenRecipeWrapper(recipe);
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		this.slot.draw(minecraft, 32, 19);
		this.slot.draw(minecraft, 84, 19);
		this.progressBar.draw(minecraft, 57, 20);
		this.fluidTank.draw(minecraft, 133, 3);
	}
}
