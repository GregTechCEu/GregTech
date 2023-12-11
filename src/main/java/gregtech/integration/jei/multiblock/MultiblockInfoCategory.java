package gregtech.integration.jei.multiblock;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

import net.minecraft.client.resources.I18n;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiblockInfoCategory implements IRecipeCategory<MultiblockInfoRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper guiHelper;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        this.guiHelper = helpers.getGuiHelper();
        this.background = this.guiHelper.createBlankDrawable(176, 166);
        this.icon = guiHelper.drawableBuilder(GuiTextures.MULTIBLOCK_CATEGORY.imageLocation, 0, 0, 16, 16)
                .setTextureSize(16, 16).build();
    }

    public static final List<MultiblockControllerBase> REGISTER = new LinkedList<>();

    public static void registerMultiblock(MultiblockControllerBase controllerBase) {
        REGISTER.add(controllerBase);
    }

    public static void registerRecipes(IModRegistry registry) {
        registry.addRecipes(REGISTER.stream().map(MultiblockInfoRecipeWrapper::new).collect(Collectors.toList()),
                "gregtech:multiblock_info");
    }

    @NotNull
    @Override
    public String getUid() {
        return "gregtech:multiblock_info";
    }

    @NotNull
    @Override
    public String getTitle() {
        return I18n.format("gregtech.multiblock.title");
    }

    @NotNull
    @Override
    public String getModName() {
        return GTValues.MODID;
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, MultiblockInfoRecipeWrapper recipeWrapper,
                          @NotNull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) recipeLayout, this.guiHelper);
    }
}
