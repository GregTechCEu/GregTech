package gregtech.api.recipes;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.ui.RecipeMapUI;
import gregtech.api.recipes.ui.RecipeMapUIBuilder;
import gregtech.api.recipes.ui.RecipeMapUIFunction;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import com.cleanroommc.modularui.drawable.UITexture;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static gregtech.api.recipes.ui.RecipeMapUI.computeOverlayKey;

public class RecipeMapBuilder<B extends RecipeBuilder<B>> {

    private final String unlocalizedName;
    private final B defaultRecipeBuilder;

    private int itemInputs;
    private boolean modifyItemInputs = true;
    private int itemOutputs;
    private boolean modifyItemOutputs = true;
    private int fluidInputs;
    private boolean modifyFluidInputs = true;
    private int fluidOutputs;
    private boolean modifyFluidOutputs = true;

    private boolean isGenerator;

    private RecipeMapUIFunction recipeMapUIFunction = this::buildUI;

    private SoundEvent sound;
    private boolean allowEmptyOutputs;

    private @Nullable Map<ResourceLocation, RecipeBuildAction<B>> buildActions;

    private boolean sortToBack;

    /* *********************** MUI 1 *********************** */

    @Deprecated
    private final Byte2ObjectMap<TextureArea> slotOverlays = new Byte2ObjectArrayMap<>();
    @Deprecated
    private @Nullable TextureArea progressBar;
    @Deprecated
    private @Nullable gregtech.api.gui.widgets.ProgressWidget.MoveType moveType;
    @Deprecated
    private @Nullable TextureArea specialTexture;
    @Deprecated
    private int @Nullable [] specialTextureLocation;

    /* *********************** MUI 2 *********************** */

    @ApiStatus.Experimental
    private boolean usesMui2 = false;

    private @Nullable Consumer<RecipeMapUIBuilder> mapUIBuilder;

    /**
     * @param unlocalizedName      the name of the recipemap
     * @param defaultRecipeBuilder the default recipe builder of the recipemap
     */
    public RecipeMapBuilder(@NotNull String unlocalizedName, @NotNull B defaultRecipeBuilder) {
        this.unlocalizedName = unlocalizedName;
        this.defaultRecipeBuilder = defaultRecipeBuilder;
    }

    /**
     * @param itemInputs the amount of item inputs
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> itemInputs(int itemInputs) {
        this.itemInputs = itemInputs;
        return this;
    }

    /**
     * @param modifyItemInputs if item input limit modification should be allowed
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> modifyItemInputs(boolean modifyItemInputs) {
        this.modifyItemInputs = modifyItemInputs;
        return this;
    }

    /**
     * @param itemOutputs the amount of item outputs
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> itemOutputs(int itemOutputs) {
        this.itemOutputs = itemOutputs;
        return this;
    }

    /**
     * @param modifyItemOutputs if item output limit modification should be allowed
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> modifyItemOutputs(boolean modifyItemOutputs) {
        this.modifyItemOutputs = modifyItemOutputs;
        return this;
    }

    /**
     * @param fluidInputs the amount of fluid inputs
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> fluidInputs(int fluidInputs) {
        this.fluidInputs = fluidInputs;
        return this;
    }

    /**
     * @param modifyFluidInputs if fluid input limit modification should be allowed
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> modifyFluidInputs(boolean modifyFluidInputs) {
        this.modifyFluidInputs = modifyFluidInputs;
        return this;
    }

    /**
     * @param fluidOutputs the amount of fluid outputs
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> fluidOutputs(int fluidOutputs) {
        this.fluidOutputs = fluidOutputs;
        return this;
    }

    /**
     * @param modifyFluidOutputs if fluid output limit modification should be allowed
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> modifyFluidOutputs(boolean modifyFluidOutputs) {
        this.modifyFluidOutputs = modifyFluidOutputs;
        return this;
    }

    /**
     * Mark this recipemap is generating energy
     *
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> generator() {
        this.isGenerator = true;
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     * @param progressBar the progress bar texture to use
     * @return this
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> progressBar(@Nullable TextureArea progressBar) {
        this.progressBar = progressBar;
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     * @param progressBar the progress bar texture to use
     * @param moveType    the progress bar move type to use
     * @return this
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> progressBar(@Nullable TextureArea progressBar,
                                                    @Nullable gregtech.api.gui.widgets.ProgressWidget.MoveType moveType) {
        this.progressBar = progressBar;
        this.moveType = moveType;
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> itemSlotOverlay(@NotNull TextureArea texture, boolean isOutput) {
        this.slotOverlays.put(computeOverlayKey(isOutput, false, false), texture);
        this.slotOverlays.put(computeOverlayKey(isOutput, false, true), texture);
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     * @param texture    the texture to use
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     * @return this
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> itemSlotOverlay(@NotNull TextureArea texture, boolean isOutput,
                                                        boolean isLastSlot) {
        this.slotOverlays.put(computeOverlayKey(isOutput, false, isLastSlot), texture);
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     * @param texture  the texture to use
     * @param isOutput if the slot is an output slot
     * @return this
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> fluidSlotOverlay(@NotNull TextureArea texture, boolean isOutput) {
        this.slotOverlays.put(computeOverlayKey(isOutput, true, false), texture);
        this.slotOverlays.put(computeOverlayKey(isOutput, true, true), texture);
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     * @param texture    the texture to use
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     * @return this
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> fluidSlotOverlay(@NotNull TextureArea texture, boolean isOutput,
                                                         boolean isLastSlot) {
        this.slotOverlays.put(computeOverlayKey(isOutput, true, isLastSlot), texture);
        return this;
    }

    /**
     * @deprecated in favor of the MUI2 method.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public @NotNull RecipeMapBuilder<B> specialTexture(@NotNull TextureArea texture, int x, int y, int width,
                                                       int height) {
        this.specialTexture = texture;
        this.specialTextureLocation = new int[] { x, y, width, height };
        return this;
    }

    /**
     * @apiNote Only needed if you do not set textures using MUI2 methods, i.e. the ones that accept{@link UITexture}.
     *          <br>
     *          Marked experimental since this method will disappear once MUI2 is fully supported by all GTCEu UIs.
     */
    @ApiStatus.Experimental
    public @NotNull RecipeMapBuilder<B> usesMui2() {
        this.usesMui2 = true;
        return this;
    }

    /**
     * @param recipeMapUIFunction the custom function for creating the RecipeMap's ui
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> ui(@NotNull RecipeMapUIFunction recipeMapUIFunction) {
        this.recipeMapUIFunction = recipeMapUIFunction;
        return this;
    }

    public @NotNull RecipeMapBuilder<B> uiBuilder(@NotNull Consumer<RecipeMapUIBuilder> mapUIBuilder) {
        this.usesMui2 = true;
        this.mapUIBuilder = Objects.requireNonNull(mapUIBuilder, "ui builder is null");
        return this;
    }

    /**
     * @param recipeMap the recipemap associated with the ui
     * @return the RecipeMap's ui
     */
    private @NotNull RecipeMapUI<?> buildUI(@NotNull RecipeMap<?> recipeMap) {
        RecipeMapUI<?> ui = new RecipeMapUI<>(recipeMap, modifyItemInputs, modifyItemOutputs, modifyFluidInputs,
                modifyFluidOutputs, isGenerator);
        if (usesMui2 && this.mapUIBuilder != null) {
            ui.buildMui2(this.mapUIBuilder);
        } else {
            if (progressBar != null) {
                ui.setProgressBarTexture(progressBar);
            }
            if (moveType != null) {
                ui.setProgressBarMoveType(moveType);
            }
            if (specialTexture != null && specialTextureLocation != null) {
                ui.setSpecialTexture(specialTexture, specialTextureLocation);
            }
            for (var entry : slotOverlays.byte2ObjectEntrySet()) {
                ui.setSlotOverlay(entry.getByteKey(), entry.getValue());
            }
        }

        return ui;
    }

    /**
     * @param sound the sound to use
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> sound(@NotNull SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Make the recipemap accept recipes without any outputs
     *
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> allowEmptyOutputs() {
        this.allowEmptyOutputs = true;
        return this;
    }

    /**
     * Add a recipe build action to be performed upon this RecipeMap's builder's recipe registration.
     *
     * @param name   the unique name of the action
     * @param action the action to perform
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> onBuild(@NotNull ResourceLocation name, @NotNull RecipeBuildAction<B> action) {
        if (buildActions == null) {
            buildActions = new Object2ObjectOpenHashMap<>();
        } else if (buildActions.containsKey(name)) {
            throw new IllegalArgumentException("Cannot register RecipeBuildAction with duplicate name: " + name);
        }
        buildActions.put(name, action);
        return this;
    }

    /**
     * Have the primary {@link gregtech.api.recipes.category.GTRecipeCategory} for the RecipeMap be sorted to the end
     * of the JEI recipe category list.
     *
     * @param sortToBack if it should be sorted to the back
     * @return this
     */
    public @NotNull RecipeMapBuilder<B> jeiSortToBack(boolean sortToBack) {
        this.sortToBack = sortToBack;
        return this;
    }

    /**
     * <strong>Do not call this twice. RecipeMapBuilders are not re-usable.</strong>
     *
     * @return a new RecipeMap
     */
    public @NotNull RecipeMap<B> build() {
        RecipeMap<B> recipeMap = new RecipeMap<>(unlocalizedName, defaultRecipeBuilder, this.recipeMapUIFunction,
                itemInputs, itemOutputs, fluidInputs, fluidOutputs);
        recipeMap.setSound(sound);
        if (allowEmptyOutputs) {
            recipeMap.allowEmptyOutput();
        }
        if (buildActions != null) {
            recipeMap.onRecipeBuild(buildActions);
        }
        recipeMap.getPrimaryRecipeCategory().jeiSortToBack(sortToBack);
        return recipeMap;
    }
}
