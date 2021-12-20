package gregtech.loaders.oreprocessing;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.builders.IntCircuitRecipeBuilder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;

import java.util.Map;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.NO_WORKING;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.SHAPE_EXTRUDER_WIRE;

/**
 * Guide to the new GregTech CE: Unofficial Cable Processing.
 *
 * Cable Covering Fluids:
 * - Rubber: This can be used for any cable EV-tier or lower. After that it is unavailable.
 *
 * - Silicone Rubber: This can be used for any cable tier, saving the amount of fluid needed. However, at IV,
 *                    it will require a Foil of the cable material as well, making it undesirable.
 *
 * - Styrene-Butadiene Rubber (SBR): This can be used for any cable tier, and is the most optimal cable-covering
 *                                   fluid available.
 *
 * Extra Materials for Cable Covering:
 * - Polyvinyl Chloride (PVC): At HV and above, you can use a Small Pile of PVC to cheapen the cable recipe,
 *                             halving the amount of fluid needed.
 *
 * - Polyphenylene Sulfide (PPS): At LuV, this foil is required to cover cables. Lower tiers will not use it.
 */
public class WireRecipeHandler {

    private static final Map<OrePrefix, Integer> INSULATION_AMOUNT = ImmutableMap.of(
            cableGtSingle, 1,
            cableGtDouble, 1,
            cableGtQuadruple, 2,
            cableGtOctal, 3,
            cableGtHex, 5
    );

    public static void register() {

        // Generate 1x Wire creation recipes (Wiremill, Extruder, Wire Cutters)
        wireGtSingle.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::processWireSingle);

        // Generate Cable Covering Recipes
        wireGtSingle.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtDouble.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtQuadruple.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtOctal.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtHex.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
    }


    public static void processWireSingle(OrePrefix wirePrefix, Material material, WireProperties property) {
        EXTRUDER_RECIPES.recipeBuilder()
                .input(ingot, material)
                .notConsumable(SHAPE_EXTRUDER_WIRE)
                .output(wireGtSingle, material, 2)
                .duration((int) material.getMass() * 2)
                .EUt(6 * getVoltageMultiplier(material))
                .buildAndRegister();

        WIREMILL_RECIPES.recipeBuilder()
                .input(ingot, material)
                .output(wireGtSingle, material, 2)
                .duration((int) material.getMass())
                .EUt(getVoltageMultiplier(material))
                .buildAndRegister();

        if (!material.hasFlag(NO_WORKING)) {
            ModHandler.addShapedRecipe(String.format("%s_wire_single", material),
                    OreDictUnifier.get(wireGtSingle, material), "Xx",
                    'X', new UnificationEntry(plate, material));
        }
    }

    public static void generateCableCovering(OrePrefix wirePrefix, Material material, WireProperties property) {

        // Superconductors have no Cables, so exit early
        if (property.isSuperconductor()) return;

        int cableAmount = (int) (wirePrefix.materialAmount * 2 / GTValues.M);
        OrePrefix cablePrefix = OrePrefix.getPrefix("cable" + wirePrefix.name().substring(4));
        int voltageTier = GTUtility.getTierByVoltage(property.getVoltage());
        int insulationAmount = INSULATION_AMOUNT.get(cablePrefix);

        // Generate hand-crafting recipes for ULV and LV cables
        if (voltageTier <= GTValues.LV) {
            generateManualRecipe(wirePrefix, material, cablePrefix, cableAmount);
        }

        // Rubber Recipe (ULV-EV cables)
        if (voltageTier <= GTValues.EV) {
            IntCircuitRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ULV]).duration(100)
                    .input(wirePrefix, material)
                    .output(cablePrefix, material);

            // Add a recipe using PVC if HV or above.
            // Also apply a circuit to the non-PVC recipe to avoid conflicts.
            if (voltageTier >= GTValues.HV) {
                builder.copy()
                        .input(dustSmall, PolyvinylChloride, insulationAmount)
                        .fluidInputs(Rubber.getFluid(GTValues.L * insulationAmount / 2))
                        .buildAndRegister();
                builder.circuitMeta(24);
            }
            builder.fluidInputs(Rubber.getFluid(GTValues.L * insulationAmount))
                    .buildAndRegister();
        }

        // Silicone Rubber Recipe (all cables)
        IntCircuitRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ULV]).duration(100)
                .input(wirePrefix, material)
                .output(cablePrefix, material);

        // Apply a Polyphenylene Sulfate Foil if LuV or above.
        if (voltageTier >= GTValues.LuV) {
            builder.input(foil, PolyphenyleneSulfide, insulationAmount);
        }

        // Apply Material Foil if IV or above.
        if (voltageTier >= GTValues.IV) {
            builder.input(foil, material, insulationAmount);
        }

        // Add a recipe using PVC if HV or above.
        // Also apply a circuit to the non-PVC recipe to avoid conflicts.
        if (voltageTier >= GTValues.HV) {
            builder.copy()
                    .input(dustSmall, PolyvinylChloride, insulationAmount)
                    .fluidInputs(SiliconeRubber.getFluid(GTValues.L * insulationAmount / 4))
                    .buildAndRegister();
            builder.circuitMeta(24);
        }

        builder.fluidInputs(SiliconeRubber.getFluid(GTValues.L * insulationAmount / 2))
                .buildAndRegister();

        // Styrene Butadiene Rubber Recipe (all cables)
        builder = ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ULV]).duration(100)
                .input(wirePrefix, material)
                .output(cablePrefix, material);

        // Apply a Polyphenylene Sulfate Foil if LuV or above.
        if (voltageTier >= GTValues.LuV) {
            builder.input(foil, PolyphenyleneSulfide, insulationAmount);
        }

        // Add a recipe using PVC if HV or above.
        // Also apply a circuit to the non-PVC recipe to avoid conflicts.
        if (voltageTier >= GTValues.HV) {
            builder.copy()
                    .input(dustSmall, PolyvinylChloride, insulationAmount)
                    .fluidInputs(StyreneButadieneRubber.getFluid(GTValues.L * insulationAmount / 8))
                    .buildAndRegister();
            builder.circuitMeta(24);
        }

        builder.fluidInputs(StyreneButadieneRubber.getFluid(GTValues.L * insulationAmount / 4))
                .buildAndRegister();
    }

    private static void generateManualRecipe(OrePrefix wirePrefix, Material material, OrePrefix cablePrefix, int cableAmount) {
        int insulationAmount = INSULATION_AMOUNT.get(cablePrefix);
        Object[] ingredients = new Object[insulationAmount + 1];
        ingredients[0] = new UnificationEntry(wirePrefix, material);
        for (int i = 1; i <= insulationAmount; i++) {
            ingredients[i] = OreDictUnifier.get(plate, Rubber);
        }
        ModHandler.addShapelessRecipe(String.format("%s_cable_%d", material, cableAmount),
                OreDictUnifier.get(cablePrefix, material),
                ingredients
        );

        PACKER_RECIPES.recipeBuilder()
                .input(wirePrefix, material)
                .input(plate, Rubber, insulationAmount)
                .output(cablePrefix, material)
                .duration(100).EUt(VA[ULV])
                .buildAndRegister();
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
