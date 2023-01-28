package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTJEIUtility;
import gregtech.api.util.GTStringUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    private Supplier<List<Integer>> dimension;
    private final int textStartX = 5;
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

        this.dimension = GTUtility.getAllRegisteredDimensions(definition.getDimensionFilter());

        GTJEIUtility.cleanupDimensionList(this.dimension);

    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull GTFluidVeinInfo gtFluidVeinInfo) {
        return gtFluidVeinInfo;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {

        GTStringUtils.drawCenteredStringWithCutoff(veinName, minecraft.fontRenderer, 176);

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

        GTJEIUtility.drawMultiLineCommaSeparatedDimensionList(namedDimensions, dimension.get(), minecraft.fontRenderer, textStartX,  startPosY + 6 * FONT_HEIGHT + 1, textStartX + dimensionLength);

    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {

        if(GTUtility.isPointWithinRange(textStartX, startPosY, weightLength, FONT_HEIGHT, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.weight_hover"));
        }
        else if (GTUtility.isPointWithinRange(textStartX, startPosY + FONT_HEIGHT + 1, minYieldLength, FONT_HEIGHT + 1, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.min_hover"));
        }
        else if (GTUtility.isPointWithinRange(textStartX, startPosY + 2 * FONT_HEIGHT + 1, maxYieldLength, FONT_HEIGHT + 1, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.max_hover"));
        }
        else if (GTUtility.isPointWithinRange(textStartX, startPosY + 3 * FONT_HEIGHT + 1, depletionChanceLength, FONT_HEIGHT + 1, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_chance_hover"));
        }
        else if (GTUtility.isPointWithinRange(textStartX, startPosY + 4 * FONT_HEIGHT + 1, depletionAmountLength, FONT_HEIGHT + 1, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_amount_hover"));
        }
        else if (GTUtility.isPointWithinRange(textStartX, startPosY + 5 * FONT_HEIGHT + 1, depletedYieldLength, FONT_HEIGHT + 1, mouseX, mouseY)) {
            return Collections.singletonList(I18n.format("gregtech.jei.fluid.dep_yield_hover"));
        }

        return Collections.emptyList();
    }
}
