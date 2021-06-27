package gregtech.loaders.oreprocessing;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.*;
import gregtech.api.unification.material.type.DustMaterial.MatFlags;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.L;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.GENERATE_PLATE;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.NO_SMASHING;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.GENERATE_BOLT_SCREW;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.GENERATE_SPRING;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.GENERATE_ROD;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.MORTAR_GRINDABLE;
import static gregtech.api.util.DyeUtil.determineDyeColor;

public class PartsRecipeHandler {

    private PartsRecipeHandler() {
    }

    public static void register() {
        OrePrefix.stick.addProcessingHandler(DustMaterial.class, PartsRecipeHandler::processStick);
        OrePrefix.stickLong.addProcessingHandler(DustMaterial.class, PartsRecipeHandler::processLongStick);
        OrePrefix.plate.addProcessingHandler(DustMaterial.class, PartsRecipeHandler::processPlate);
        OrePrefix.plateDense.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processPlateDense);
        OrePrefix.compressed.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processCompressed);

        OrePrefix.turbineBlade.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processTurbine);
        OrePrefix.rotor.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processRotor);
        OrePrefix.bolt.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processBolt);
        OrePrefix.screw.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processScrew);
        OrePrefix.wireFine.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processFineWire);
        OrePrefix.foil.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processFoil);
        OrePrefix.lens.addProcessingHandler(GemMaterial.class, PartsRecipeHandler::processLens);

        OrePrefix.gear.addProcessingHandler(SolidMaterial.class, PartsRecipeHandler::processGear);
        OrePrefix.gearSmall.addProcessingHandler(SolidMaterial.class, PartsRecipeHandler::processGear);
        OrePrefix.ring.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processRing);
        OrePrefix.springSmall.addProcessingHandler(IngotMaterial.class, PartsRecipeHandler::processSpringSmall);
    }

    public static void processBolt(OrePrefix boltPrefix, IngotMaterial material) {
        ItemStack boltStack = OreDictUnifier.get(boltPrefix, material);
        ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, material);

        ModHandler.addShapedRecipe(String.format("bolt_file_%s", material.toString()),
            boltStack, "fS", "S ",
            'S', new UnificationEntry(OrePrefix.screw, material));

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
            .input(OrePrefix.screw, material)
            .outputs(boltStack)
            .duration(20)
            .EUt(24)
            .buildAndRegister();

        if (!boltStack.isEmpty() && !ingotStack.isEmpty()) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .notConsumable(MetaItems.SHAPE_EXTRUDER_BOLT)
                .input(OrePrefix.ingot, material)
                .outputs(GTUtility.copyAmount(8, boltStack))
                .duration(15)
                .EUt(120)
                .buildAndRegister();
        }
    }

    public static void processScrew(OrePrefix screwPrefix, IngotMaterial material) {
        ItemStack screwStack = OreDictUnifier.get(screwPrefix, material);

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
            .input(OrePrefix.bolt, material)
            .outputs(screwStack)
            .duration((int) Math.max(1, material.getAverageMass() / 8L))
            .EUt(4)
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("screw_%s", material.toString()),
            screwStack, "fX", "X ",
            'X', new UnificationEntry(OrePrefix.bolt, material));
    }

    public static void processFoil(OrePrefix foilPrefix, IngotMaterial material) {
        ItemStack foilStack = OreDictUnifier.get(foilPrefix, material);
        RecipeMaps.BENDER_RECIPES.recipeBuilder()
            .input(OrePrefix.plate, material)
            .outputs(GTUtility.copyAmount(4, foilStack))
            .duration((int) material.getAverageMass())
            .EUt(24)
            .circuitMeta(0)
            .buildAndRegister();
    }

    public static void processFineWire(OrePrefix fineWirePrefix, IngotMaterial material) {
        ItemStack fineWireStack = OreDictUnifier.get(fineWirePrefix, material);
        ModHandler.addShapelessRecipe(String.format("fine_wire_%s", material.toString()),
            fineWireStack, 'x', new UnificationEntry(OrePrefix.foil, material));
        if (material.cableProperties != null) {
            RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
                .input(OrePrefix.wireGtSingle, material)
                .outputs(OreDictUnifier.get(OrePrefix.wireFine, material, 4))
                .duration(200)
                .EUt(8)
                .buildAndRegister();
        } else {
            RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material)
                .outputs(OreDictUnifier.get(OrePrefix.wireFine, material, 8))
                .duration(400)
                .EUt(8)
                .buildAndRegister();
        }
    }

    public static void processGear(OrePrefix gearPrefix, SolidMaterial material) {
        ItemStack stack = OreDictUnifier.get(gearPrefix, material);
        if (gearPrefix == OrePrefix.gear && material instanceof IngotMaterial) {
            int voltageMultiplier = getVoltageMultiplier(material);
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material, 4)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR)
                .outputs(OreDictUnifier.get(gearPrefix, material))
                .duration((int) material.getAverageMass() * 5)
                .EUt(8 * voltageMultiplier)
                .buildAndRegister();

            RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material, 8)
                .notConsumable(MetaItems.SHAPE_MOLD_GEAR)
                .outputs(OreDictUnifier.get(gearPrefix, material))
                .duration((int) material.getAverageMass() * 10)
                .EUt(2 * voltageMultiplier)
                .buildAndRegister();
        }

        if (material.shouldGenerateFluid()) {
            boolean isSmall = gearPrefix == OrePrefix.gearSmall;
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .notConsumable(isSmall ? MetaItems.SHAPE_MOLD_GEAR_SMALL : MetaItems.SHAPE_MOLD_GEAR)
                .fluidInputs(material.getFluid(L * (isSmall ? 1 : 4)))
                .outputs(stack)
                .duration(isSmall ? 20 : 100)
                .EUt(8)
                .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_PLATE | GENERATE_ROD)) {
            if (gearPrefix == OrePrefix.gearSmall) {
                ModHandler.addShapedRecipe(String.format("small_gear_%s", material), stack,
                    "h ", " P", 'P', new UnificationEntry(OrePrefix.plate, material));
                RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material)
                    .outputs(stack)
                    .duration(60)
                    .EUt(6)
                    .buildAndRegister();
            } else {
                ModHandler.addShapedRecipe(String.format("gear_%s", material), stack,
                    "RPR", "PdP", "RPR",
                    'P', new UnificationEntry(OrePrefix.plate, material),
                    'R', new UnificationEntry(OrePrefix.stick, material));
            }
        }
    }

    public static void processLens(OrePrefix lensPrefix, GemMaterial material) {
        ItemStack stack = OreDictUnifier.get(lensPrefix, material);

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
            .input(OrePrefix.plate, material)
            .outputs(stack, OreDictUnifier.get(OrePrefix.dustSmall, material))
            .duration((int) (material.getAverageMass() / 2L))
            .EUt(16)
            .buildAndRegister();

        EnumDyeColor dyeColor = determineDyeColor(material.materialRGB);
        MarkerMaterial colorMaterial = MarkerMaterials.Color.COLORS.get(dyeColor);
        OreDictUnifier.registerOre(stack, OrePrefix.craftingLens, colorMaterial);
    }

    public static void processPlate(OrePrefix platePrefix, DustMaterial material) {
        if (material.shouldGenerateFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .notConsumable(MetaItems.SHAPE_MOLD_PLATE)
                .fluidInputs(material.getFluid(L))
                .outputs(OreDictUnifier.get(platePrefix, material))
                .duration(40)
                .EUt(8)
                .buildAndRegister();
        }

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            ItemStack dustStack = OreDictUnifier.get(OrePrefix.dust, material);
            ModHandler.addShapedRecipe(String.format("plate_to_dust_%s", material),
                dustStack, "X", "m",
                'X', new UnificationEntry(OrePrefix.plate, material));
        }
    }

    public static void processCompressed(OrePrefix compressed, SolidMaterial material) {
        if (!material.hasFlag(MatFlags.GENERATE_PLATE)) return;
        ItemStack compressedStack = OreDictUnifier.get(compressed, material);
        RecipeMaps.IMPLOSION_RECIPES.recipeBuilder()
            .input(OrePrefix.plank, material, 2)
            .explosivesAmount(2)
            .outputs(compressedStack, OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh))
            .buildAndRegister();
    }

    public static void processPlateDense(OrePrefix orePrefix, IngotMaterial material) {
        RecipeMaps.BENDER_RECIPES.recipeBuilder()
            .input(OrePrefix.plate, material, 9)
            .circuitMeta(2)
            .outputs(OreDictUnifier.get(orePrefix, material))
            .duration((int) Math.max(material.getAverageMass() * 9L, 1L))
            .EUt(96)
            .buildAndRegister();
    }

    public static void processRing(OrePrefix ringPrefix, IngotMaterial material) {
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_RING)
            .outputs(OreDictUnifier.get(ringPrefix, material, 4))
            .duration((int) material.getAverageMass() * 2)
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        if (!material.hasFlag(NO_SMASHING)) {
            ModHandler.addShapedRecipe(String.format("ring_%s", material),
                OreDictUnifier.get(ringPrefix, material),
                "h ", " X",
                'X', new UnificationEntry(OrePrefix.stick, material));
        }
    }

    public static void processSpringSmall(OrePrefix springPrefix, IngotMaterial material) {
        if (material.cableProperties != null) {
            RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(OrePrefix.wireGtSingle, material)
                .outputs(OreDictUnifier.get(OrePrefix.springSmall, material, 2))
                .duration(100)
                .EUt(8)
                .buildAndRegister();
        }
    }

    public static void processRotor(OrePrefix rotorPrefix, SolidMaterial material) {
        ItemStack stack = OreDictUnifier.get(rotorPrefix, material);
        ModHandler.addShapedRecipe(String.format("rotor_%s", material.toString()), stack,
            "PdP", " R ", "PSP",
            'P', new UnificationEntry(OrePrefix.plate, material),
            'R', new UnificationEntry(OrePrefix.ring, material),
            'S', new UnificationEntry(OrePrefix.screw, material));

        if (material.shouldGenerateFluid()) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .notConsumable(MetaItems.SHAPE_MOLD_ROTOR)
                .fluidInputs(material.getFluid(L * 4))
                .outputs(OreDictUnifier.get(rotorPrefix, material))
                .duration(120)
                .EUt(20)
                .buildAndRegister();
        }
    }

    public static void processStick(OrePrefix stickPrefix, DustMaterial material) {
        if (material instanceof GemMaterial || material instanceof IngotMaterial) {
            RecipeMaps.LATHE_RECIPES.recipeBuilder()
                .input(material instanceof GemMaterial ? OrePrefix.gem : OrePrefix.ingot, material)
                .outputs(OreDictUnifier.get(OrePrefix.stick, material, 2))
                .duration((int) Math.max(material.getAverageMass() * 2, 1))
                .EUt(16)
                .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_BOLT_SCREW)) {
            ItemStack boltStack = OreDictUnifier.get(OrePrefix.bolt, material);
            RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .input(stickPrefix, material)
                .outputs(GTUtility.copyAmount(4, boltStack))
                .duration((int) Math.max(material.getAverageMass() * 2L, 1L))
                .EUt(4)
                .buildAndRegister();

            ModHandler.addShapedRecipe(String.format("bolt_saw_%s", material.toString()),
                GTUtility.copyAmount(2, boltStack),
                "s ", " X",
                'X', new UnificationEntry(OrePrefix.stick, material));
        }
    }

    public static void processLongStick(OrePrefix longStickPrefix, DustMaterial material) {
        ItemStack stack = OreDictUnifier.get(longStickPrefix, material);
        ItemStack stickStack = OreDictUnifier.get(OrePrefix.stick, material);

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
            .input(longStickPrefix, material)
            .outputs(GTUtility.copyAmount(2, stickStack))
            .duration((int) Math.max(material.getAverageMass(), 1L)).EUt(4)
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("stick_long_%s", material.toString()),
            GTUtility.copyAmount(2, stickStack),
            "s", "X", 'X', new UnificationEntry(OrePrefix.stickLong, material));

        ModHandler.addShapedRecipe(String.format("stick_long_gem_flawless_%s", material.toString()),
            stickStack,
            "sf",
            "G ",
            'G', new UnificationEntry(OrePrefix.gemFlawless, material));

        ModHandler.addShapedRecipe(String.format("stick_long_gem_exquisite_%s", material.toString()),
            GTUtility.copyAmount(2, stickStack),
            "sf", "G ",
            'G', new UnificationEntry(OrePrefix.gemExquisite, material));

        if (material.hasFlag(GENERATE_SPRING)) {
            RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(longStickPrefix, material)
                .outputs(OreDictUnifier.get(OrePrefix.spring, material))
                .circuitMeta(1)
                .duration(200)
                .EUt(16)
                .buildAndRegister();
        }

        ModHandler.addShapedRecipe(String.format("stick_long_stick_%s", material.toString()), stack,
            "ShS",
            'S', new UnificationEntry(OrePrefix.stick, material));

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(OrePrefix.stick, material, 2)
            .outputs(stack)
            .duration((int) Math.max(material.getAverageMass(), 1L))
            .EUt(16)
            .buildAndRegister();
    }

    public static void processTurbine(OrePrefix toolPrefix, IngotMaterial material) {
        ItemStack rotorStack = MetaItems.TURBINE_ROTOR.getStackForm();
        //noinspection ConstantConditions
        TurbineRotorBehavior.getInstanceFor(rotorStack).setPartMaterial(rotorStack, material);

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .input(OrePrefix.turbineBlade, material, 8)
            .input(OrePrefix.stickLong, Materials.Magnalium)
            .outputs(rotorStack)
            .duration(200)
            .EUt(400)
            .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .input(OrePrefix.plate, material, 5)
            .input(OrePrefix.screw, material, 2)
            .outputs(OreDictUnifier.get(toolPrefix, material))
            .duration(20)
            .EUt(256)
            .circuitMeta(10)
            .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("turbine_blade_%s", material),
            OreDictUnifier.get(toolPrefix, material),
            "PPP", "SPS", "fPd",
            'P', new UnificationEntry(OrePrefix.plate, material),
            'S', new UnificationEntry(OrePrefix.screw, material));
    }

    private static int getVoltageMultiplier(Material material) {
        return material instanceof IngotMaterial && ((IngotMaterial) material)
            .blastFurnaceTemperature >= 2800 ? 32 : 8;
    }
}
