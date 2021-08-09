package gregtech.integration.jei.recipe.primitive;

import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.recipes.PrimitiveBlastFurnaceRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class PrimitiveBlastRecipeCategory extends PrimitiveRecipeCategory<PrimitiveBlastFurnaceRecipe, PrimitiveBlastRecipeWrapper> {

    protected final IDrawable slot;
    protected final IDrawable progressBar;

    public PrimitiveBlastRecipeCategory(IGuiHelper guiHelper) {
        super("primitive_blast_furnace",
                "gregtech.machine.primitive_blast_furnace.bronze.name",
                guiHelper.createBlankDrawable(140, 60), guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
        this.progressBar = guiHelper.drawableBuilder(GuiTextures.BRONZE_BLAST_FURNACE_PROGRESS_BAR.imageLocation, 0, 0, 20, 15).setTextureSize(20, 30).build();
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @Nonnull PrimitiveBlastRecipeWrapper recipeWrapper,
                          @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        itemStackGroup.init(0, true, 32, 4);
        itemStackGroup.init(1, true, 32, 22);

        itemStackGroup.init(2, false, 84, 13);
        itemStackGroup.init(3, false, 102, 13);
        itemStackGroup.set(ingredients);
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull PrimitiveBlastFurnaceRecipe recipe) {
        return new PrimitiveBlastRecipeWrapper(recipe);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        this.slot.draw(minecraft, 32, 4);
        this.slot.draw(minecraft, 32, 22);
        this.slot.draw(minecraft, 84, 13);
        this.slot.draw(minecraft, 102, 13);
        this.progressBar.draw(minecraft, 57, 14);
    }
}
