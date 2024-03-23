package gregtech.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;

import com.google.common.base.CaseFormat;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.Glue;
import static gregtech.api.unification.material.info.MaterialFlags.NO_SMASHING;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.unification.ore.OrePrefix.plate;
import static gregtech.api.unification.ore.OrePrefix.plateDouble;
import static gregtech.api.util.GTUtility.gregtechId;

public class PipeRecipeHandler {

    public static void register() {
        GregTechAPI.oreProcessorManager.registerProcessor(pipeTinyFluid, gregtechId("process_pipe_tiny"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeTiny);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeSmallFluid, gregtechId("process_pipe_small"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeSmall);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeNormalFluid, gregtechId("process_pipe_normal"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeNormal);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeLargeFluid, gregtechId("process_pipe_large"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeLarge);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeHugeFluid, gregtechId("process_pipe_huge"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeHuge);

        GregTechAPI.oreProcessorManager.registerProcessor(pipeQuadrupleFluid, gregtechId("process_pipe_quadruple"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeQuadruple);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeNonupleFluid, gregtechId("process_pipe_nonuple"),
                PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeNonuple);

        GregTechAPI.oreProcessorManager.registerProcessor(pipeTinyItem, gregtechId("process_pipe_tiny"),
                PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeTiny);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeSmallItem, gregtechId("process_pipe_small"),
                PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeSmall);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeNormalItem, gregtechId("process_pipe_normal"),
                PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeNormal);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeLargeItem, gregtechId("process_pipe_large"),
                PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeLarge);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeHugeItem, gregtechId("process_pipe_huge"),
                PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeHuge);

        GregTechAPI.oreProcessorManager.registerProcessor(pipeSmallRestrictive,
                gregtechId("process_pipe_small_restrictive"), PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeNormalRestrictive,
                gregtechId("process_pipe_normal_restrictive"), PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeLargeRestrictive,
                gregtechId("process_pipe_large_restrictive"), PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
        GregTechAPI.oreProcessorManager.registerProcessor(pipeHugeRestrictive,
                gregtechId("process_pipe_huge_restrictive"), PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
    }

    private static void processRestrictivePipe(OrePrefix pipePrefix, Material material, ItemPipeProperties property) {
        OrePrefix unrestrictive;
        if (pipePrefix == OrePrefix.pipeSmallRestrictive) unrestrictive = OrePrefix.pipeSmallItem;
        else if (pipePrefix == OrePrefix.pipeNormalRestrictive) unrestrictive = OrePrefix.pipeNormalItem;
        else if (pipePrefix == OrePrefix.pipeLargeRestrictive) unrestrictive = OrePrefix.pipeLargeItem;
        else if (pipePrefix == OrePrefix.pipeHugeRestrictive) unrestrictive = OrePrefix.pipeHugeItem;
        else return;

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(unrestrictive, material)
                .input(OrePrefix.ring, Materials.Iron, 2)
                .output(pipePrefix, material)
                .duration(20)
                .EUt(VA[ULV])
                .buildAndRegister();

        ModHandler.addShapedRecipe(
                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, pipePrefix.toString()) + "_" +
                        material.toCamelCaseString(),
                OreDictUnifier.get(pipePrefix, material), "PR", "Rh",
                'P', new UnificationEntry(unrestrictive, material), 'R',
                OreDictUnifier.get(OrePrefix.ring, Materials.Iron));
    }

    private static void processPipeTiny(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);

        // Some pipes like wood do not have an ingot
        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_TINY)
                    .outputs(GTUtility.copy(2, pipeStack))
                    .duration((int) (material.getMass()))
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_TINY)
                    .outputs(GTUtility.copy(2, pipeStack))
                    .duration((int) (material.getMass()))
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        } else {
            if (ModHandler.isMaterialWood(material)) {
                ModHandler.addShapedRecipe(String.format("tiny_%s_pipe", material),
                        GTUtility.copy(2, pipeStack), " s ", "rXw",
                        'X', new UnificationEntry(OrePrefix.plank, material));

                ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(plate, material)
                        .circuitMeta(18)
                        .fluidInputs(Glue.getFluid(10))
                        .output(pipePrefix, material, 2)
                        .buildAndRegister();
            } else {
                ModHandler.addShapedRecipe(String.format("tiny_%s_pipe", material),
                        GTUtility.copy(2, pipeStack), " s ", "hXw",
                        'X', new UnificationEntry(OrePrefix.plate, material));
            }
        }
    }

    private static void processPipeSmall(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_SMALL)
                    .outputs(pipeStack)
                    .duration((int) (material.getMass()))
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_SMALL)
                    .outputs(pipeStack)
                    .duration((int) (material.getMass()))
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        } else {
            if (ModHandler.isMaterialWood(material)) {
                ModHandler.addShapedRecipe(String.format("small_%s_pipe", material),
                        pipeStack, "sXr",
                        'X', new UnificationEntry(OrePrefix.plank, material));

                ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(plate, material)
                        .circuitMeta(12)
                        .fluidInputs(Glue.getFluid(10))
                        .output(pipePrefix, material)
                        .buildAndRegister();

            } else {
                ModHandler.addShapedRecipe(String.format("small_%s_pipe", material),
                        pipeStack, "wXh",
                        'X', new UnificationEntry(OrePrefix.plate, material));
            }
        }
    }

    private static void processPipeNormal(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 3)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_NORMAL)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 3)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material, 3)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_NORMAL)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 3)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        } else {
            if (ModHandler.isMaterialWood(material)) {
                ModHandler.addShapedRecipe(String.format("medium_%s_pipe", material),
                        pipeStack, "XXX", "s r",
                        'X', new UnificationEntry(OrePrefix.plank, material));

                ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LV])
                        .input(plate, material, 3)
                        .circuitMeta(6)
                        .fluidInputs(Glue.getFluid(20))
                        .output(pipePrefix, material)
                        .buildAndRegister();

            } else {
                ModHandler.addShapedRecipe(String.format("medium_%s_pipe", material),
                        pipeStack, "XXX", "w h",
                        'X', new UnificationEntry(OrePrefix.plate, material));
            }
        }
    }

    private static void processPipeLarge(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 6)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_LARGE)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 6)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material, 6)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_LARGE)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 6)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        } else {
            if (ModHandler.isMaterialWood(material)) {
                ModHandler.addShapedRecipe(String.format("large_%s_pipe", material),
                        pipeStack, "XXX", "s r", "XXX",
                        'X', new UnificationEntry(OrePrefix.plank, material));

                ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                        .input(plate, material, 6)
                        .circuitMeta(2)
                        .fluidInputs(Glue.getFluid(50))
                        .output(pipePrefix, material)
                        .buildAndRegister();
            } else {
                ModHandler.addShapedRecipe(String.format("large_%s_pipe", material),
                        pipeStack, "XXX", "w h", "XXX",
                        'X', new UnificationEntry(OrePrefix.plate, material));
            }
        }
    }

    private static void processPipeHuge(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 12)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_HUGE)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 24)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material, 12)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_HUGE)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 24)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        } else if (OrePrefix.plateDouble.doGenerateItem(material)) {
            if (ModHandler.isMaterialWood(material)) {
                ModHandler.addShapedRecipe(String.format("huge_%s_pipe", material),
                        pipeStack, "XXX", "s r", "XXX",
                        'X', new UnificationEntry(OrePrefix.plateDouble, material));

                ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                        .input(plateDouble, material, 6)
                        .circuitMeta(24)
                        .fluidInputs(Glue.getFluid(100))
                        .output(pipePrefix, material)
                        .buildAndRegister();
            } else {
                ModHandler.addShapedRecipe(String.format("huge_%s_pipe", material),
                        pipeStack, "XXX", "w h", "XXX",
                        'X', new UnificationEntry(OrePrefix.plateDouble, material));
            }
        }
    }

    private static void processPipeQuadruple(OrePrefix pipePrefix, Material material, FluidPipeProperties property) {
        ItemStack smallPipe = OreDictUnifier.get(OrePrefix.pipeSmallFluid, material);
        ItemStack quadPipe = OreDictUnifier.get(pipePrefix, material);
        ModHandler.addShapedRecipe(String.format("quadruple_%s_pipe", material.toString()),
                quadPipe, "XX", "XX",
                'X', smallPipe);

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, smallPipe))
                .circuitMeta(4)
                .outputs(quadPipe)
                .duration(30)
                .EUt(VA[ULV])
                .buildAndRegister();
    }

    private static void processPipeNonuple(OrePrefix pipePrefix, Material material, FluidPipeProperties property) {
        ItemStack smallPipe = OreDictUnifier.get(OrePrefix.pipeSmallFluid, material);
        ItemStack nonuplePipe = OreDictUnifier.get(pipePrefix, material);
        ModHandler.addShapedRecipe(String.format("nonuple_%s_pipe", material.toString()),
                nonuplePipe, "XXX", "XXX", "XXX",
                'X', smallPipe);

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(9, smallPipe))
                .circuitMeta(9)
                .outputs(nonuplePipe)
                .duration(40)
                .EUt(VA[ULV])
                .buildAndRegister();
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
