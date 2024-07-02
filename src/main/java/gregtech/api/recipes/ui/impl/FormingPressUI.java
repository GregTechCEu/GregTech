package gregtech.api.recipes.ui.impl;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ui.RecipeMapUI;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class FormingPressUI<R extends RecipeMap<?>> extends RecipeMapUI<R> {

    public FormingPressUI(@NotNull R recipeMap) {
        super(recipeMap, true, true, true, true, false);
        setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL);
    }

    @Override
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler,
                           FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        SlotWidget slotWidget = new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs);
        TextureArea base = GuiTextures.SLOT;
        if (isOutputs)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_3);
        else if (slotIndex == 0 || slotIndex == 3)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_2);
        else if (slotIndex == 1 || slotIndex == 4)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_4);
        else if (slotIndex == 2 || slotIndex == 5)
            slotWidget.setBackgroundTexture(base, GuiTextures.PRESS_OVERLAY_1);

        builder.widget(slotWidget);
    }
}
