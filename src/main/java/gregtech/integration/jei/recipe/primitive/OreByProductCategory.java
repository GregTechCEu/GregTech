package gregtech.integration.jei.recipe.primitive;

import gregtech.api.gui.GuiTextures;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class OreByProductCategory extends BasicRecipeCategory<OreByProduct, OreByProduct> {

    protected final IDrawable slot;
    protected final IDrawable arrowBackground;
    protected final IDrawableStatic arrowForeground;
    protected final IDrawableAnimated arrowAnimation;

    public OreByProductCategory(IGuiHelper guiHelper) {
        super("ore_by_product",
                "recipemap.byproductlist.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
        this.arrowBackground = guiHelper.drawableBuilder(GuiTextures.PROGRESS_BAR_ARROW.imageLocation, 0, 0, 20, 20).setTextureSize(20, 40).build();
        this.arrowForeground = guiHelper.drawableBuilder(GuiTextures.PROGRESS_BAR_ARROW.imageLocation, 0, 20, 20, 20).setTextureSize(20, 40).build();
        this.arrowAnimation = guiHelper.createAnimatedDrawable(arrowForeground, 30, StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, OreByProduct recipeWrapper, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        itemStackGroup.init(0, true, 22, 29); //Ore
        itemStackGroup.init(1, true, 70, 19); //Crushed
        itemStackGroup.init(2, true, 88, 19); //Crushed Purified
        itemStackGroup.init(3, true, 106, 19); //Crushed Centrifuged
        itemStackGroup.init(4, true, 70, 37); //Dust Impure
        itemStackGroup.init(5, true, 88, 37); //Dust Purified
        itemStackGroup.init(6, true, 106, 37); //Dust

        for (int i = 0; i < recipeWrapper.getOutputCount(); i++)
            itemStackGroup.init(i + 7, false, 70 + (i * 18), 59);

        itemStackGroup.addTooltipCallback(recipeWrapper::addTooltip);
        itemStackGroup.set(ingredients);
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull OreByProduct recipe) {
        return recipe;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        this.slot.draw(minecraft, 22, 29);
        this.arrowBackground.draw(minecraft, 44, 28);
        this.arrowAnimation.draw(minecraft, 44, 28);

        this.slot.draw(minecraft, 70, 19);
        this.slot.draw(minecraft, 88, 19);
        this.slot.draw(minecraft, 106, 19);
        this.slot.draw(minecraft, 70, 37);
        this.slot.draw(minecraft, 88, 37);
        this.slot.draw(minecraft, 106, 37);

        for (int i = 0; i < 4; i++)
            this.slot.draw(minecraft, 70 + (i * 18), 59);
    }

}
