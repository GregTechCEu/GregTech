package gregtech.loaders.oreprocessing;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.item.ItemStack;

public class PolarizingRecipeHandler {

    private static final OrePrefix[] POLARIZING_PREFIXES = new OrePrefix[]{
        OrePrefix.stick, OrePrefix.stickLong, OrePrefix.plate, OrePrefix.ingot, OrePrefix.plateDense, OrePrefix.rotor,
        OrePrefix.bolt, OrePrefix.screw, OrePrefix.wireFine, OrePrefix.foil, OrePrefix.dust, OrePrefix.ring};

    public static void register() {
        for (OrePrefix orePrefix : POLARIZING_PREFIXES) {
            orePrefix.addProcessingHandler(IngotMaterial.class, PolarizingRecipeHandler::processPolarizing);
        }
    }

    public static void processPolarizing(OrePrefix polarizingPrefix, IngotMaterial material) {
        if (material.magneticMaterial != null && polarizingPrefix.doGenerateItem(material.magneticMaterial)) {
            ItemStack magneticStack = OreDictUnifier.get(polarizingPrefix, material.magneticMaterial);
            RecipeMaps.POLARIZER_RECIPES.recipeBuilder() //polarizing
                .input(polarizingPrefix, material)
                .outputs(magneticStack)
                .duration((int) ((int) material.getAverageMass() * polarizingPrefix.materialAmount / GTValues.M))
                .EUt(8 * getVoltageMultiplier(material))
                .buildAndRegister();

            ModHandler.addSmeltingRecipe(new UnificationEntry(polarizingPrefix, material.magneticMaterial),
                OreDictUnifier.get(polarizingPrefix, material)); //de-magnetizing
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material instanceof IngotMaterial && ((IngotMaterial) material)
                .blastFurnaceTemperature >= 1200 ? 32 : 2;
    }

}
