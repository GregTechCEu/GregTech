package gregtech.loaders.oreprocessing;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

public class PipeRecipeHandler {

    public static void register() {
        OrePrefix.pipeTiny.addProcessingHandler(IngotMaterial.class, PipeRecipeHandler::processPipeTiny);
        OrePrefix.pipeSmall.addProcessingHandler(IngotMaterial.class, PipeRecipeHandler::processPipeSmall);
        OrePrefix.pipeMedium.addProcessingHandler(IngotMaterial.class, PipeRecipeHandler::processPipeNormal);
        OrePrefix.pipeLarge.addProcessingHandler(IngotMaterial.class, PipeRecipeHandler::processPipeLarge);
        OrePrefix.pipeHuge.addProcessingHandler(IngotMaterial.class, PipeRecipeHandler::processPipeHuge);
    }

    private static void processPipeTiny(OrePrefix pipePrefix, IngotMaterial material) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material, 1)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_TINY)
            .outputs(GTUtility.copyAmount(2, pipeStack))
            .duration((int) (material.getAverageMass()))
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("tiny_%s_pipe", material.toString()),
                GTUtility.copyAmount(8, pipeStack), "XXX", "h w", "XXX",
                'X', new UnificationEntry(OrePrefix.plate, material));
    }

    private static void processPipeSmall(OrePrefix pipePrefix, IngotMaterial material) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material, 1)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_SMALL)
            .outputs(pipeStack)
            .duration((int) (material.getAverageMass()))
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("small_%s_pipe", material.toString()),
            GTUtility.copyAmount(6, pipeStack), "XwX", "X X", "XhX",
            'X', new UnificationEntry(OrePrefix.plate, material));
    }

    private static void processPipeNormal(OrePrefix pipePrefix, IngotMaterial material) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material, 3)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_MEDIUM)
            .outputs(pipeStack)
            .duration((int) material.getAverageMass() * 3)
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("medium_%s_pipe", material.toString()),
            GTUtility.copyAmount(2, pipeStack), "XXX", "w h", "XXX",
            'X', new UnificationEntry(OrePrefix.plate, material));
    }

    private static void processPipeLarge(OrePrefix pipePrefix, IngotMaterial material) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material, 6)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_LARGE)
            .outputs(pipeStack)
            .duration((int) material.getAverageMass() * 6)
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("large_%s_pipe", material.toString()),
                pipeStack, "XhX", "X X", "XwX",
                'X', new UnificationEntry(OrePrefix.plate, material));
    }

    private static void processPipeHuge(OrePrefix pipePrefix, IngotMaterial material) {
        ItemStack pipeStack = OreDictUnifier.get(pipePrefix, material);
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material, 12)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_PIPE_HUGE)
                .outputs(pipeStack)
                .duration((int) material.getAverageMass() * 24)
                .EUt(6 * getVoltageMultiplier(material))
                .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("huge_%s_pipe", material.toString()),
                pipeStack, "XhX", "X X", "XwX",
                'X', new UnificationEntry(OrePrefix.plateDouble, material));
    }


    private static int getVoltageMultiplier(Material material) {
        return material instanceof IngotMaterial && ((IngotMaterial) material)
            .blastFurnaceTemperature >= 2800 ? 32 : 8;
    }
}
