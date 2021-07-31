package gregtech.loaders.oreprocessing;

import com.google.common.base.CaseFormat;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.ItemPipeProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

import static gregtech.api.unification.material.properties.DummyProperties.fluidPipeProperty;
import static gregtech.api.unification.material.properties.DummyProperties.itemPipeProperty;

public class PipeRecipeHandler {

    public static void register() {
        OrePrefix.pipeTinyFluid.addProcessingHandler(fluidPipeProperty, PipeRecipeHandler::processPipeTiny);
        OrePrefix.pipeSmallFluid.addProcessingHandler(fluidPipeProperty, PipeRecipeHandler::processPipeSmall);
        OrePrefix.pipeNormalFluid.addProcessingHandler(fluidPipeProperty, PipeRecipeHandler::processPipeNormal);
        OrePrefix.pipeLargeFluid.addProcessingHandler(fluidPipeProperty, PipeRecipeHandler::processPipeLarge);
        OrePrefix.pipeHugeFluid.addProcessingHandler(fluidPipeProperty, PipeRecipeHandler::processPipeHuge);

        OrePrefix.pipeTinyItem.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processPipeTiny);
        OrePrefix.pipeSmallItem.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processPipeSmall);
        OrePrefix.pipeNormalItem.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processPipeNormal);
        OrePrefix.pipeLargeItem.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processPipeLarge);
        OrePrefix.pipeHugeItem.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processPipeHuge);

        OrePrefix.pipeSmallRestrictive.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processRestrictivePipe);
        OrePrefix.pipeNormalRestrictive.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processRestrictivePipe);
        OrePrefix.pipeLargeRestrictive.addProcessingHandler(itemPipeProperty, PipeRecipeHandler::processRestrictivePipe);
    }

    private static void processRestrictivePipe(OrePrefix pipePrefix, Material material, ItemPipeProperty property) {
        OrePrefix unrestrictive;
        switch (pipePrefix) {
            case pipeSmallRestrictive: unrestrictive = OrePrefix.pipeSmallItem; break;
            case pipeNormalRestrictive: unrestrictive = OrePrefix.pipeNormalItem; break;
            case pipeLargeRestrictive: unrestrictive = OrePrefix.pipeLargeItem; break;
            default: return;
        }

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .input(unrestrictive, material)
            .input(OrePrefix.ring, Materials.Iron, 2)
            .output(pipePrefix, material)
            .duration(20)
            .EUt(8)
            .buildAndRegister();

        ModHandler.addShapedRecipe(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, pipePrefix.toString()) + "_" + material.toCamelCaseString(),
                OreDictUnifier.get(pipePrefix, material), "PR", "Rh",
                'P', new UnificationEntry(unrestrictive, material), 'R', OreDictUnifier.get(OrePrefix.ring, Materials.Iron));
    }

    private static void processPipeTiny(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
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

    private static void processPipeSmall(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
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

    private static void processPipeNormal(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
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

    private static void processPipeLarge(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
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

    private static void processPipeHuge(OrePrefix pipePrefix, Material material, IMaterialProperty property) {
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
        BlastProperty prop = material.getProperties().getBlastProperty();
        return prop != null && prop.getBlastTemperature() >= 2800 ? 32 : 8;
    }
}
