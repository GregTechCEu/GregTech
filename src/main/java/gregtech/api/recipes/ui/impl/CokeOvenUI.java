package gregtech.api.recipes.ui.impl;

import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ui.RecipeMapUI;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class CokeOvenUI<R extends RecipeMap<?>> extends RecipeMapUI<R> {

    /**
     * @param recipeMap the recipemap corresponding to this ui
     */
    public CokeOvenUI(@NotNull R recipeMap) {
        super(recipeMap, false, false, false, false, false);
    }

    @Override
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 MultipleTankHandler importFluids, MultipleTankHandler exportFluids,
                                                 int yOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 100)
                .widget(new ProgressWidget(200, 70, 19, 36, 18, GuiTextures.PROGRESS_BAR_COKE_OVEN,
                        ProgressWidget.MoveType.HORIZONTAL));
        addSlot(builder, 52, 10, 0, importItems, null, false, false);
        addSlot(builder, 106, 10, 0, exportItems, null, false, true);
        addSlot(builder, 106, 28, 0, null, exportFluids, true, true);
        return builder;
    }
}
