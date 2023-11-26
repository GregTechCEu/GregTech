package gregtech.api.recipes.machines;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleSupplier;

public class RecipeMapResearchStation<R extends RecipeBuilder<R>> extends RecipeMap<R> implements IScannerRecipeMap {

    public RecipeMapResearchStation(@NotNull String unlocalizedName, int maxInputs, int maxOutputs,
                                    int maxFluidInputs, int maxFluidOutputs, @NotNull R defaultRecipeBuilder,
                                    boolean isHidden) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipeBuilder, isHidden);
    }

    @Override
    @NotNull
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        Pair<DoubleSupplier, DoubleSupplier> pairedSuppliers = RecipeMapCrackerUnit.createPairedSupplier(200, 90, 0.75);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .widget(new ImageWidget(10, 0, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .widget(new ProgressWidget(pairedSuppliers.getLeft(), 72, 28, 54, 5,
                        GuiTextures.PROGRESS_BAR_RESEARCH_STATION_1, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new ProgressWidget(pairedSuppliers.getRight(), 119, 32, 10, 18,
                        GuiTextures.PROGRESS_BAR_RESEARCH_STATION_2, ProgressWidget.MoveType.VERTICAL_DOWNWARDS))
                .widget(new SlotWidget(importItems, 0, 115, 50, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY))
                .widget(new SlotWidget(importItems, 1, 43, 21, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.SCANNER_OVERLAY))
                .widget(new SlotWidget(exportItems, 0, 97, 21, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.RESEARCH_STATION_OVERLAY));
    }
}
