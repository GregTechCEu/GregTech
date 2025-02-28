package gregtech.api.recipes.ui;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.recipes.ui.RecipeMapUI.computeOverlayKey;

public class RecipeMapUIBuilder {

    // todo try to store this better
    private final Byte2ObjectMap<UITexture> slotOverlayTextures = new Byte2ObjectArrayMap<>();
    private @Nullable UITexture progressTexture;
    private @Nullable ProgressWidget.Direction progressDirection;
    private @Nullable UITexture specialTexture;
    private @NotNull Area specialTextureLocation = new Area();

    /**
     * @param progressBar the progress bar texture to use
     * @return this
     */
    public @NotNull RecipeMapUIBuilder progressBar(@Nullable UITexture progressBar) {
        this.progressTexture = progressBar;
        return this;
    }

    /**
     * @param progressBar the progress bar texture to use
     * @param moveType    the progress bar move type to use
     * @return this
     */
    public @NotNull RecipeMapUIBuilder progressBar(@Nullable UITexture progressBar,
                                                   @Nullable ProgressWidget.Direction moveType) {
        this.progressDirection = moveType;
        return progressBar(progressBar);
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
        this.slotOverlayTextures.put(computeOverlayKey(isOutput, false, isLastSlot), texture);
        return this;
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
        this.slotOverlayTextures.put(computeOverlayKey(isOutput, true, isLastSlot), texture);
        return this;
    }

    public @NotNull RecipeMapUIBuilder specialTexture(@NotNull UITexture texture, int x, int y, int width,
                                                      int height) {
        this.specialTexture = texture;
        this.specialTextureLocation.set(x, y, width, height);
        return this;
    }

    public void setMapUi(RecipeMapUI<?> mapUi) {
        mapUi.setUsesMui2();
        if (progressTexture != null) {
            mapUi.setProgressBarTexture(progressTexture);
        }
        if (progressDirection != null) {
            mapUi.setProgressBarDirection(progressDirection);
        }
        if (specialTexture != null) {
            mapUi.setSpecialTexture(specialTexture, specialTextureLocation);
        }
        for (var entry : slotOverlayTextures.byte2ObjectEntrySet()) {
            mapUi.setSlotOverlay(entry.getByteKey(), entry.getValue());
        }
    }
}
