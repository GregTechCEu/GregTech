package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.integration.jei.recipe.primitive.BasicRecipeCategory;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.GTValues.MODID_AR;

public class GTFluidVeinCategory extends BasicRecipeCategory<GTFluidVeinInfo, GTFluidVeinInfo> {

    protected final IDrawable slot;
    private BedrockFluidDepositDefinition definition;
    private String veinName;
    private int weight;
    private int[] yields; // the [minimum, maximum) yields
    private int depletionAmount; // amount of fluid the vein gets drained by
    private int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    private int depletedYield; // yield after the vein is depleted
    private final int SLOT_CENTER = 79;
    protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    protected final Map<Integer, String> namedDimensions = WorldGenRegistry.getNamedDimensions();
    private final Supplier<List<Integer>> dimension = this::getAllRegisteredDimensions;
    private final int textStartX= 5;
    private int weightLength;
    private int minYieldLength;
    private int maxYieldLength;
    private int depletionChanceLength;
    private int depletionAmountLength;
    private int depletedYieldLength;
    private final int startPosY = 40;


    public GTFluidVeinCategory(IGuiHelper guiHelper) {
        super("fluid_spawn_location",
                "fluid.spawnlocation.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();

    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, GTFluidVeinInfo gtFluidVeinInfo, @Nonnull IIngredients ingredients) {

        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

        fluidStackGroup.init(0, true, SLOT_CENTER, 19, 16, 16, 1, false, null);

        fluidStackGroup.addTooltipCallback(gtFluidVeinInfo::addTooltip);
        fluidStackGroup.set(ingredients);

        this.definition = gtFluidVeinInfo.getDefinition();
        this.veinName = gtFluidVeinInfo.getName();
        this.weight = gtFluidVeinInfo.getWeight();
        this.yields = gtFluidVeinInfo.getYields();
        this.depletionAmount = gtFluidVeinInfo.getDepletionAmount();
        this.depletionChance = gtFluidVeinInfo.getDepletionChance();
        this.depletedYield = gtFluidVeinInfo.getDepletedYield();

    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull GTFluidVeinInfo gtFluidVeinInfo) {
        return gtFluidVeinInfo;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {

        String dimName;
        String fullDimName;
        int dimDisplayLength;

        drawVeinName(minecraft.fontRenderer);

        this.slot.draw(minecraft, SLOT_CENTER - 1, 18);

        // Vein Weight information
        String veinWeight = I18n.format("gregtech.jei.fluid.vein_weight") + " " + weight;
        weightLength = minecraft.fontRenderer.getStringWidth(veinWeight);
        minecraft.fontRenderer.drawString(veinWeight, textStartX, startPosY, 0x111111);

        // Vein Minimum Yield information
        String veinMinYield = I18n.format("gregtech.jei.fluid.min_yield") + " " + yields[0];
        minYieldLength = minecraft.fontRenderer.getStringWidth(veinMinYield);
        minecraft.fontRenderer.drawString(veinMinYield, textStartX, startPosY + FONT_HEIGHT + 1, 0x111111);

        // Vein Maximum Yield information
        String veinMaxYield = I18n.format("gregtech.jei.fluid.max_yield") + " " + yields[1];
        maxYieldLength = minecraft.fontRenderer.getStringWidth(veinMaxYield);
        minecraft.fontRenderer.drawString(veinMaxYield, textStartX, startPosY + 2 * FONT_HEIGHT + 1, 0x111111);

        // Vein Depletion Chance information
        String veinDepletionChance = I18n.format("gregtech.jei.fluid.depletion_chance") + " " + depletionChance;
        depletionChanceLength = minecraft.fontRenderer.getStringWidth(veinDepletionChance);
        minecraft.fontRenderer.drawString(veinDepletionChance, textStartX, startPosY + 3 * FONT_HEIGHT + 1, 0x111111);

        // Vein Depletion Amount information
        String veinDepletionAmount = I18n.format("gregtech.jei.fluid.depletion_amount") + " " + depletionAmount;
        depletionAmountLength = minecraft.fontRenderer.getStringWidth(veinDepletionAmount);
        minecraft.fontRenderer.drawString(veinDepletionAmount, textStartX, startPosY + 4 * FONT_HEIGHT + 1, 0x111111);

        // Vein Depleted Yield information
        String veinDepletedYield = I18n.format("gregtech.jei.fluid.depleted_rate") + " " + depletedYield;
        depletedYieldLength = minecraft.fontRenderer.getStringWidth(veinDepletedYield);
        minecraft.fontRenderer.drawString(veinDepletedYield, textStartX, startPosY + 5 * FONT_HEIGHT + 1, 0x111111);

        // Vein Dimensions information
        String veinDimension = I18n.format("gregtech.jei.fluid.dimension") + " ";
        int dimensionLength = minecraft.fontRenderer.getStringWidth(veinDimension);
        minecraft.fontRenderer.drawString(veinDimension, textStartX, startPosY + 6 * FONT_HEIGHT + 1, 0x111111);

        List<Integer> dimensionIDs = dimension.get();

        //Will attempt to write dimension IDs in a single line, separated by commas. If the list is so long such that it
        //would run off the end of the page, the list is continued on a new line.
        int dimStartYPos = startPosY + 6 * FONT_HEIGHT + 1;
        int dimDisplayPos = textStartX + dimensionLength;
        for (int i = 0; i < dimensionIDs.size(); i++) {

            //If the dimension name is included, append it to the dimension number
            if (namedDimensions.containsKey(dimensionIDs.get(i))) {
                dimName = namedDimensions.get(dimensionIDs.get(i));
                fullDimName = i == dimensionIDs.size() - 1 ?
                        dimensionIDs.get(i) + " (" + dimName + ")" :
                        dimensionIDs.get(i) + " (" + dimName + "), ";
            }
            //If the dimension name is not included, just add the dimension number
            else {

                fullDimName = i == dimensionIDs.size() - 1 ?
                        Integer.toString(dimensionIDs.get(i)) :
                        dimensionIDs.get(i) + ", ";
            }

            //Find the length of the dimension name string
            dimDisplayLength = minecraft.fontRenderer.getStringWidth(fullDimName);

            //If the length of the string would go off the edge of screen, instead increment the y position
            if (dimDisplayLength > (176 - dimDisplayPos)) {
                dimStartYPos = dimStartYPos + FONT_HEIGHT;
                dimDisplayPos = 70;
            }

            minecraft.fontRenderer.drawString(fullDimName, dimDisplayPos, dimStartYPos, 0x111111);

            //Increment the dimension name display position
            dimDisplayPos = dimDisplayPos + dimDisplayLength;
        }

    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {

        if(textStartX <= mouseX && mouseX <= weightLength && startPosY <= mouseY && mouseY <= startPosY + FONT_HEIGHT) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.weight_hover"));
        }
        else if(textStartX <= mouseX && mouseX <= minYieldLength && startPosY + FONT_HEIGHT + 1 <= mouseY && mouseY <= startPosY + 2 * FONT_HEIGHT + 1) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.min_hover"));
        }
        else if(textStartX <= mouseX && mouseX <= maxYieldLength && startPosY + 2 * FONT_HEIGHT + 1 <= mouseY && mouseY <= startPosY + 3 * FONT_HEIGHT + 1) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.max_hover"));
        }
        else if(textStartX <= mouseX && mouseX <= depletionChanceLength && startPosY + 3 * FONT_HEIGHT + 1 <= mouseY && mouseY <= startPosY + 4 * FONT_HEIGHT + 1) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_chance_hover"));
        }
        else if(textStartX <= mouseX && mouseX <= depletionAmountLength && startPosY + 4 * FONT_HEIGHT + 1 <= mouseY && mouseY <= startPosY + 5 * FONT_HEIGHT + 1) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_amount_hover"));
        }
        else if(textStartX <= mouseX && mouseX <= depletedYieldLength && startPosY + 5 * FONT_HEIGHT + 1 <= mouseY && mouseY <= startPosY + 6 * FONT_HEIGHT + 1) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_yield_hover"));
        }

        return Collections.emptyList();
    }

    public void drawVeinName(final FontRenderer fontRenderer) {
        final int maxVeinNameLength = 176;

        String veinNameToDraw = veinName;

        //Account for really long names
        if (fontRenderer.getStringWidth(veinNameToDraw) > maxVeinNameLength) {
            veinNameToDraw = fontRenderer.trimStringToWidth(veinName, maxVeinNameLength - 3, false) + "...";
        }

        //Ensure that the vein name is centered
        int startPosition = (maxVeinNameLength - fontRenderer.getStringWidth(veinNameToDraw)) / 2;

        fontRenderer.drawString(veinNameToDraw, startPosition, 1, 0x111111);
    }

    public List<Integer> getAllRegisteredDimensions() {
        List<Integer> dims = new ArrayList<>();
        /*
        Gather the registered dimensions here instead of at the top of the class to catch very late registered dimensions
        such as Advanced Rocketry
         */
        Map<DimensionType, IntSortedSet> dimMap = DimensionManager.getRegisteredDimensions();
        dimMap.values().stream()
                .flatMap(Collection::stream)
                .mapToInt(Integer::intValue)
                .filter(num -> definition.getDimensionFilter().test(DimensionManager.createProviderFor(num)))
                .forEach(dims::add);

        //Slight cleanup of the list if Advanced Rocketry is installed
        if (Loader.isModLoaded(MODID_AR)) {
            try {
                int[] spaceDims = DimensionManager.getDimensions(DimensionType.byName("space"));

                //Remove Space from the dimension list
                for (int spaceDim : spaceDims) {
                    if (dims.contains(spaceDim)) {
                        dims.remove((Integer) spaceDim);
                    }
                }
            } catch (IllegalArgumentException e) {
                GTLog.logger.error("Something went wrong with AR JEI integration, No DimensionType found");
                GTLog.logger.error(e);
            }
        }

        return dims;
    }
}
