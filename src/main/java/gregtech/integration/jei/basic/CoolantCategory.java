package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.Minecraft;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.Nullable;

public class CoolantCategory extends BasicRecipeCategory<CoolantInfo, CoolantInfo> {

    private final IDrawable icon;
    protected final IDrawable slot;
    private final IDrawable arrow;

    public CoolantCategory(IGuiHelper guiHelper) {
        super("coolant", "fission.coolant.name", guiHelper.createBlankDrawable(176, 90), guiHelper);

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
    public void setRecipe(IRecipeLayout recipeLayout, CoolantInfo recipeWrapper, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

        fluidStackGroup.init(0, true, 55, 9);
        fluidStackGroup.set(0, recipeWrapper.coolant);
        fluidStackGroup.init(1, true, 105, 9);
        fluidStackGroup.set(1, recipeWrapper.hotCoolant);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 54, 8);
        slot.draw(minecraft, 104, 8);
        arrow.draw(minecraft, 77, 6);
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(CoolantInfo recipe) {
        return recipe;
    }
}
