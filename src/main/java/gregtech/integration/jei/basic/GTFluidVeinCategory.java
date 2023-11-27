package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTStringUtils;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.integration.jei.utils.JEIResourceDepositCategoryUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class GTFluidVeinCategory extends BasicRecipeCategory<GTFluidVeinInfo, GTFluidVeinInfo> {

    private static final int SLOT_CENTER = 79;
    private static final int TEXT_START_X = 5;
    private static final int START_POS_Y = 40;

    protected final IDrawable slot;
    private String veinName;
    private int weight;
    private int[] yields; // the [minimum, maximum) yields
    private int depletionAmount; // amount of fluid the vein gets drained by
    private int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    private int depletedYield; // yield after the vein is depleted
    private int[] dimensions;
    private int weightLength;
    private int minYieldLength;
    private int maxYieldLength;
    private int depletionChanceLength;
    private int depletionAmountLength;
    private int depletedYieldLength;

    public GTFluidVeinCategory(IGuiHelper guiHelper) {
        super("fluid_spawn_location",
                "fluid.spawnlocation.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, GTFluidVeinInfo gtFluidVeinInfo,
                          @NotNull IIngredients ingredients) {
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

        fluidStackGroup.init(0, true, SLOT_CENTER, 19, 16, 16, 1, false, null);

        fluidStackGroup.addTooltipCallback(gtFluidVeinInfo::addTooltip);
        fluidStackGroup.set(ingredients);

        this.veinName = gtFluidVeinInfo.getName();
        this.weight = gtFluidVeinInfo.getWeight();
        this.yields = gtFluidVeinInfo.getYields();
        this.depletionAmount = gtFluidVeinInfo.getDepletionAmount();
        this.depletionChance = gtFluidVeinInfo.getDepletionChance();
        this.depletedYield = gtFluidVeinInfo.getDepletedYield();

        this.dimensions = JEIResourceDepositCategoryUtils.getAllRegisteredDimensions(
                gtFluidVeinInfo.getDefinition().getDimensionFilter());
    }

    @NotNull
    @Override
    public IRecipeWrapper getRecipeWrapper(@NotNull GTFluidVeinInfo gtFluidVeinInfo) {
        return gtFluidVeinInfo;
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        GTStringUtils.drawCenteredStringWithCutoff(veinName, minecraft.fontRenderer, 176);

        this.slot.draw(minecraft, SLOT_CENTER - 1, 18);

        // Vein Weight information
        String veinWeight = I18n.format("gregtech.jei.fluid.vein_weight", weight);
        weightLength = minecraft.fontRenderer.getStringWidth(veinWeight);
        minecraft.fontRenderer.drawString(veinWeight, TEXT_START_X, START_POS_Y, 0x111111);

        // Vein Minimum Yield information
        String veinMinYield = I18n.format("gregtech.jei.fluid.min_yield", yields[0]);
        minYieldLength = minecraft.fontRenderer.getStringWidth(veinMinYield);
        minecraft.fontRenderer.drawString(veinMinYield, TEXT_START_X, START_POS_Y + FONT_HEIGHT + 1, 0x111111);

        // Vein Maximum Yield information
        String veinMaxYield = I18n.format("gregtech.jei.fluid.max_yield", yields[1]);
        maxYieldLength = minecraft.fontRenderer.getStringWidth(veinMaxYield);
        minecraft.fontRenderer.drawString(veinMaxYield, TEXT_START_X, START_POS_Y + 2 * FONT_HEIGHT + 1, 0x111111);

        // Vein Depletion Chance information
        String veinDepletionChance = I18n.format("gregtech.jei.fluid.depletion_chance", depletionChance);
        depletionChanceLength = minecraft.fontRenderer.getStringWidth(veinDepletionChance);
        minecraft.fontRenderer.drawString(veinDepletionChance, TEXT_START_X, START_POS_Y + 3 * FONT_HEIGHT + 1,
                0x111111);

        // Vein Depletion Amount information
        String veinDepletionAmount = I18n.format("gregtech.jei.fluid.depletion_amount", depletionAmount);
        depletionAmountLength = minecraft.fontRenderer.getStringWidth(veinDepletionAmount);
        minecraft.fontRenderer.drawString(veinDepletionAmount, TEXT_START_X, START_POS_Y + 4 * FONT_HEIGHT + 1,
                0x111111);

        // Vein Depleted Yield information
        String veinDepletedYield = I18n.format("gregtech.jei.fluid.depleted_rate", depletedYield);
        depletedYieldLength = minecraft.fontRenderer.getStringWidth(veinDepletedYield);
        minecraft.fontRenderer.drawString(veinDepletedYield, TEXT_START_X, START_POS_Y + 5 * FONT_HEIGHT + 1, 0x111111);

        // Vein Dimensions information
        String veinDimension = I18n.format("gregtech.jei.fluid.dimension") + " ";
        int dimensionLength = minecraft.fontRenderer.getStringWidth(veinDimension);
        minecraft.fontRenderer.drawString(veinDimension, TEXT_START_X, START_POS_Y + 6 * FONT_HEIGHT + 1, 0x111111);

        JEIResourceDepositCategoryUtils.drawMultiLineCommaSeparatedDimensionList(WorldGenRegistry.getNamedDimensions(),
                dimensions,
                minecraft.fontRenderer,
                TEXT_START_X,
                START_POS_Y + 6 * FONT_HEIGHT + 1,
                TEXT_START_X + dimensionLength);
    }

    @NotNull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (isPointWithinRange(TEXT_START_X, START_POS_Y, weightLength, FONT_HEIGHT, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.weight_hover"));
        } else if (isPointWithinRange(TEXT_START_X, START_POS_Y + FONT_HEIGHT + 1, minYieldLength, FONT_HEIGHT + 1,
                mouseX, mouseY)) {
                    return Collections.singletonList(I18n.format("gregtech.jei.fluid.min_hover"));
                } else
            if (isPointWithinRange(TEXT_START_X, START_POS_Y + 2 * FONT_HEIGHT + 1, maxYieldLength, FONT_HEIGHT + 1,
                    mouseX, mouseY)) {
                        return Collections.singletonList(I18n.format("gregtech.jei.fluid.max_hover"));
                    } else
                if (isPointWithinRange(TEXT_START_X, START_POS_Y + 3 * FONT_HEIGHT + 1, depletionChanceLength,
                        FONT_HEIGHT + 1, mouseX, mouseY)) {
                            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_chance_hover"));
                        } else
                    if (isPointWithinRange(TEXT_START_X, START_POS_Y + 4 * FONT_HEIGHT + 1, depletionAmountLength,
                            FONT_HEIGHT + 1, mouseX, mouseY)) {
                                return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_amount_hover"));
                            } else
                        if (isPointWithinRange(TEXT_START_X, START_POS_Y + 5 * FONT_HEIGHT + 1, depletedYieldLength,
                                FONT_HEIGHT + 1, mouseX, mouseY)) {
                                    return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_yield_hover"));
                                }

        return Collections.emptyList();
    }

    /**
     * Checks if an (X,Y) point is within a defined box range
     *
     * @param initialX The initial X point of the box
     * @param initialY The initial Y point of the box
     * @param width    The width of the box
     * @param height   The height of the box
     * @param pointX   The X value of the point to check
     * @param pointY   The Y value of the point to check
     * @return True if the provided (X,Y) point is within the described box, else false
     */
    private static boolean isPointWithinRange(int initialX, int initialY, int width, int height, int pointX,
                                              int pointY) {
        return initialX <= pointX && pointX <= initialX + width && initialY <= pointY && pointY <= initialY + height;
    }
}
