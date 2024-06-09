package gtrmcore.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenExpansion("mods.gregtech.recipe.RecipeMaps")
@ZenRegister
public class GTRMRecipeMaps {

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> PRIMITIVE_MIXER_RECIPES = new RecipeMap<>(
            "primitive_mixer", 6, 2, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.MIXER);

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> PRIMITIVE_ASSEMBLER_RECIPES = new RecipeMap<>(
            "primitive_assembler", 9, 2, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);

    @ZenProperty
    public static final RecipeMap<SimpleRecipeBuilder> PRIMITIVE_CIRCUIT_ASSEMBLER_RECIPES = new RecipeMap<>(
            "primitive_circuit_assembler", 6, 2, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT_ASSEMBLER, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);
}
