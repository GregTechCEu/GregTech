package gregtech.api.recipes.ui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class RecipeMapUIBuilder {

    private final RecipeMapUI<?> mapUI;

    public RecipeMapUIBuilder(RecipeMapUI<?> mapUI) {
        this.mapUI = mapUI;
        this.mapUI.setUsesMui2();
    }

    /**
     * @param progressBar the progress bar texture to use
     * @return this
     */
    public @NotNull RecipeMapUIBuilder progressBar(@NotNull UITexture progressBar) {
        this.mapUI.setProgressBarTexture(progressBar);
        return this;
    }

    /**
     * @param moveType the progress bar move type to use
     * @return this
     */
    public @NotNull RecipeMapUIBuilder progressDirection(@NotNull ProgressWidget.Direction moveType) {
        this.mapUI.setProgressBarDirection(moveType);
        return this;
    }

    /**
     * @param progressBar the progress bar texture to use
     * @param moveType    the progress bar move type to use
     * @return this
     */
    public @NotNull RecipeMapUIBuilder progressBar(@NotNull UITexture progressBar,
                                                   @NotNull ProgressWidget.Direction moveType) {
        return progressBar(progressBar).progressDirection(moveType);
    }

    /**
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder itemSlotOverlay(@NotNull IDrawable texture, boolean isOutput) {
        int max = getMax(false, isOutput);
        for (int i = 0; i < max; i++) {
            slotOverlay(texture, i, false, isOutput);
        }
        return this;
    }

    /**
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder itemSlotOverlay(@NotNull IDrawable texture, int index, boolean isOutput) {
        return slotOverlay(texture, index, false, isOutput);
    }

    /**
     * @param texture    the texture to use
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder itemSlotOverlay(@NotNull IDrawable texture,
                                                       boolean isOutput,
                                                       boolean isLastSlot) {
        int max = getMax(false, isOutput);
        if (isLastSlot) {
            return slotOverlay(texture, max - 1, false, isOutput);
        } else for (int i = 0; i < max - 1; i++) {
            slotOverlay(texture, i, false, isOutput);
        }
        return this;
    }

    /**
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder fluidSlotOverlay(@NotNull IDrawable texture,
                                                        boolean isOutput) {
        int max = getMax(true, isOutput);
        for (int i = 0; i < max; i++) {
            slotOverlay(texture, i, true, isOutput);
        }
        return this;
    }

    /**
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder fluidSlotOverlay(@NotNull IDrawable texture,
                                                        int index,
                                                        boolean isOutput) {
        return slotOverlay(texture, index, true, isOutput);
    }

    /**
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder fluidSlotOverlay(@NotNull IDrawable texture,
                                                        boolean isOutput,
                                                        boolean isLastSlot) {
        int max = getMax(true, isOutput);
        if (isLastSlot) {
            return slotOverlay(texture, max - 1, true, isOutput);
        } else for (int i = 0; i < max - 1; i++) {
            slotOverlay(texture, i, true, isOutput);
        }
        return this;
    }

    /**
     * @param texture  the texture to use
     * @param index    the slot index
     * @param isFluid  if this slot is fluid
     * @param isOutput if this slot is an output
     * @return this
     */
    public @NotNull RecipeMapUIBuilder slotOverlay(@NotNull IDrawable texture, int index, boolean isFluid,
                                                   boolean isOutput) {
        this.mapUI.setSlotOverlay(texture, index, isFluid, isOutput);
        return this;
    }

    public @NotNull RecipeMapUIBuilder specialTexture(@NotNull UITexture texture,
                                                      int x, int y,
                                                      int width, int height) {
        return specialTexture(texture, new Area(x, y, width, height));
    }

    public @NotNull RecipeMapUIBuilder specialTexture(@NotNull UITexture texture, @NotNull Area area) {
        this.mapUI.setSpecialTexture(texture, area);
        return this;
    }

    private int getMax(boolean isFluid, boolean isOutput) {
        var map = mapUI.recipeMap();
        if (isOutput) return isFluid ? map.getMaxFluidOutputs() : map.getMaxOutputs();
        else return isFluid ? map.getMaxFluidInputs() : map.getMaxInputs();
    }
}
