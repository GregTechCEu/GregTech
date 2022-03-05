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
    protected final IDrawable arrowsChemBath;
    protected final IDrawable arrowsSeparator;
    protected final IDrawable arrowsSifter;
    protected final IDrawable icon;
    protected final List<Boolean> itemOutputExists = new ArrayList<>();
    protected final List<Boolean> fluidInputExists = new ArrayList<>();
    protected boolean hasDirectSmelt;
    //protected boolean hasChemBath;
    //protected boolean hasSeparator;
    protected boolean hasSifter;

    // XY positions of every item and fluid, in three enormous lists
    protected final static ImmutableList<Integer> ITEM_INPUT_LOCATIONS = ImmutableList.of(
            3, 3,       // ore
            23, 3,      // furnace (direct smelt)
            3, 24,      // macerator (ore -> crushed)
            23, 71,     // macerator (crushed -> impure)
            50, 80,     // centrifuge (impure -> dust)
            24, 25,     // ore washer
            97, 71,     // thermal centrifuge
            70, 80,     // macerator (centrifuged -> dust)
            114, 48,    // macerator (crushed purified -> purified)
            133, 71,    // centrifuge (purified -> dust)
            3, 123,     // cauldron / simple washer (crushed)
            41, 145,    // cauldron (impure)
            102, 145,   // cauldron (purified)
            24, 48,     // chem bath
            155, 71,    // electro separator
            101, 25     // sifter
    );

    protected final static ImmutableList<Integer> ITEM_OUTPUT_LOCATIONS = ImmutableList.of(
            46, 3,      // smelt result
            3, 47,      // ore -> crushed
            3, 65,      // byproduct
            23, 92,     // crushed -> impure
            23, 110,    // byproduct
            50, 101,    // impure -> dust
            50, 119,    // byproduct
            64, 25,     // crushed -> crushed purified (wash)
            82, 25,     // byproduct
            97, 92,     // crushed/crushed purified -> centrifuged
            97, 110,    // byproduct
            70, 101,    // centrifuged -> dust
            70, 119,    // byproduct
            137, 47,    // crushed purified -> purified
            155, 47,    // byproduct
            133, 92,    // purified -> dust
            133, 110,   // byproduct
            3, 105,     // crushed cauldron
            3, 145,     // -> purified crushed
            23, 145,    // impure cauldron
            63, 145,    // -> dust
            84, 145,    // purified cauldron
            124, 145,   // -> dust
            64, 48,     // crushed -> crushed purified (chem bath)
            82, 48,     // byproduct
            155, 92,    // purified -> dust (electro separator)
            155, 110,   // byproduct 1
            155, 128,   // byproduct 2
            119, 3,     // sifter outputs...
            137, 3,
            155, 3,
            119, 21,
            137, 21,
            155, 21
    );

    protected final static ImmutableList<Integer> FLUID_LOCATIONS = ImmutableList.of(
            42, 25, // washer in
            42, 48  // chem bath in
    );

    public OreByProductCategory(IGuiHelper guiHelper) {
        super("ore_by_product",
                "recipemap.byproductlist.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
        this.fluidSlot = guiHelper.drawableBuilder(GuiTextures.FLUID_SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();

        String baseloc = GTValues.MODID + ":textures/gui/arrows/";
        this.arrowsBase = guiHelper.drawableBuilder(new ResourceLocation(baseloc + "oreby-base.png"), 0, 0, 176, 166)
                .setTextureSize(176, 166).build();
        this.arrowsDirectSmelt = guiHelper.drawableBuilder(new ResourceLocation(baseloc + "oreby-smelt.png"), 0, 0, 176, 166)
                .setTextureSize(176, 166).build();
        this.arrowsChemBath = guiHelper.drawableBuilder(new ResourceLocation(baseloc + "oreby-chem.png"), 0, 0, 176, 166)
                .setTextureSize(176, 166).build();
        this.arrowsSeparator = guiHelper.drawableBuilder(new ResourceLocation(baseloc + "oreby-sep.png"), 0, 0, 176, 166)
                .setTextureSize(176, 166).build();
        this.arrowsSifter = guiHelper.drawableBuilder(new ResourceLocation(baseloc + "oreby-sift.png"), 0, 0, 176, 166)
                .setTextureSize(176, 166).build();

        this.icon = guiHelper.createDrawableIngredient(OreDictUnifier.get(OrePrefix.ore, Materials.Iron));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @Nonnull OreByProduct recipeWrapper, @Nonnull IIngredients ingredients) {
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
        for (int i = 0; i < FLUID_LOCATIONS.size(); i += 2) {
            fluidStackGroup.init(i / 2, true, new FluidStackTextRenderer(1, false, 16, 16, null),
                    FLUID_LOCATIONS.get(i) + 1, FLUID_LOCATIONS.get(i + 1) + 1, 16, 16, 0, 0);
            fluidInputExists.add(fluidInputs.get(i / 2).size() > 0);
        }

        itemStackGroup.addTooltipCallback(recipeWrapper::addTooltip);
        itemStackGroup.set(ingredients);
        fluidStackGroup.set(ingredients);

        hasDirectSmelt = recipeWrapper.hasDirectSmelt();
        //hasChemBath = recipeWrapper.hasChemBath();
        //hasSeparator = recipeWrapper.hasSeparator();
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
        if (hasDirectSmelt) {
            arrowsDirectSmelt.draw(minecraft, 0, 0);
        }
        //if (hasChemBath) {
        //    arrowsChemBath.draw(minecraft, 0, 0);
        //}
        //if (hasSeparator) {
        //    arrowsSeparator.draw(minecraft, 0, 0);
        //}
        if(hasSifter) {
            arrowsSifter.draw(minecraft, 0, 0);
        }

        // only draw slot on inputs if it is the ore
        slot.draw(minecraft, ITEM_INPUT_LOCATIONS.get(0), ITEM_INPUT_LOCATIONS.get(1));

        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            // stupid hack to show all sifter slots if the first one exists
            if (itemOutputExists.get(i / 2) || (i > 28 * 2 && itemOutputExists.get(28) && hasSifter)) {
                slot.draw(minecraft, ITEM_OUTPUT_LOCATIONS.get(i), ITEM_OUTPUT_LOCATIONS.get(i + 1));
            }
        }

        for (int i = 0; i < FLUID_LOCATIONS.size(); i += 2) {
            if (fluidInputExists.get(i / 2)) {
                fluidSlot.draw(minecraft, FLUID_LOCATIONS.get(i), FLUID_LOCATIONS.get(i + 1));
            }
        }
    }

}
