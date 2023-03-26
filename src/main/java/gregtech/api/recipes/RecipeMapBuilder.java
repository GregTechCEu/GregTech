package gregtech.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

public final class RecipeMapBuilder<R extends RecipeBuilder<R>> {

    private final String unlocalizedName;
    private final RecipeBuilder<R> defaultRecipeBuilder;
    private RecipeMapBackend.BackendCreator<R> backendCreator = RecipeMapBackend::new;
    private RecipeMapFrontend.FrontendCreator frontendCreator = RecipeMapFrontend::new;

    private final Collection<Consumer<RecipeBuilder<R>>> buildActions = new LinkedList<>();
    private final Byte2ObjectMap<TextureArea> slotOverlays = new Byte2ObjectOpenHashMap<>();

    private int maxItemInputs;
    private boolean modifyItemInputs = true;
    private int maxItemOutputs;
    private boolean modifyItemOutputs = true;
    private int maxFluidInputs;
    private boolean modifyFluidInputs = true;
    private int maxFluidOutputs;
    private boolean modifyFluidOutputs = true;
    private boolean isVisible = true;
    private RecipeMap<?> smallRecipeMap;

    private TextureArea progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
    private ProgressWidget.MoveType progressBarMovetype = ProgressWidget.MoveType.HORIZONTAL;
    private TextureArea specialTexture;
    private int[] specialTexturePosition;
    private SoundEvent sound;

    public RecipeMapBuilder(@Nonnull String unlocalizedName, @Nonnull RecipeBuilder<R> defaultRecipeBuilder) {
        this.unlocalizedName = unlocalizedName;
        this.defaultRecipeBuilder = defaultRecipeBuilder;
    }

    /**
     * @param maxItemInputs the max amount of item inputs
     */
    @Nonnull
    public RecipeMapBuilder<R> itemInputs(@Nonnegative int maxItemInputs) {
        this.maxItemInputs = maxItemInputs;
        return this;
    }

    /**
     * @param maxItemInputs the max amount of item inputs
     * @param modifyItemInputs if max item inputs can be modified after RecipeMap creation
     */
    @Nonnull
    public RecipeMapBuilder<R> itemInputs(@Nonnegative int maxItemInputs, boolean modifyItemInputs) {
        this.maxItemInputs = maxItemInputs;
        this.modifyItemInputs = modifyItemInputs;
        return this;
    }

    /**
     * @param maxItemOutputs the max amount of item outputs
     */
    @Nonnull
    public RecipeMapBuilder<R> itemOutputs(@Nonnegative int maxItemOutputs) {
        this.maxItemOutputs = maxItemOutputs;
        return this;
    }

    /**
     * @param maxItemOutputs the max amount of item outputs
     * @param modifyItemOutputs if max item outputs can be modified after RecipeMap creation
     */
    @Nonnull
    public RecipeMapBuilder<R> itemOutputs(@Nonnegative int maxItemOutputs, boolean modifyItemOutputs) {
        this.maxItemOutputs = maxItemOutputs;
        this.modifyItemOutputs = modifyItemOutputs;
        return this;
    }

    /**
     * @param maxFluidInputs the max amount of fluid inputs
     */
    @Nonnull
    public RecipeMapBuilder<R> fluidInputs(@Nonnegative int maxFluidInputs) {
        this.maxFluidInputs = maxFluidInputs;
        return this;
    }

    /**
     * @param maxFluidInputs the max amount of fluid inputs
     * @param modifyFluidInputs if max fluid inputs can be modified after RecipeMap creation
     */
    @Nonnull
    public RecipeMapBuilder<R> fluidInputs(@Nonnegative int maxFluidInputs, boolean modifyFluidInputs) {
        this.maxFluidInputs = maxFluidInputs;
        this.modifyFluidInputs = modifyFluidInputs;
        return this;
    }

    /**
     * @param maxFluidOutputs the max amount of fluid outputs
     */
    @Nonnull
    public RecipeMapBuilder<R> fluidOutputs(@Nonnegative int maxFluidOutputs) {
        this.maxFluidOutputs = maxFluidOutputs;
        return this;
    }

    /**
     * @param maxFluidOutputs the max amount of fluid outputs
     * @param modifyFluidOutputs if max fluid outputs can be modified after RecipeMap creation
     */
    @Nonnull
    public RecipeMapBuilder<R> fluidOutputs(@Nonnegative int maxFluidOutputs, boolean modifyFluidOutputs) {
        this.maxFluidOutputs = maxFluidOutputs;
        this.modifyFluidOutputs = modifyFluidOutputs;
        return this;
    }

    /**
     * @param buildAction a build action to be performed upon recipe build
     */
    @Nonnull
    public RecipeMapBuilder<R> onBuild(Consumer<RecipeBuilder<R>> buildAction) {
        this.buildActions.add(buildAction);
        return this;
    }

    /**
     * @param buildActions build actions to be performed upon recipe build
     */
    @Nonnull
    public RecipeMapBuilder<R> onBuild(Collection<Consumer<RecipeBuilder<R>>> buildActions) {
        this.buildActions.addAll(buildActions);
        return this;
    }

    /**
     * @param smallRecipeMap the small version of this recipemap
     */
    @Nonnull
    public RecipeMapBuilder<R> smallRecipeMap(@Nullable RecipeMap<?> smallRecipeMap) {
        this.smallRecipeMap = smallRecipeMap;
        return this;
    }

    /**
     * Set the recipemap as hidden
     */
    @Nonnull
    public RecipeMapBuilder<R> hidden() {
        return visibility(false);
    }

    /**
     * @param isVisible if the recipemap should be visible
     */
    @Nonnull
    public RecipeMapBuilder<R> visibility(boolean isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    /**
     * @param progressBarTexture texture for the progress bar
     */
    @Nonnull
    public RecipeMapBuilder<R> progressBar(@Nonnull TextureArea progressBarTexture) {
        this.progressBarTexture = progressBarTexture;
        return this;
    }

    /**
     * @param progressBarTexture texture for the progress bar
     * @param moveType the type of movement for the progress bar
     */
    @Nonnull
    public RecipeMapBuilder<R> progressBar(@Nonnull TextureArea progressBarTexture, @Nonnull ProgressWidget.MoveType moveType) {
        this.progressBarTexture = progressBarTexture;
        this.progressBarMovetype = moveType;
        return this;
    }

    /**
     * @param specialTexture the special texture to add
     * @param position the position of the texture. index=0 is x, index=1 is y, index=2 is width, index=3 is height
     */
    @Nonnull
    public RecipeMapBuilder<R> specialTexture(@Nullable TextureArea specialTexture, @Nullable int... position) {
        this.specialTexture = specialTexture;
        if (position != null && position.length != 4) {
            throw new IllegalArgumentException("Special Texture position must have 4 arguments");
        }
        this.specialTexturePosition = position;
        return this;
    }

    /**
     * @param isOutput if the slot is an output slot
     * @param isFluid if the slot is a fluid slot
     * @param slotOverlay the overlay for the slot
     */
    @Nonnull
    public RecipeMapBuilder<R> slotOverlay(boolean isOutput, boolean isFluid, @Nonnull TextureArea slotOverlay) {
        this.slotOverlay(isOutput, isFluid, false, slotOverlay);
        return this.slotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    /**
     * @param isOutput if the slot is an output slot
     * @param isFluid if the slot is a fluid slot
     * @param isLast if the slot is the last slot
     * @param slotOverlay the overlay for the slot
     */
    @Nonnull
    public RecipeMapBuilder<R> slotOverlay(boolean isOutput, boolean isFluid, boolean isLast, @Nonnull TextureArea slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    /**
     * @param sound the sound to use for the recipemap
     */
    @Nonnull
    public RecipeMapBuilder<R> sound(@Nullable SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    /**
     * @param backendCreator the function creating a recipemap backend.
     */
    @Nonnull
    public RecipeMapBuilder<R> backend(@Nonnull RecipeMapBackend.BackendCreator<R> backendCreator) {
        this.backendCreator = backendCreator;
        return this;
    }

    /**
     * @param frontEndCreator a function creating a recipemap frontend.
     */
    @Nonnull
    public RecipeMapBuilder<R> frontend(@Nonnull RecipeMapFrontend.FrontendCreator frontEndCreator) {
        this.frontendCreator = frontEndCreator;
        return this;
    }

    @Nonnull
    public RecipeMap<R> build() {
        final RecipeMapFrontend frontend = this.frontendCreator.apply(unlocalizedName, slotOverlays, progressBarTexture,
                progressBarMovetype, specialTexture, specialTexturePosition, sound, isVisible);

        final RecipeMapBackend<R> backend = this.backendCreator.apply(unlocalizedName, defaultRecipeBuilder);

        final RecipeMap<R> recipeMap = new RecipeMap<>(
                unlocalizedName, defaultRecipeBuilder,
                maxItemInputs, modifyItemInputs,
                maxItemOutputs, modifyItemOutputs,
                maxFluidInputs, modifyFluidInputs,
                maxFluidOutputs, modifyFluidOutputs,
                buildActions, frontend, backend
        );
        recipeMap.setSmallRecipeMap(this.smallRecipeMap);
        return recipeMap;
    }
}
