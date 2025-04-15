package gregtech.loaders.recipe.handlers;


import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;

import static gregtech.api.GTValues.*;
import static gregtech.api.GTValues.ULV;
import static gregtech.common.items.MetaItems.*;

public class PartRecipeHandler1 {
    public static void register() {
        OrePrefix.bolt.addProcessingHandler(PropertyKey.DUST, PartRecipeHandler1::processBolt);
        OrePrefix.toolHeadDrill.addProcessingHandler(PropertyKey.INGOT, PartRecipeHandler1::processDrillHead);
        OrePrefix.ring.addProcessingHandler(PropertyKey.DUST, PartRecipeHandler1::processRing);
        OrePrefix.round.addProcessingHandler(PropertyKey.DUST, PartRecipeHandler1::processRound);
        OrePrefix.screw.addProcessingHandler(PropertyKey.DUST, PartRecipeHandler1::processScrew);
        OrePrefix.stick.addProcessingHandler(PropertyKey.DUST, PartRecipeHandler1::processStick);
        OrePrefix.stickLong.addProcessingHandler(PropertyKey.DUST, PartRecipeHandler1::processStickLong);
        OrePrefix.turbineBlade.addProcessingHandler(PropertyKey.INGOT, PartRecipeHandler1::processTurbineBlade);
    }

    private static void processBolt(OrePrefix prefix, Material material, DustProperty property) {
        // Because all gems do not have molten liquid, so we ignored flag predicate.
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_BOLT)
                    .fluidInputs(material.getFluid(L / 8))
                    .output(OrePrefix.bolt, material)
                    .EUt(VA[LV])
                    .duration(2 * SECOND + 5 * TICK)
                    .buildAndRegister();
        }
    }

    private static void processDrillHead(OrePrefix prefix, Material material, IngotProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_DRILL_HEAD)
                    .fluidInputs(material.getFluid(L * 4))
                    .output(OrePrefix.toolHeadDrill, material)
                    .EUt(VA[MV])
                    .duration(5 * SECOND)
                    .buildAndRegister();
        }
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .notConsumable(SHAPE_EXTRUDER_DRILL_HEAD)
                .input(OrePrefix.ingot, material, 4)
                .output(OrePrefix.toolHeadDrill, material, 1)
                .EUt(VA[MV])
                .duration(5 * SECOND)
                .buildAndRegister();
    }

    private static void processRing(OrePrefix prefix, Material material, DustProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_RING)
                    .fluidInputs(material.getFluid(L / 4))
                    .output(OrePrefix.ring, material)
                    .EUt(VA[LV])
                    .duration(5 * SECOND)
                    .buildAndRegister();
        }
    }

    private static void processRound(OrePrefix prefix, Material material, DustProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_ROUND)
                    .fluidInputs(material.getFluid(L / 8))
                    .output(OrePrefix.round, material)
                    .EUt(VA[LV])
                    .duration(2 * SECOND + 5 * TICK)
                    .buildAndRegister();
        }
    }

    private static void processScrew(OrePrefix prefix, Material material, DustProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_SCREW)
                    .fluidInputs(material.getFluid(L / 8))
                    .output(OrePrefix.screw, material)
                    .EUt(Math.max(VA[MV], 4 * getVoltageMultiplier(material)))
                    .duration(2 * SECOND + 5 * TICK)
                    .buildAndRegister();
        }
    }

    private static void processStick(OrePrefix prefix, Material material, DustProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_ROD)
                    .fluidInputs(material.getFluid(L / 2))
                    .output(OrePrefix.stick, material)
                    .EUt(VA[LV])
                    .duration(7 * SECOND + 5 * TICK)
                    .buildAndRegister();
        }
    }

    private static void processStickLong(OrePrefix prefix, Material material, DustProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_ROD_LONG)
                    .fluidInputs(material.getFluid(L))
                    .output(OrePrefix.stickLong, material)
                    .EUt(Math.max(VA[MV], 16 * getVoltageMultiplier(material)))
                    .duration(15 * SECOND)
                    .buildAndRegister();
        }
    }

    private static void processTurbineBlade(OrePrefix prefix, Material material, IngotProperty property) {
        if (material.hasFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(SHAPE_MOLD_TURBINE_BLADE)
                    .fluidInputs(material.getFluid(L * 6))
                    .output(OrePrefix.turbineBlade, material)
                    .EUt(Math.max(VA[MV], 6 * getVoltageMultiplier(material)))
                    .duration(20 * SECOND)
                    .buildAndRegister();
        }

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .notConsumable(SHAPE_EXTRUDER_TURBINE_BLADE)
                .input(OrePrefix.ingot, material, 4)
                .output(OrePrefix.turbineBlade, material)
                .EUt(VA[MV])
                .duration(20 * SECOND)
                .buildAndRegister();
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }
}
