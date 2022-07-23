package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import gregtech.integration.jei.utils.render.ItemStackTextRenderer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OreByProductCategory extends BasicRecipeCategory<OreByProduct, OreByProduct> {

    protected final IDrawable slot;
    protected final IDrawable fluidSlot;
    protected final IDrawable arrowsBase;
    protected final IDrawable arrowsDirectSmelt;
    protected final IDrawable arrowsMercuryBath;
    protected final IDrawable arrowsPersulfateBath;
    protected final IDrawable arrowsVitriol;
    protected final IDrawable arrowsSifter;
    protected final IDrawable icon;
    protected final List<Boolean> itemOutputExists = new ArrayList<>();
    protected final List<Boolean> fluidInputExists = new ArrayList<>();
    protected final List<Boolean> fluidOutputExists = new ArrayList<>();
    protected boolean hasDirectSmelt;
    protected boolean hasMercuryBath;
    protected boolean hasPersulfateBath;
    protected boolean hasVitriol;
    protected boolean hasSifter;

    // XY positions of every item and fluid, in three enormous lists
    protected final static ImmutableList<Integer> ITEM_INPUT_LOCATIONS = ImmutableList.of(
            3, 3,       // ore
            24, 3,      // furnace (direct smelt)
            3, 25,      // macerator (ore -> crushed)
            24, 25,     // ore washer
            3, 92,      // macerator (crushed -> impure)
            24, 47,     // chem bath (mercury)
            24, 69,     // chem bath (persulfate)
            86, 92,     // macerator (crushed purified -> dust)
            68, 92,     // thermal centrifuge (crushed purified -> refined)
            108, 25,    // sifter
            68, 3,      // chem bath (vitriol)
            86, 110,    // macerator (refined -> dust)
            24, 110     // chem bath (impure -> dust)
    );

    protected final static ImmutableList<Integer> ITEM_OUTPUT_LOCATIONS = ImmutableList.of(
            46, 3,      // smelt result (furnace)
            3, 47,      // ore -> crushed (macerate)
            3, 65,      // byproduct
            3, 110,     // crushed -> impure (macerate)
            3, 128,     // byproduct
            68, 25,     // crushed -> crushed purified (water wash)
            86, 25,     // byproduct
            68, 47,     // crushed -> crushed purified (mercury wash)
            86, 47,     // byproduct
            68, 69,     // crushed -> crushed purified (persulfate wash)
            86, 69,     // byproduct
            108, 92,    // crushed purified -> dust (macerator)
            126, 92,    // byproduct
            144, 92,    // byproduct
            68, 110,    // crushed purified -> refined (thermal centrifuge)
            68, 128,    // byproduct
            108, 47,    // sifter gems...
            126, 47,    // sifter gems...
            144, 47,    // sifter gems...
            108, 65,    // sifter gems...
            126, 65,    // sifter gems...
            144, 65,    // sifter gems...
            108, 3,     // refined (vitriol wash)
            126, 3,     // refined (vitriol wash)
            108, 110,   // refined -> dust (macerator)
            126, 110,   // byproduct
            144, 110,   // byproduct
            46, 128     // impure dust -> dust
    );

    protected final static ImmutableList<Integer> FLUID_INPUT_LOCATIONS = ImmutableList.of(
            46, 25,  // water wash in
            46, 47,  // mercury bath in
            46, 69,  // persulfate bath in
            86, 3,   // acid bath in
            46, 110  // water dust wash in
    );

    protected final static ImmutableList<Integer> FLUID_OUTPUT_LOCATIONS = ImmutableList.of(
            144, 3   // vitriol bath out
    );

    public OreByProductCategory(IGuiHelper guiHelper) {
        super("ore_by_product", "recipemap.byproductlist.name", guiHelper.createBlankDrawable(176, 148), guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
        this.fluidSlot = guiHelper.drawableBuilder(GuiTextures.FLUID_SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();

        final String base = String.format("%s:textures/gui/arrows/orebyproduct_%%s_arrows.png", GTValues.MODID);
        this.arrowsBase = guiHelper.drawableBuilder(new ResourceLocation(String.format(base, "base")), 0, 0, 176, 166)
                .setTextureSize(176, 166).build();
        this.arrowsDirectSmelt = guiHelper.drawableBuilder(new ResourceLocation(String.format(base, "direct_smelt")), 0, 0, 32, 32)
                .setTextureSize(32, 32).build();
        this.arrowsMercuryBath = guiHelper.drawableBuilder(new ResourceLocation(String.format(base, "bath")), 0, 0, 64, 64)
                .setTextureSize(64, 64).build();
        this.arrowsPersulfateBath = guiHelper.drawableBuilder(new ResourceLocation(String.format(base, "bath")), 0, 0, 64, 64)
                .setTextureSize(64, 64).build();
        this.arrowsVitriol = guiHelper.drawableBuilder(new ResourceLocation(String.format(base, "acid_bath")), 0, 0, 32, 32)
                .setTextureSize(32, 32).build();
        this.arrowsSifter = guiHelper.drawableBuilder(new ResourceLocation(String.format(base, "sifting")), 0, 0, 64, 64)
                .setTextureSize(64, 64).build();

        this.icon = guiHelper.createDrawableIngredient(OreDictUnifier.get(OrePrefix.ore, Materials.Chalcopyrite));
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull OreByProduct recipeWrapper, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

        List<List<ItemStack>> itemInputs = ingredients.getInputs(VanillaTypes.ITEM);
        for (int i = 0; i < ITEM_INPUT_LOCATIONS.size(); i += 2) {
            itemStackGroup.init(i / 2, true, ITEM_INPUT_LOCATIONS.get(i), ITEM_INPUT_LOCATIONS.get(i + 1));
        }

        List<List<ItemStack>> itemOutputs = ingredients.getOutputs(VanillaTypes.ITEM);
        itemOutputExists.clear();
        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            itemStackGroup.init(i / 2 + itemInputs.size(), false, new ItemStackTextRenderer(recipeWrapper.getChance(i / 2 + itemInputs.size())),
                    ITEM_OUTPUT_LOCATIONS.get(i) + 1, ITEM_OUTPUT_LOCATIONS.get(i + 1) + 1, 16, 16, 0, 0);
            itemOutputExists.add(itemOutputs.get(i / 2).size() > 0);
        }

        List<List<FluidStack>> fluidInputs = ingredients.getInputs(VanillaTypes.FLUID);
        fluidInputExists.clear();
        for (int i = 0; i < FLUID_INPUT_LOCATIONS.size(); i += 2) {
            fluidStackGroup.init(i / 2, true, new FluidStackTextRenderer(1, false, 16, 16, null),
                    FLUID_INPUT_LOCATIONS.get(i) + 1, FLUID_INPUT_LOCATIONS.get(i + 1) + 1, 16, 16, 0, 0);
            fluidInputExists.add(fluidInputs.get(i / 2).size() > 0);
        }

        List<List<FluidStack>> fluidOutputs = ingredients.getOutputs(VanillaTypes.FLUID);
        fluidOutputExists.clear();
        for (int i = 0; i < FLUID_OUTPUT_LOCATIONS.size(); i += 2) {
            fluidStackGroup.init(i / 2 + fluidInputs.size(), false, new FluidStackTextRenderer(1, false, 16, 16, null),
                    FLUID_OUTPUT_LOCATIONS.get(i) + 1, FLUID_OUTPUT_LOCATIONS.get(i + 1) + 1, 16, 16, 0, 0);
            fluidOutputExists.add(fluidOutputs.get(i / 2).size() > 0);
        }

        itemStackGroup.addTooltipCallback(recipeWrapper::addTooltip);
        itemStackGroup.set(ingredients);
        fluidStackGroup.set(ingredients);

        hasDirectSmelt = recipeWrapper.hasDirectSmelt();
        hasMercuryBath = recipeWrapper.hasMercuryBath();
        hasPersulfateBath = recipeWrapper.hasPersulfateBath();
        hasVitriol = recipeWrapper.hasVitriol();
        hasSifter = recipeWrapper.hasSifter();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull OreByProduct recipe) {
        return recipe;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        arrowsBase.draw(minecraft, 0, 0);
        if (hasDirectSmelt) arrowsDirectSmelt.draw(minecraft, 21, 10);
        if (hasMercuryBath) arrowsMercuryBath.draw(minecraft, 24, 55);
        if (hasPersulfateBath) arrowsPersulfateBath.draw(minecraft, 24, 76);
        if (hasVitriol) arrowsVitriol.draw(minecraft, 77, 10);
        if (hasSifter) arrowsSifter.draw(minecraft, 77, 23);

        // only draw slot on inputs if it is the ore
        slot.draw(minecraft, ITEM_INPUT_LOCATIONS.get(0), ITEM_INPUT_LOCATIONS.get(1));

        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            // stupid hack to show all sifter slots if the first one exists
            if (itemOutputExists.get(i / 2) || (i > 28 * 2 && itemOutputExists.get(28) && hasSifter)) {
                slot.draw(minecraft, ITEM_OUTPUT_LOCATIONS.get(i), ITEM_OUTPUT_LOCATIONS.get(i + 1));
            }
        }

        for (int i = 0; i < FLUID_INPUT_LOCATIONS.size(); i += 2) {
            if (fluidInputExists.get(i / 2)) {
                fluidSlot.draw(minecraft, FLUID_INPUT_LOCATIONS.get(i), FLUID_INPUT_LOCATIONS.get(i + 1));
            }
        }
        for (int i = 0; i < FLUID_OUTPUT_LOCATIONS.size(); i +=2) {
            if (fluidOutputExists.get(i / 2)) {
                fluidSlot.draw(minecraft, FLUID_OUTPUT_LOCATIONS.get(i), FLUID_OUTPUT_LOCATIONS.get(i + 1));
            }
        }
    }
}
