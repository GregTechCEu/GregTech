package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTStringUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.integration.jei.recipe.primitive.BasicRecipeCategory;
import gregtech.integration.jei.utils.render.ItemStackTextRenderer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static gregtech.api.GTValues.MODID_AR;

public class GTOreCategory extends BasicRecipeCategory<GTOreInfo, GTOreInfo> {

    protected final IDrawable slot;
    protected OreDepositDefinition definition;
    protected String veinName;
    protected int minHeight;
    protected int maxHeight;
    protected int outputCount;
    protected int weight;
    protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    protected final Map<Integer, String> namedDimensions = WorldGenRegistry.getNamedDimensions();
    private Supplier<List<Integer>> dimension;
    private final int NUM_OF_SLOTS = 5;
    private final int SLOT_WIDTH = 18;
    private final int SLOT_HEIGHT = 18;

    public GTOreCategory(IGuiHelper guiHelper) {
        super("ore_spawn_location",
                "ore.spawnlocation.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
    }


    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GTOreInfo recipeWrapper, @Nonnull IIngredients ingredients) {

        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        int baseYPos = 19;

        //The ore selected from JEI
        itemStackGroup.init(0, true, 22, baseYPos);
        //The Surface Identifier
        itemStackGroup.init(1, true, 22, 73);


        for (int i = 0; i < recipeWrapper.getOutputCount(); i++) {
            int yPos = baseYPos + (i / NUM_OF_SLOTS) * SLOT_HEIGHT;
            int xPos = 70 + (i % NUM_OF_SLOTS) * SLOT_WIDTH;

            itemStackGroup.init(i + 2, false,
                    new ItemStackTextRenderer(recipeWrapper.getOreWeight(i) * 100, -1),
                    xPos + 1, yPos + 1, 16, 16, 0, 0);
        }

        itemStackGroup.addTooltipCallback(recipeWrapper::addTooltip);
        itemStackGroup.set(ingredients);
        veinName = recipeWrapper.getVeinName();
        minHeight = recipeWrapper.getMinHeight();
        maxHeight = recipeWrapper.getMaxHeight();
        outputCount = recipeWrapper.getOutputCount();
        weight = recipeWrapper.getWeight();
        definition = recipeWrapper.getDefinition();

        this.dimension = GTUtility.getAllRegisteredDimensions(definition.getDimensionFilter());

        //Slight cleanup of the list if Advanced Rocketry is installed
        if (Loader.isModLoaded(MODID_AR)) {
            try {
                int[] spaceDims = DimensionManager.getDimensions(DimensionType.byName("space"));

                //Remove Space from the dimension list
                for (int spaceDim : spaceDims) {
                    if (this.dimension.get().contains(spaceDim)) {
                        this.dimension.get().remove((Integer) spaceDim);
                    }
                }
            } catch (IllegalArgumentException e) {
                GTLog.logger.error("Something went wrong with AR JEI integration, No DimensionType found");
                GTLog.logger.error(e);
            }
        }
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull GTOreInfo recipe) {
        return recipe;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {

        int baseXPos = 70;
        int baseYPos = 19;
        int dimDisplayPos = 70;

        //Selected Ore
        this.slot.draw(minecraft, 22, baseYPos);
        //Surface Identifier
        this.slot.draw(minecraft, 22, SLOT_HEIGHT * (NUM_OF_SLOTS - 1) + 1);

        int yPos = 0;
        for (int i = 0; i < outputCount; i++) {
            yPos = baseYPos + (i / NUM_OF_SLOTS) * SLOT_HEIGHT;
            int xPos = baseXPos + (i % NUM_OF_SLOTS) * SLOT_WIDTH;

            this.slot.draw(minecraft, xPos, yPos);
        }

        //base positions set to position of last rendered slot for later use.
        //Must account for the fact that yPos is the top corner of the slot, so add in another slot height
        baseYPos = yPos + SLOT_HEIGHT;

        GTStringUtils.drawCenteredStringWithCutoff(veinName, minecraft.fontRenderer, 176);

        //Begin Drawing information, depending on how many rows of ore outputs were created
        //Give room for 5 lines of 5 ores each, so 25 unique ores in the vein
        //73 is SLOT_HEIGHT * (NUM_OF_SLOTS - 1) + 1
        if (baseYPos >= SLOT_HEIGHT * NUM_OF_SLOTS) {
            minecraft.fontRenderer.drawString("Spawn Range: " + minHeight + "-" + maxHeight, 70, baseYPos + 1, 0x111111);
        } else {
            minecraft.fontRenderer.drawString("Spawn Range: " + minHeight + "-" + maxHeight, 70, SLOT_HEIGHT * (NUM_OF_SLOTS - 1) + 1, 0x111111);
            //Update the position at which the spawn information ends
            baseYPos = 73;
        }

        //Create the Weight
        minecraft.fontRenderer.drawString("Vein Weight: " + weight, 70, baseYPos + FONT_HEIGHT, 0x111111);

        //Create the Dimensions
        minecraft.fontRenderer.drawString("Dimensions: ", 70, baseYPos + (2 * FONT_HEIGHT), 0x111111);

        GTStringUtils.drawMultiLineCommaSeparatedDimensionList(namedDimensions, dimension.get(), baseYPos + 3 * FONT_HEIGHT, dimDisplayPos);

        //Label the Surface Identifier
        minecraft.fontRenderer.drawSplitString("SurfaceMaterial", 15, 92, minecraft.fontRenderer.getStringWidth("Surface"), 0x111111);

    }
}
