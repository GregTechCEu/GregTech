package gregtech.api.recipes.ui.impl;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ui.RecipeMapUI;
import gregtech.api.util.GTUtility;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleSupplier;

@ApiStatus.Internal
public class CrackerUnitUI<R extends RecipeMap<?>> extends RecipeMapUI<R> {

    public CrackerUnitUI(@NotNull R recipeMap) {
        super(recipeMap, true, true, false, true, false);
        setFluidSlotOverlay(GuiTextures.CRACKING_OVERLAY_1, false);
        setFluidSlotOverlay(GuiTextures.CRACKING_OVERLAY_2, true);
        setItemSlotOverlay(GuiTextures.CIRCUIT_OVERLAY, false);
        setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, ProgressWidget.MoveType.HORIZONTAL);
    }

    @Override
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        if (recipeMap().getMaxInputs() == 1) {
            addSlot(builder, 52, 24 + yOffset, 0, importItems, importFluids, false, false);
        } else {
            int[] grid = determineSlotsGrid(recipeMap().getMaxInputs());
            for (int y = 0; y < grid[1]; y++) {
                for (int x = 0; x < grid[0]; x++) {
                    addSlot(builder, 34 + (x * 18) - (Math.max(0, grid[0] - 2) * 18),
                            24 + (y * 18) - (Math.max(0, grid[1] - 1) * 18) + yOffset,
                            y * grid[0] + x, importItems, importFluids, false, false);
                }
            }
        }

        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        addSlot(builder, 52, 24 + yOffset + 19 + 18, 0, importItems, importFluids, true, false);
        addSlot(builder, 34, 24 + yOffset + 19 + 18, 1, importItems, importFluids, true, false);

        Pair<DoubleSupplier, DoubleSupplier> suppliers = GTUtility.createPairedSupplier(200, 41, 0.5);
        builder.widget(new RecipeProgressWidget(suppliers.getLeft(), 42, 24 + yOffset + 18, 21, 19,
                GuiTextures.PROGRESS_BAR_CRACKING_INPUT, ProgressWidget.MoveType.VERTICAL, recipeMap()));
        builder.widget(new RecipeProgressWidget(suppliers.getRight(), 78, 23 + yOffset, 20, 20, progressBarTexture(),
                progressBarMoveType(), recipeMap()));
        return builder;
    }
}
