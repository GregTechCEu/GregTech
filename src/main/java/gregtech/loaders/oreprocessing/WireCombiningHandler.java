package gregtech.loaders.oreprocessing;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import org.apache.commons.lang3.ArrayUtils;

import static gregtech.api.recipes.RecipeMaps.PACKER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.UNPACKER_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.*;

public class WireCombiningHandler {

    private static final OrePrefix[] WIRE_DOUBLING_ORDER = new OrePrefix[]{
            wireGtSingle, wireGtDouble, wireGtQuadruple, wireGtOctal, wireGtHex
    };

    private static final OrePrefix[] CABLE_DOUBLING_ORDER = new OrePrefix[]{
            cableGtSingle, cableGtDouble, cableGtQuadruple, cableGtOctal, cableGtHex
    };

    public static void register() {

        // Generate Wire Packer/Unpacker recipes TODO Move into generateWireCombining? Add for cables?
        wireGtSingle.addProcessingHandler(PropertyKey.WIRE, WireCombiningHandler::processWireCompression);

        // Generate manual recipes for combining Wires/Cables
        for (OrePrefix wirePrefix : WIRE_DOUBLING_ORDER) {
            wirePrefix.addProcessingHandler(PropertyKey.WIRE, WireCombiningHandler::generateWireCombiningRecipe);
        }

        for (OrePrefix cablePrefix : CABLE_DOUBLING_ORDER) {
            cablePrefix.addProcessingHandler(PropertyKey.WIRE, WireCombiningHandler::generateCableCombiningRecipe);
        }
    }

    private static void generateWireCombiningRecipe(OrePrefix wirePrefix, Material material, WireProperties property) {
        int wireIndex = ArrayUtils.indexOf(WIRE_DOUBLING_ORDER, wirePrefix);

        if (wireIndex < WIRE_DOUBLING_ORDER.length - 1) {
            ModHandler.addShapelessRecipe(String.format("%s_wire_%s_doubling", material, wirePrefix),
                    OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex + 1], material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material));
        }

        if (wireIndex > 0) {
            ModHandler.addShapelessRecipe(String.format("%s_wire_%s_splitting", material, wirePrefix),
                    OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex - 1], material, 2),
                    new UnificationEntry(wirePrefix, material));
        }

        if (wireIndex < 3) {
            ModHandler.addShapelessRecipe(String.format("%s_wire_%s_quadrupling", material, wirePrefix),
                    OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex + 2], material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material));
        }
    }

    private static void generateCableCombiningRecipe(OrePrefix cablePrefix, Material material, WireProperties property) {
        if (property.isSuperconductor) return;
        int cableIndex = ArrayUtils.indexOf(CABLE_DOUBLING_ORDER, cablePrefix);

        if (cableIndex < CABLE_DOUBLING_ORDER.length - 1) {
            ModHandler.addShapelessRecipe(String.format("%s_cable_%s_doubling", material, cablePrefix),
                    OreDictUnifier.get(CABLE_DOUBLING_ORDER[cableIndex + 1], material),
                    new UnificationEntry(cablePrefix, material),
                    new UnificationEntry(cablePrefix, material));
        }

        if (cableIndex > 0) {
            ModHandler.addShapelessRecipe(String.format("%s_cable_%s_splitting", material, cablePrefix),
                    OreDictUnifier.get(CABLE_DOUBLING_ORDER[cableIndex - 1], material, 2),
                    new UnificationEntry(cablePrefix, material));
        }

        if (cableIndex < 3) {
            ModHandler.addShapelessRecipe(String.format("%s_cable_%s_quadrupling", material, cablePrefix),
                    OreDictUnifier.get(CABLE_DOUBLING_ORDER[cableIndex + 2], material),
                    new UnificationEntry(cablePrefix, material),
                    new UnificationEntry(cablePrefix, material),
                    new UnificationEntry(cablePrefix, material),
                    new UnificationEntry(cablePrefix, material));
        }
    }

    private static void processWireCompression(OrePrefix prefix, Material material, WireProperties property) {
        for (int startTier = 0; startTier < 4; startTier++) {
            for (int i = 1; i < 5 - startTier; i++) {
                PACKER_RECIPES.recipeBuilder()
                        .inputs(OreDictUnifier.get(WIRE_DOUBLING_ORDER[startTier], material, 1 << i))
                        .notConsumable(new IntCircuitIngredient((int) Math.pow(2, i)))
                        .outputs(OreDictUnifier.get(WIRE_DOUBLING_ORDER[startTier + i], material, 1))
                        .buildAndRegister();
            }
        }

        for (int i = 1; i < 5; i++) {
            UNPACKER_RECIPES.recipeBuilder()
                    .inputs(OreDictUnifier.get(WIRE_DOUBLING_ORDER[i], material, 1))
                    .notConsumable(new IntCircuitIngredient(1))
                    .outputs(OreDictUnifier.get(WIRE_DOUBLING_ORDER[0], material, (int) Math.pow(2, i)))
                    .buildAndRegister();
        }
    }
}
