package gregtech.api.recipes.ui;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.ui.RecipeMapUI.computeOverlayKey;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class RecipeMapUIBuilder {

    private final RecipeMapUI<?> mapUI;

    public RecipeMapUIBuilder(RecipeMapUI<?> mapUI) {
        this.mapUI = mapUI;
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
    public @NotNull RecipeMapUIBuilder itemSlotOverlay(@NotNull UITexture texture, boolean isOutput) {
        itemSlotOverlay(texture, isOutput, false);
        itemSlotOverlay(texture, isOutput, true);
        return this;
    }

    /**
     * @param texture    the texture to use
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder itemSlotOverlay(@NotNull UITexture texture, boolean isOutput,
                                                       boolean isLastSlot) {
        return slotOverlay(texture, isOutput, false, isLastSlot);
    }

    /**
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder fluidSlotOverlay(@NotNull UITexture texture, boolean isOutput) {
        fluidSlotOverlay(texture, isOutput, false);
        fluidSlotOverlay(texture, isOutput, true);
        return this;
    }

    /**
     * @param texture    the texture to use
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder fluidSlotOverlay(@NotNull UITexture texture, boolean isOutput,
                                                        boolean isLastSlot) {
        return slotOverlay(texture, isOutput, true, isLastSlot);
    }

    /**
     * @param texture    the texture to use
     * @param isOutput   if the slot is an output slot
     * @param isFluid    if the slot is a fluid slot
     * @param isLastSlot if the slot is the last slot
     * @return this
     */
    public @NotNull RecipeMapUIBuilder slotOverlay(@NotNull UITexture texture, boolean isOutput,
                                                   boolean isFluid, boolean isLastSlot) {
        this.mapUI.setSlotOverlay(computeOverlayKey(isOutput, isFluid, isLastSlot), texture);
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
}
