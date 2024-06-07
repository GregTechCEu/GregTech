package gregtech.loaders.recipe.handlers;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FissionFuelProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class NuclearRecipeHandler {

    public static void register() {
        OrePrefix.fuelRod.addProcessingHandler(PropertyKey.FISSION_FUEL, NuclearRecipeHandler::processFuelRod);
    }

    private static void processFuelRod(OrePrefix orePrefix, Material material, FissionFuelProperty oreProperty) {
        SPENT_FUEL_POOL_RECIPES.recipeBuilder().duration(10000).EUt(20) // This is fine, since it goes up to 320x
                                                                        // parallel
                .input(fuelRod, material)
                .output(fuelRodDepleted, material)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                .input(fuelRodDepleted, material)
                .output(dust, Zircaloy, 4)
                .buildAndRegister();

        SPENT_FUEL_POOL_RECIPES.recipeBuilder().duration(10000).EUt(20)
                .input(fuelRodHotDepleted, material)
                .output(fuelRodDepleted, material)
                .buildAndRegister();

        CUTTER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(fuelRodDepleted, material)
                .output(plate, Zircaloy, 4)
                .output(fuelPelletDepleted, material, 16)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                .input(dust, material, 1)
                .notConsumable(MetaItems.SHAPE_MOLD_CYLINDER)
                .output(fuelPellet, material)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(800).EUt(VA[EV])
                .input(plate, Zircaloy, 4)
                .input(spring, Inconel, 1)
                .input(round, StainlessSteel, 2)
                .input(fuelPellet, material, 16)
                .output(fuelRod, material)
                .buildAndRegister();
    }
}
