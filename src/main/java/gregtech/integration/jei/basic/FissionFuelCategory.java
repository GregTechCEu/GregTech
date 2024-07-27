package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.api.nuclear.fission.FissionFuelRegistry;
import gregtech.api.nuclear.fission.IFissionFuelStats;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.Nullable;

public class FissionFuelCategory extends BasicRecipeCategory<FissionFuelInfo, FissionFuelInfo> {

    private final IDrawable icon;
    protected final IDrawable slot;
    private final IDrawable arrow;

    public FissionFuelCategory(IGuiHelper guiHelper) {
        super("fission_fuel", "fission.fuel.name", guiHelper.createBlankDrawable(176, 90), guiHelper);

        this.icon = guiHelper.createDrawableIngredient(MetaTileEntities.FISSION_REACTOR.getStackForm());
        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
        this.arrow = guiHelper.drawableBuilder(GuiTextures.PROGRESS_BAR_ARROW.imageLocation, 0, 20, 20, 20)
                .setTextureSize(20, 40).build();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FissionFuelInfo recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        itemStackGroup.init(0, true, 54, 8);
        itemStackGroup.set(0, recipeWrapper.rod);
        itemStackGroup.init(1, true, 104, 8);
        itemStackGroup.set(1, recipeWrapper.depletedRod);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 54, 8);
        slot.draw(minecraft, 104, 8);
        arrow.draw(minecraft, 77, 6);

    }

    @Override
    public IRecipeWrapper getRecipeWrapper(FissionFuelInfo recipe) {
        return recipe;
    }
}
