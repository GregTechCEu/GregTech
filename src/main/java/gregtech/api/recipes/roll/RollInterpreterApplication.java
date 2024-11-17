package gregtech.api.recipes.roll;

import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

public enum RollInterpreterApplication {

    ITEM_OUTPUT("gregtech.recipe.interpreter_application.item_output"),
    ITEM_INPUT("gregtech.recipe.interpreter_application.item_input"),
    FLUID_OUTPUT("gregtech.recipe.interpreter_application.fluid_output"),
    FLUID_INPUT("gregtech.recipe.interpreter_application.fluid_input");

    private final @NotNull String translationKey;

    RollInterpreterApplication(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    public @NotNull String getTranslationKey() {
        return translationKey;
    }

    public @NotNull String getTranslated() {
        return I18n.format(translationKey);
    }

    boolean isOutput() {
        return this == ITEM_OUTPUT || this == FLUID_OUTPUT;
    }

    boolean isItem() {
        return this == ITEM_INPUT || this == ITEM_OUTPUT;
    }

    boolean isFluid() {
        return this == FLUID_INPUT || this == FLUID_OUTPUT;
    }

    public @NotNull String flowDirectionTranslated() {
        return I18n.format(isOutput() ? "gregtech.recipe.interpreter_application.output" :
                "gregtech.recipe.interpreter_application.input");
    }
}
