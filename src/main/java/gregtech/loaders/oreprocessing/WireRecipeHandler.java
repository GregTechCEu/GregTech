package gregtech.loaders.oreprocessing;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.WireProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

import static gregtech.api.GTValues.M;
import static gregtech.api.recipes.RecipeMaps.PACKER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.UNPACKER_RECIPES;
import static gregtech.api.unification.material.info.MaterialFlags.NO_WORKING;

public class WireRecipeHandler {

    public static final Map<Material, Integer> INSULATION_MATERIALS = new HashMap<>();

    static {
        INSULATION_MATERIALS.put(Materials.Rubber, GTValues.HV);
        INSULATION_MATERIALS.put(Materials.StyreneButadieneRubber, GTValues.LuV);
        INSULATION_MATERIALS.put(Materials.SiliconeRubber, GTValues.UHV);
    }

    public static void register() {
        OrePrefix.wireGtSingle.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::processWireSingle);
        OrePrefix.wireGtSingle.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::processWireCompression);
        for (OrePrefix wirePrefix : WIRE_DOUBLING_ORDER) {
            wirePrefix.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateWireRecipe);
            wirePrefix.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateWireCombiningRecipe);
        }

        for (OrePrefix cablePrefix : CABLE_DOUBLING_ORDER) {
            cablePrefix.addProcessingHandler(PropertyKey.WIRE, WireRecipeHandler::generateCableCombiningRecipe);
        }
    }

    private static final OrePrefix[] WIRE_DOUBLING_ORDER = new OrePrefix[]{
        OrePrefix.wireGtSingle, OrePrefix.wireGtDouble, OrePrefix.wireGtQuadruple, OrePrefix.wireGtOctal, OrePrefix.wireGtHex
    };

    private static final OrePrefix[] CABLE_DOUBLING_ORDER = new OrePrefix[]{
        OrePrefix.cableGtSingle, OrePrefix.cableGtDouble, OrePrefix.cableGtQuadruple, OrePrefix.cableGtOctal, OrePrefix.cableGtHex
    };

    public static void processWireSingle(OrePrefix wirePrefix, Material material, WireProperty property) {
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_WIRE)
            .outputs(OreDictUnifier.get(OrePrefix.wireGtSingle, material, 2))
            .duration((int) material.getAverageMass() * 2)
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material)
            .outputs(OreDictUnifier.get(OrePrefix.wireGtSingle, material, 2))
            .duration((int) material.getAverageMass())
            .EUt(getVoltageMultiplier(material))
            .buildAndRegister();

        if (!material.hasFlag(NO_WORKING)) {
            ModHandler.addShapedRecipe(String.format("%s_wire_single", material),
                OreDictUnifier.get(OrePrefix.wireGtSingle, material), "Xx",
                'X', new UnificationEntry(OrePrefix.plate, material));
        }
    }

    public static void generateWireRecipe(OrePrefix wirePrefix, Material material, WireProperty property) {
        int cableAmount = (int) (wirePrefix.materialAmount * 2 / M);
        OrePrefix cablePrefix = OrePrefix.valueOf("cable" + wirePrefix.name().substring(4));
        ItemStack cableStack = OreDictUnifier.get(cablePrefix, material);

        if (isManualInsulatedCable(property)) {
            if (cableAmount <= 8) {
                Object[] ingredients = new Object[1 + cableAmount];
                ingredients[0] = new UnificationEntry(wirePrefix, material);
                for (int i = 1; i < ingredients.length; i++) {
                    ingredients[i] = OreDictUnifier.get(OrePrefix.plate, Materials.Rubber);
                }
                ModHandler.addShapelessRecipe(String.format("%s_cable_%d", material, cableAmount), cableStack, ingredients);
            }
        }

        if (isManualInsulatedCable(property)) {
            ItemStack rubberStack = OreDictUnifier.get(OrePrefix.plate, Materials.Rubber, cableAmount);
            RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(wirePrefix, material)
                .inputs(rubberStack)
                .outputs(cableStack)
                .duration(100).EUt(8)
                .buildAndRegister();
        }

        for(Material insulationMaterial : INSULATION_MATERIALS.keySet()) {
            int cableTier = GTUtility.getTierByVoltage(property.voltage);
            int materialAmount = getMaterialAmount(cableTier, INSULATION_MATERIALS.get(insulationMaterial));
            if (materialAmount == -1) continue;

            if (wirePrefix != OrePrefix.wireGtSingle) {
                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.wireGtSingle, material, cableAmount).circuitMeta(24 + ArrayUtils.indexOf(WIRE_DOUBLING_ORDER, wirePrefix))
                    .fluidInputs(insulationMaterial.getFluid(materialAmount * cableAmount))
                    .outputs(cableStack)
                    .duration(150).EUt(8)
                    .buildAndRegister();
            }
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(wirePrefix, material).circuitMeta(24)
                .fluidInputs(insulationMaterial.getFluid(materialAmount * cableAmount))
                .outputs(cableStack)
                .duration(150).EUt(8)
                .buildAndRegister();
        }
    }


    public static void generateWireCombiningRecipe(OrePrefix wirePrefix, Material material, WireProperty property) {
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

    public static void generateCableCombiningRecipe(OrePrefix cablePrefix, Material material, WireProperty property) {
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

    /**
     * Wire compression Material Handler
     */
    private static void processWireCompression(OrePrefix prefix, Material material, WireProperty property) {
        for(int startTier = 0; startTier < 4; startTier++) {
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

    private static int getMaterialAmount(int cableTier, int insulationTier) {
        if (cableTier > insulationTier) {
            return -1;
        }
        int insulationDiscount = (insulationTier - cableTier) / 2;
        return Math.max(36, 144 / (1 + insulationDiscount));
    }

    public static boolean isManualInsulatedCable(WireProperty property) {
        return GTUtility.getTierByVoltage(property.voltage) <= 1;
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? 32 : 8;
    }

}
