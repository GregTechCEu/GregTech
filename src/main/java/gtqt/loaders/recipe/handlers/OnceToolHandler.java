package gtqt.loaders.recipe.handlers;

import gregtech.api.recipes.RecipeMaps;

import net.minecraft.init.Items;

import static gregtech.api.GTValues.IV;
import static gregtech.api.GTValues.VA;
import static gregtech.common.items.ToolItems.*;

public class OnceToolHandler {

    public static void register() {
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_WRENCH.get())
                .circuitMeta(1)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_HARD_HAMMER.get())
                .circuitMeta(2)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_WIRE_CUTTER.get())
                .circuitMeta(3)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_SAW.get())
                .circuitMeta(4)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_FILE.get())
                .circuitMeta(5)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_SCREWDRIVER.get())
                .circuitMeta(6)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .output(ONCE_MORTAR.get())
                .circuitMeta(7)
                .duration(1)
                .EUt(VA[IV])
                .buildAndRegister();
    }
}
