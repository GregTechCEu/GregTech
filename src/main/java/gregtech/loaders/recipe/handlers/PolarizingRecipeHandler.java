package gregtech.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;

public class PolarizingRecipeHandler {

    private static final OrePrefix[] POLARIZING_PREFIXES = new OrePrefix[]{
            OrePrefix.stick, OrePrefix.stickLong, OrePrefix.plate, OrePrefix.ingot, OrePrefix.plateDense, OrePrefix.rotor,
            OrePrefix.bolt, OrePrefix.screw, OrePrefix.wireFine, OrePrefix.foil, OrePrefix.ring};

    public static void register() {
        for (OrePrefix orePrefix : POLARIZING_PREFIXES) {
            orePrefix.addProcessingHandler(PropertyKey.INGOT, PolarizingRecipeHandler::processPolarizing);
        }
    }

    public static void processPolarizing(OrePrefix polarizingPrefix, Material material, IngotProperty property) {
        Material magneticMaterial = property.getMagneticMaterial();

        if (magneticMaterial != null && polarizingPrefix.doGenerateItem(magneticMaterial)) {
            ItemStack magneticStack = OreDictUnifier.get(polarizingPrefix, magneticMaterial);
            RecipeMaps.POLARIZER_RECIPES.recipeBuilder() //polarizing
                    .input(polarizingPrefix, material)
                    .outputs(magneticStack)
                    .duration((int) ((int) material.getMass() * polarizingPrefix.getMaterialAmount(material) / GTValues.M))
                    .EUt(8 * getVoltageMultiplier(material))
                    .buildAndRegister();

            ModHandler.addSmeltingRecipe(new UnificationEntry(polarizingPrefix, magneticMaterial),
                    OreDictUnifier.get(polarizingPrefix, material)); //de-magnetizing
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 1200 ? VA[LV] : 2;
    }

}
