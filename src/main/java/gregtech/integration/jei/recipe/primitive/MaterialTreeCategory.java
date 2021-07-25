package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.integration.jei.utils.render.DrawableRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
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
	protected final IDrawable icon;
	protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

    protected List<Boolean> itemExists = new ArrayList<>();
    protected List<Boolean> fluidExists = new ArrayList<>();
    // XY positions of ingredients
	protected final static ImmutableList<Integer> ITEM_LOCATIONS = ImmutableList.of(
	        // corresponds pair-to-one with PREFIXES in MaterialTree.java
            4, 101,   // dust 0
            4, 67,    // dustSmall
            4, 135,   // dustTiny
            29, 67,   // ingotHot
            29, 101,  // ingot
            29, 101,  // gem (override) 5
            29, 135,  // block
            54, 55,   // stick
            79, 67,   // stickLong
            79, 101,  // spring
            79, 101,  // gemFlawless (override) 10
            104, 67,  // bolt
            104, 101, // screw
            104, 101, // gemExquisite (override)
            129, 55,  // ring
            129, 85,  // gear 15
            129, 117, // frameGt
            54, 85,   // nugget
            54, 117,  // round
            54, 147,  // plate
            79, 135,  // plateDense 20
            79, 135,  // gemChipped (override)
            104, 135, // gearSmall
            104, 135, // gemFlawed (override)
            129, 147, // foil
            129, 147, // lens (override) 25
            154, 147, // wireFine
            154, 117, // wireGtSingle
            154, 85   //cableGtSingle 28
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
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Item.getByNameOrId("minecraft:iron_ingot")));

        /* couldn't think of a better way to register all these
        generated with bash, requires imagemagick and sed
        for file in ./*.png; do
            dimstring=$(identify -ping -format '%w, %h' "$file")
            basename "$file" .png | sed "s/\(.*\)/registerArrow(guiHelper, \"\1\", $dimstring);/"
        done
        */
        registerArrow(guiHelper, "dh_down_16", 5, 16);
        registerArrow(guiHelper, "dh_right_down_16_26", 18, 28);
        registerArrow(guiHelper, "down_12", 5, 12);
        registerArrow(guiHelper, "down_14", 5, 14);
        registerArrow(guiHelper, "down_16", 5, 16);
        registerArrow(guiHelper, "down_right_8_17", 17, 10);
        registerArrow(guiHelper, "left_up_16_5", 18, 5);
        registerArrow(guiHelper, "right_57", 57, 5);
        registerArrow(guiHelper, "right_7", 7, 5);
        registerArrow(guiHelper, "right_down_16_5", 18, 5);
        registerArrow(guiHelper, "right_down_41_5", 43, 5);
        registerArrow(guiHelper, "right_down_right_3_17_4", 7, 19);
        registerArrow(guiHelper, "right_down_right_3_43_4", 7, 45);
        registerArrow(guiHelper, "right_down_right_53_31_4", 57, 33);
        registerArrow(guiHelper, "right_down_right_53_64_4", 57, 66);
        registerArrow(guiHelper, "right_up_16_5", 18, 5);
        registerArrow(guiHelper, "right_up_41_5", 43, 5);
        registerArrow(guiHelper, "right_up_right_3_17_4", 7, 19);
        registerArrow(guiHelper, "right_up_right_3_35_4", 7, 37);
        registerArrow(guiHelper, "right_up_right_3_47_4", 7, 49);
        registerArrow(guiHelper, "up_14", 5, 14);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MaterialTree recipeWrapper, IIngredients ingredients) {
        // place and check existence of items
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		List<List<ItemStack>> itemInputs = ingredients.getInputs(ItemStack.class);
		itemExists.clear();
		for (int i = 0; i < ITEM_LOCATIONS.size(); i+=2) {
			itemStackGroup.init(i, true, ITEM_LOCATIONS.get(i), ITEM_LOCATIONS.get(i + 1));
            itemExists.add(itemInputs.get(i / 2).size() > 0);
		}
		itemStackGroup.set(ingredients);

		// place and check existence of fluid(s)
		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
		List<List<FluidStack>> fluidInputs = ingredients.getInputs(FluidStack.class);
		fluidExists.clear();
        for (int i = 0; i < FLUID_LOCATIONS.size(); i+=2) {
            // fluids annoyingly need to be offset by 1 to fit in the slot graphic
    		fluidStackGroup.init(0, true, FLUID_LOCATIONS.get(i) + 1, FLUID_LOCATIONS.get(i + 1) + 1);
    		fluidExists.add(fluidInputs.get(i / 2).size() > 0);
        }
		fluidStackGroup.set(ingredients);

		// set info of current material
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

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
	public void drawExtras(Minecraft minecraft) {
        // item slot rendering
    	for (int i = 0; i < ITEM_LOCATIONS.size(); i+=2){
    	    if(itemExists.get(i / 2))
        		this.slot.draw(minecraft, ITEM_LOCATIONS.get(i), ITEM_LOCATIONS.get(i + 1));
		}

    	// fluid slot rendering
    	for (int i = 0; i < FLUID_LOCATIONS.size(); i+=2){
    	    if(fluidExists.get(i / 2))
        		this.slot.draw(minecraft, FLUID_LOCATIONS.get(i), FLUID_LOCATIONS.get(i + 1));
		}

    	// arrow rendering, aka hardcoded jank
        // indeces are from ITEM_LOCATIONS / MaterialTree.PREFIXES
        // wire -> cable
        drawArrow(minecraft, "up_14", 160, 103, itemExists.get(27) && itemExists.get(28));
    	// wire -> fine wire
        drawArrow(minecraft, "down_12", 160, 135, itemExists.get(27) && itemExists.get(26));
        // nugget -> round
        drawArrow(minecraft, "down_14", 60, 103, itemExists.get(17) && itemExists.get(18));
        // hot ingot -> ingot
        drawArrow(minecraft, "down_16", 35, 85, itemExists.get(3) && itemExists.get(4));
        // long rod -> spring
        drawArrow(minecraft, "down_16", 85, 85, itemExists.get(8) && itemExists.get(9));
        // bolt -> screw
        drawArrow(minecraft, "down_16", 110, 85, itemExists.get(11) && itemExists.get(12));
        // dust -> ingot/gem
        // only if no hot ingot
        drawArrow(minecraft, "right_7", 22, 107, !itemExists.get(3) &&
                itemExists.get(0) && (itemExists.get(4) || itemExists.get(5)));
        // foil -> fine wire
        drawArrow(minecraft, "right_7", 147, 153, itemExists.get(24) && itemExists.get(26));
        // rod -> ring
        drawArrow(minecraft, "right_57", 72, 60, itemExists.get(7) && itemExists.get(14));
        // plate -> foil/lens
        drawArrow(minecraft, "right_57", 72, 155, itemExists.get(19) &&
                (itemExists.get(24) || itemExists.get(25)));
        // small dust <-> dust
        drawArrow(minecraft, "dh_down_16", 10, 85, itemExists.get(1) && itemExists.get(0));
        // tiny dust <-> dust
        drawArrow(minecraft, "dh_down_16", 10, 119, itemExists.get(2) && itemExists.get(0));
        // block <-> ingot/gem
        drawArrow(minecraft, "dh_down_16", 35, 119, itemExists.get(6) &&
                (itemExists.get(4) || itemExists.get(5)));
        // block -> plate
        drawArrow(minecraft, "down_right_8_17", 37, 153, itemExists.get(6) && itemExists.get(19));
        // long rod -> rod
        drawArrow(minecraft, "left_up_16_5", 61, 73, itemExists.get(8) && itemExists.get(7));
        // plate -> dense plate
        drawArrow(minecraft, "right_up_16_5", 72, 153, itemExists.get(19) && itemExists.get(20));
        // plate -> small gear
        drawArrow(minecraft, "right_up_41_5", 72, 153, itemExists.get(19) && itemExists.get(22));
        // rod -> long rod
        drawArrow(minecraft, "right_down_16_5", 72, 62, itemExists.get(7) && itemExists.get(8));
        // rod -> bolt
        drawArrow(minecraft, "right_down_41_5", 72, 62, itemExists.get(7) && itemExists.get(11));
        // dust <-> block
        // only if no ingot or gem
        drawArrow(minecraft, "dh_right_down_16_26", 22, 107, !itemExists.get(4) &&
                !itemExists.get(5) && itemExists.get(0) && itemExists.get(6));
        // dust -> hot ingot
        drawArrow(minecraft, "right_up_right_3_35_4", 22, 73, itemExists.get(0) && itemExists.get(3));
        // ingot -> plate
        drawArrow(minecraft, "right_down_right_3_43_4", 47, 109, itemExists.get(4) && itemExists.get(19));
        // ingot -> nugget
        drawArrow(minecraft, "right_up_right_3_17_4", 47, 91, itemExists.get(4) && itemExists.get(17));
        // ingot -> round
        drawArrow(minecraft, "right_down_right_3_17_4", 47, 109, itemExists.get(4) && itemExists.get(18));
        // rod -> gear
        drawArrow(minecraft, "right_down_right_53_31_4", 72, 62, itemExists.get(7) && itemExists.get(15));
        // ingot/gem -> rod
        drawArrow(minecraft, "right_up_right_3_47_4", 47, 61, itemExists.get(7) &&
                (itemExists.get(4) || itemExists.get(5)));
        // rod -> frame box
        drawArrow(minecraft, "right_down_right_53_64_4", 72, 62, itemExists.get(7) && itemExists.get(16));

        // material info rendering
		minecraft.fontRenderer.drawString(materialName, 0, 0, 0x111111);
		if (minecraft.fontRenderer.getStringWidth(materialFormula) > 176) {
		    minecraft.fontRenderer.drawString(minecraft.fontRenderer.trimStringToWidth(materialFormula, 171) + "...",
                    0, FONT_HEIGHT, 0x111111);
        } else {
            minecraft.fontRenderer.drawString(materialFormula, 0, FONT_HEIGHT, 0x111111);
        }
		// don't think theres a good way to get the coil tier other than this
		if (!materialBFTemp.equals("N/A")) {
            BlastTemperatureProperty.getInstance().drawInfo(minecraft, 0, 2 * FONT_HEIGHT, 0x111111, Integer.valueOf(materialBFTemp));
        }
		minecraft.fontRenderer.drawString(materialAvgM, 0, 3 * (FONT_HEIGHT), 0x111111);
		minecraft.fontRenderer.drawString(materialAvgN, 0, 4 * (FONT_HEIGHT), 0x111111);
		minecraft.fontRenderer.drawString(materialAvgP, 0, 5 * (FONT_HEIGHT), 0x111111);
	}

	// a couple wrappers to make the code look less terrible
	private void registerArrow(IGuiHelper guiHelper, String name, int width, int height) {
        DrawableRegistry.initDrawable(guiHelper, GTValues.MODID + ":textures/gui/arrows/" + name + ".png", width, height, name);
    }

    private void drawArrow(Minecraft minecraft, String name, int x, int y, boolean shown) {
        if (shown)
            DrawableRegistry.drawDrawable(minecraft, name, x, y);
    }
}
