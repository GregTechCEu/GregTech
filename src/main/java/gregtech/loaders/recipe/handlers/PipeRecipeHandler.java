package gregtech.loaders.recipe.handlers;

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
import static gregtech.api.unification.ore.OrePrefix.plate;

public class PipeRecipeHandler {

    public static void register() {
        OrePrefix.pipeFluid.addProcessingHandler(PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipe);

        OrePrefix.pipeQuadrupleFluid.addProcessingHandler(PropertyKey.FLUID_PIPE,
                PipeRecipeHandler::processPipeQuadruple);
        OrePrefix.pipeNonupleFluid.addProcessingHandler(PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeNonuple);

        OrePrefix.pipeItem.addProcessingHandler(PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipe);

        OrePrefix.pipeRestrictive.addProcessingHandler(PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
    }

    private static void processRestrictivePipe(OrePrefix pipePrefix, Material material, ItemPipeProperties property) {
        OrePrefix unrestrictive;
        if (pipePrefix == OrePrefix.pipeRestrictive) unrestrictive = OrePrefix.pipeItem;
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

    private static void processPipe(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 3)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE)
                    .outputs(pipeStack)
                    .duration((int) material.getMass() * 3)
                    .EUt(6 * getVoltageMultiplier(material))
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material, 3)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE)
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

    private static void processPipeQuadruple(OrePrefix pipePrefix, Material material, FluidPipeProperties property) {
        ItemStack smallPipe = OreDictUnifier.get(OrePrefix.pipeFluid, material);
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
        ItemStack smallPipe = OreDictUnifier.get(OrePrefix.pipeFluid, material);
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
