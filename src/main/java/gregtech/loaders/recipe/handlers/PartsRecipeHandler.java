package gregtech.loaders.recipe.handlers;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.GemProperty;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.AbstractMaterialPartBehavior;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.BENDER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.LATHE_RECIPES;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.util.DyeUtil.determineDyeColor;

public class PartsRecipeHandler {

    private PartsRecipeHandler() {}

    public static void register() {
        OrePrefix.stick.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processStick);
        OrePrefix.stickLong.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processLongStick);
        OrePrefix.plate.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processPlate);
        OrePrefix.plateDouble.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processPlateDouble);
        OrePrefix.plateDense.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processPlateDense);

        OrePrefix.turbineBlade.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processTurbine);
        OrePrefix.rotor.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processRotor);
        OrePrefix.bolt.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processBolt);
        OrePrefix.screw.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processScrew);
        OrePrefix.wireFine.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processFineWire);
        OrePrefix.foil.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processFoil);
        OrePrefix.lens.addProcessingHandler(PropertyKey.GEM, PartsRecipeHandler::processLens);

        OrePrefix.gear.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processGear);
        OrePrefix.gearSmall.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processGear);
        OrePrefix.ring.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processRing);
        OrePrefix.springSmall.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processSpringSmall);
        OrePrefix.spring.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processSpring);
        OrePrefix.round.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processRound);
    }

    public static void processBolt(OrePrefix boltPrefix, Material material, DustProperty property) {
        ItemStack boltStack = OreDictUnifier.get(boltPrefix, material);
        ItemStack ingotStack = OreDictUnifier.get(OrePrefix.ingot, material);
        int workingTier = material.getWorkingTier();

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .input(OrePrefix.screw, material)
                .outputs(boltStack)
                .duration(20)
                .EUt(GTUtility.scaleVoltage(24, workingTier))
                .buildAndRegister();

        if (!boltStack.isEmpty() && !ingotStack.isEmpty()) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_BOLT)
                    .outputs(GTUtility.copy(8, boltStack))
                    .duration(15)
                    .EUt(GTUtility.scaleVoltage(VA[MV], workingTier))
                    .buildAndRegister();

            if (material.hasFlag(NO_SMASHING)) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.dust, material)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_BOLT)
                        .outputs(GTUtility.copy(8, boltStack))
                        .duration(15)
                        .EUt(GTUtility.scaleVoltage(VA[MV], workingTier))
                        .buildAndRegister();
            }
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_BOLT)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L / 8))
                    .output(boltPrefix, material)
                    .EUt(VA[ULV]).duration(6)
                    .buildAndRegister();
        }
    }

    public static void processScrew(OrePrefix screwPrefix, Material material, DustProperty property) {
        ItemStack screwStack = OreDictUnifier.get(screwPrefix, material);
        int workingTier = material.getWorkingTier();

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
                .input(OrePrefix.bolt, material)
                .outputs(screwStack)
                .duration((int) Math.max(1, material.getMass() / 8L))
                .EUt(GTUtility.scaleVoltage(4, workingTier))
                .buildAndRegister();

        if (workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("screw_%s", material),
                    screwStack, "fX", "X ",
                    'X', new UnificationEntry(OrePrefix.bolt, material));
        }
    }

    public static void processFoil(OrePrefix foilPrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        if (!material.hasFlag(NO_SMASHING) && workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("foil_%s", material),
                    OreDictUnifier.get(foilPrefix, material, 2),
                    "hP ", 'P', new UnificationEntry(plate, material));
        }

        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(plate, material)
                .output(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(GTUtility.scaleVoltage(24, workingTier))
                .circuitMeta(1)
                .buildAndRegister();

        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(ingot, material)
                .output(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(GTUtility.scaleVoltage(24, workingTier))
                .circuitMeta(10)
                .buildAndRegister();

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(ingot, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_FOIL)
                    .output(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(GTUtility.scaleVoltage(24, workingTier))
                    .buildAndRegister();

            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(dust, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_FOIL)
                    .output(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(GTUtility.scaleVoltage(24, workingTier))
                    .buildAndRegister();
        }
    }

    public static void processFineWire(OrePrefix fineWirePrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        if (!OreDictUnifier.get(foil, material).isEmpty() && workingTier <= HV)
            ModHandler.addShapelessRecipe(String.format("fine_wire_%s", material.toString()),
                    OreDictUnifier.get(fineWirePrefix, material),
                    'x', new UnificationEntry(OrePrefix.foil, material));

        if (material.hasProperty(PropertyKey.WIRE)) {
            RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
                    .input(OrePrefix.wireGtSingle, material)
                    .circuitMeta(1)
                    .output(fineWirePrefix, material, 4)
                    .duration((int) material.getMass() * 3 / 2)
                    .EUt(GTUtility.scaleVoltage(VA[ULV], workingTier))
                    .buildAndRegister();
        }

        RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material)
                .circuitMeta(3)
                .output(fineWirePrefix, material, 8)
                .duration((int) material.getMass() * 2)
                .EUt(GTUtility.scaleVoltage(VA[ULV], workingTier))
                .buildAndRegister();
    }

    public static void processGear(OrePrefix gearPrefix, Material material, DustProperty property) {
        int workingTier = material.getWorkingTier();

        ItemStack stack = OreDictUnifier.get(gearPrefix, material);
        if (gearPrefix == OrePrefix.gear && material.hasProperty(PropertyKey.INGOT)) {
            long voltageMultiplier = getVoltageMultiplier(material);
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 4)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR)
                    .outputs(OreDictUnifier.get(gearPrefix, material))
                    .duration((int) material.getMass() * 5)
                    .EUt(GTUtility.scaleVoltage(8 * voltageMultiplier, workingTier))
                    .buildAndRegister();

            RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 8)
                    .notConsumable(MetaItems.SHAPE_MOLD_GEAR)
                    .outputs(OreDictUnifier.get(gearPrefix, material))
                    .duration((int) material.getMass() * 10)
                    .EUt(GTUtility.scaleVoltage(2 * voltageMultiplier, workingTier))
                    .buildAndRegister();

            if (material.hasFlag(NO_SMASHING)) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.dust, material, 4)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR)
                        .outputs(OreDictUnifier.get(gearPrefix, material))
                        .duration((int) material.getMass() * 5)
                        .EUt(GTUtility.scaleVoltage(8 * voltageMultiplier, workingTier))
                        .buildAndRegister();
            }
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            boolean isSmall = gearPrefix == OrePrefix.gearSmall;
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(isSmall ? MetaItems.SHAPE_MOLD_GEAR_SMALL : MetaItems.SHAPE_MOLD_GEAR)
                    .fluidInputs(
                            material.getProperty(PropertyKey.FLUID).solidifiesFrom(L * (isSmall ? 1 : 4)))
                    .outputs(stack)
                    .duration(isSmall ? 20 : 100)
                    .EUt(GTUtility.scaleVoltage(VA[ULV], workingTier))
                    .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_PLATE) && material.hasFlag(GENERATE_ROD)) {
            if (gearPrefix == OrePrefix.gearSmall) {
                if (workingTier <= HV) {
                    ModHandler.addShapedRecipe(String.format("small_gear_%s", material),
                            OreDictUnifier.get(gearSmall, material),
                            " R ", "hPx", " R ", 'R', new UnificationEntry(stick, material), 'P',
                            new UnificationEntry(plate, material));
                }

                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.ingot, material)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR_SMALL)
                        .outputs(stack)
                        .duration((int) material.getMass())
                        .EUt(GTUtility.scaleVoltage(material.getBlastTemperature() >= 2800 ? 256 : 64, workingTier))
                        .buildAndRegister();

                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .input(ingot, material, 2)
                        .notConsumable(MetaItems.SHAPE_MOLD_GEAR_SMALL.getStackForm())
                        .output(gearSmall, material)
                        .duration((int) material.getMass())
                        .EUt(GTUtility.scaleVoltage(VA[LV], workingTier))
                        .buildAndRegister();

                if (material.hasFlag(NO_SMASHING)) {
                    RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                            .input(OrePrefix.dust, material)
                            .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR_SMALL)
                            .outputs(stack)
                            .duration((int) material.getMass())
                            .EUt(GTUtility.scaleVoltage(material.getBlastTemperature() >= 2800 ? 256 : 64, workingTier))
                            .buildAndRegister();
                }
            } else if (workingTier <= HV) {
                ModHandler.addShapedRecipe(String.format("gear_%s", material), stack,
                        "RPR", "PwP", "RPR",
                        'P', new UnificationEntry(OrePrefix.plate, material),
                        'R', new UnificationEntry(OrePrefix.stick, material));
            }
        }
    }

    public static void processLens(OrePrefix lensPrefix, Material material, GemProperty property) {
        ItemStack stack = OreDictUnifier.get(lensPrefix, material);
        int workingTier = material.getWorkingTier();

        LATHE_RECIPES.recipeBuilder()
                .input(plate, material)
                .output(lens, material)
                .output(dustSmall, material)
                .duration(1200)
                .EUt(GTUtility.scaleVoltage(VA[MV], workingTier))
                .buildAndRegister();

        if (!OreDictUnifier.get(gemExquisite, material).isEmpty()) {
            LATHE_RECIPES.recipeBuilder()
                    .input(gemExquisite, material)
                    .output(lens, material)
                    .output(dust, material, 2)
                    .duration(2400)
                    .EUt(GTUtility.scaleVoltage(VA[LV], workingTier))
                    .buildAndRegister();
        }

        if (material == Materials.Diamond) { // override Diamond Lens to be LightBlue
            OreDictUnifier.registerOre(stack, OrePrefix.craftingLens, MarkerMaterials.Color.LightBlue);
        } else if (material == Materials.Ruby) { // override Ruby Lens to be Red
            OreDictUnifier.registerOre(stack, OrePrefix.craftingLens, MarkerMaterials.Color.Red);
        } else if (material == Materials.Emerald) { // override Emerald Lens to be Green
            OreDictUnifier.registerOre(stack, OrePrefix.craftingLens, MarkerMaterials.Color.Green);
        } else if (material == Materials.Glass) { // the overriding is done in OreDictionaryLoader to prevent log spam
            OreDictUnifier.registerOre(stack, OrePrefix.craftingLens.name() + material.toCamelCaseString());
        } else { // add more custom lenses here if needed

            // Default behavior for determining lens color, left for addons and CraftTweaker
            EnumDyeColor dyeColor = determineDyeColor(material.getMaterialRGB());
            MarkerMaterial colorMaterial = MarkerMaterials.Color.COLORS.get(dyeColor);
            OreDictUnifier.registerOre(stack, OrePrefix.craftingLens, colorMaterial);
        }
    }

    public static void processPlate(OrePrefix platePrefix, Material material, DustProperty property) {
        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_PLATE)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                    .outputs(OreDictUnifier.get(platePrefix, material))
                    .duration(40)
                    .EUt(GTUtility.scaleVoltage(VA[ULV], material.getWorkingTier()))
                    .buildAndRegister();
        }
    }

    public static void processPlateDouble(OrePrefix doublePrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        if (material.hasFlag(GENERATE_PLATE)) {
            if (!material.hasFlag(NO_SMASHING) && workingTier <= HV) {
                ModHandler.addShapedRecipe(String.format("plate_double_%s", material),
                        OreDictUnifier.get(doublePrefix, material),
                        "h", "P", "P", 'P', new UnificationEntry(plate, material));
            }

            BENDER_RECIPES.recipeBuilder()
                    .input(plate, material, 2)
                    .output(doublePrefix, material)
                    .circuitMeta(2)
                    .duration((int) material.getMass() * 2)
                    .EUt(GTUtility.scaleVoltage(96, workingTier))
                    .buildAndRegister();

            BENDER_RECIPES.recipeBuilder()
                    .input(ingot, material, 2)
                    .circuitMeta(2)
                    .output(doublePrefix, material)
                    .duration((int) material.getMass() * 2)
                    .EUt(GTUtility.scaleVoltage(96, workingTier))
                    .buildAndRegister();
        }
    }

    public static void processPlateDense(OrePrefix orePrefix, Material material, DustProperty property) {
        int workingTier = material.getWorkingTier();

        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, material, 9)
                .circuitMeta(9)
                .output(orePrefix, material)
                .duration((int) Math.max(material.getMass() * 9L, 1L))
                .EUt(GTUtility.scaleVoltage(96, workingTier))
                .buildAndRegister();

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.BENDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 9)
                    .circuitMeta(9)
                    .output(orePrefix, material)
                    .duration((int) Math.max(material.getMass() * 9L, 1L))
                    .EUt(GTUtility.scaleVoltage(96, workingTier))
                    .buildAndRegister();
        }
    }

    public static void processRing(OrePrefix ringPrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_RING)
                .outputs(OreDictUnifier.get(ringPrefix, material, 4))
                .duration((int) material.getMass() * 2)
                .EUt(GTUtility.scaleVoltage(6 * getVoltageMultiplier(material), workingTier))
                .buildAndRegister();

        if (!material.hasFlag(NO_SMASHING)) {
            if (workingTier <= HV) {
                ModHandler.addShapedRecipe(String.format("ring_%s", material),
                        OreDictUnifier.get(ringPrefix, material),
                        "h ", " X",
                        'X', new UnificationEntry(OrePrefix.stick, material));
            }
        } else {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_RING)
                    .outputs(OreDictUnifier.get(ringPrefix, material, 4))
                    .duration((int) material.getMass() * 2)
                    .EUt(GTUtility.scaleVoltage(6 * getVoltageMultiplier(material), workingTier))
                    .buildAndRegister();
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_RING)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L / 4))
                    .output(ringPrefix, material)
                    .EUt(VA[ULV]).duration(3)
                    .buildAndRegister();
        }
    }

    public static void processSpringSmall(OrePrefix springPrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        if (workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("spring_small_%s", material.toString()),
                    OreDictUnifier.get(springSmall, material),
                    " s ", "fRx", 'R', new UnificationEntry(stick, material));
        }

        BENDER_RECIPES.recipeBuilder()
                .input(stick, material)
                .output(springSmall, material, 2)
                .circuitMeta(1)
                .duration((int) (material.getMass() / 2))
                .EUt(GTUtility.scaleVoltage(VA[ULV], workingTier))
                .buildAndRegister();
    }

    public static void processSpring(OrePrefix springPrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(stickLong, material)
                .outputs(OreDictUnifier.get(OrePrefix.spring, material))
                .circuitMeta(1)
                .duration(200)
                .EUt(GTUtility.scaleVoltage(16, workingTier))
                .buildAndRegister();

        if (workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("spring_%s", material.toString()),
                    OreDictUnifier.get(spring, material),
                    " s ", "fRx", " R ", 'R', new UnificationEntry(stickLong, material));
        }
    }

    public static void processRotor(OrePrefix rotorPrefix, Material material, IngotProperty property) {
        ItemStack stack = OreDictUnifier.get(rotorPrefix, material);
        int workingTier = material.getWorkingTier();

        if (workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("rotor_%s", material.toString()), stack,
                    "ChC", "SRf", "CdC",
                    'C', new UnificationEntry(plate, material),
                    'S', new UnificationEntry(screw, material),
                    'R', new UnificationEntry(ring, material));
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_ROTOR)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L * 4))
                    .outputs(GTUtility.copy(stack))
                    .duration(120)
                    .EUt(GTUtility.scaleVoltage(20, workingTier))
                    .buildAndRegister();
        }

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                .input(ingot, material, 4)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_ROTOR)
                .outputs(GTUtility.copy(stack))
                .duration((int) material.getMass() * 4)
                .EUt(GTUtility.scaleVoltage(material.getBlastTemperature() >= 2800 ? 256 : 64, workingTier))
                .buildAndRegister();

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(dust, material, 4)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_ROTOR)
                    .outputs(GTUtility.copy(stack))
                    .duration((int) material.getMass() * 4)
                    .EUt(GTUtility.scaleVoltage(material.getBlastTemperature() >= 2800 ? 256 : 64, workingTier))
                    .buildAndRegister();
        }
    }

    public static void processStick(OrePrefix stickPrefix, Material material, DustProperty property) {
        int workingTier = material.getWorkingTier();

        if (material.hasProperty(PropertyKey.GEM) || material.hasProperty(PropertyKey.INGOT)) {
            RecipeBuilder<?> builder = RecipeMaps.LATHE_RECIPES.recipeBuilder()
                    .input(material.hasProperty(PropertyKey.GEM) ? OrePrefix.gem : OrePrefix.ingot, material)
                    .duration((int) Math.max(material.getMass() * 2, 1))
                    .EUt(GTUtility.scaleVoltage(16, workingTier));

            if (ConfigHolder.recipes.harderRods) {
                builder.output(OrePrefix.stick, material);
                builder.output(OrePrefix.dustSmall, material, 2);
            } else {
                builder.output(OrePrefix.stick, material, 2);
            }
            builder.buildAndRegister();
        }

        if (material.hasFlag(GENERATE_BOLT_SCREW)) {
            ItemStack boltStack = OreDictUnifier.get(OrePrefix.bolt, material);
            RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                    .input(stickPrefix, material)
                    .outputs(GTUtility.copy(4, boltStack))
                    .duration((int) Math.max(material.getMass() * 2L, 1L))
                    .EUt(GTUtility.scaleVoltage(4, workingTier))
                    .buildAndRegister();

            if (workingTier <= HV) {
                ModHandler.addShapedRecipe(String.format("bolt_saw_%s", material),
                        GTUtility.copy(2, boltStack),
                        "s ", " X",
                        'X', new UnificationEntry(OrePrefix.stick, material));
            }
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_ROD)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L / 2))
                    .output(stickPrefix, material)
                    .EUt(VA[ULV]).duration(10)
                    .buildAndRegister();
        }
    }

    public static void processLongStick(OrePrefix longStickPrefix, Material material, DustProperty property) {
        ItemStack stack = OreDictUnifier.get(longStickPrefix, material);
        ItemStack stickStack = OreDictUnifier.get(OrePrefix.stick, material);
        int workingTier = material.getWorkingTier();

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .input(longStickPrefix, material)
                .outputs(GTUtility.copy(2, stickStack))
                .duration((int) Math.max(material.getMass(), 1L))
                .EUt(GTUtility.scaleVoltage(4, workingTier))
                .buildAndRegister();

        if (workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("stick_long_%s", material),
                    GTUtility.copy(2, stickStack),
                    "s", "X", 'X', new UnificationEntry(OrePrefix.stickLong, material));

            if (material.hasProperty(PropertyKey.GEM)) {
                ModHandler.addShapedRecipe(String.format("stick_long_gem_flawless_%s", material),
                        stickStack,
                        "sf",
                        "G ",
                        'G', new UnificationEntry(OrePrefix.gemFlawless, material));

                ModHandler.addShapedRecipe(String.format("stick_long_gem_exquisite_%s", material),
                        GTUtility.copy(2, stickStack),
                        "sf", "G ",
                        'G', new UnificationEntry(OrePrefix.gemExquisite, material));

            }

            ModHandler.addShapedRecipe(String.format("stick_long_stick_%s", material), stack,
                    "ShS",
                    'S', new UnificationEntry(OrePrefix.stick, material));
        }

        RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, material, 2)
                .outputs(stack)
                .duration((int) Math.max(material.getMass(), 1L))
                .EUt(GTUtility.scaleVoltage(16, workingTier))
                .buildAndRegister();

        if (material.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_ROD_LONG)
                    .outputs(stack)
                    .duration((int) Math.max(material.getMass(), 1L))
                    .EUt(GTUtility.scaleVoltage(64, workingTier))
                    .buildAndRegister();

            if (material.hasFlag(NO_SMASHING)) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.dust, material)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_ROD_LONG)
                        .outputs(stack)
                        .duration((int) Math.max(material.getMass(), 1L))
                        .EUt(GTUtility.scaleVoltage(64, workingTier))
                        .buildAndRegister();
            }
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_ROD_LONG)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                    .output(longStickPrefix, material)
                    .EUt(VA[ULV]).duration(20)
                    .buildAndRegister();
        }
    }

    public static void processTurbine(OrePrefix toolPrefix, Material material, IngotProperty property) {
        ItemStack rotorStack = MetaItems.TURBINE_ROTOR.getStackForm();
        AbstractMaterialPartBehavior.setPartMaterial(rotorStack, material);
        int workingTier = material.getWorkingTier();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.turbineBlade, material, 8)
                .input(OrePrefix.stickLong, Materials.Magnalium)
                .outputs(rotorStack)
                .duration(200)
                .EUt(400)
                .buildAndRegister();

        boolean hasDoublePlate = OrePrefix.plateDouble.doGenerateItem(material);
        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(hasDoublePlate ? OrePrefix.plateDouble : OrePrefix.plate, material, hasDoublePlate ? 5 : 10)
                .input(OrePrefix.screw, material, 2)
                .outputs(OreDictUnifier.get(toolPrefix, material))
                .duration(20)
                .EUt(GTUtility.scaleVoltage(256, workingTier))
                .buildAndRegister();

        if (hasDoublePlate && workingTier <= HV) {
            ModHandler.addShapedRecipe(String.format("turbine_blade_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "PPP", "SPS", "fPd",
                    'P', new UnificationEntry(OrePrefix.plateDouble, material),
                    'S', new UnificationEntry(OrePrefix.screw, material));
        }
    }

    public static void processRound(OrePrefix roundPrefix, Material material, IngotProperty property) {
        int workingTier = material.getWorkingTier();

        if (!material.hasFlag(NO_SMASHING) && workingTier <= HV) {

            ModHandler.addShapedRecipe(String.format("round_%s", material),
                    OreDictUnifier.get(round, material),
                    "fN", "Nh", 'N', new UnificationEntry(nugget, material));

            ModHandler.addShapedRecipe(String.format("round_from_ingot_%s", material),
                    OreDictUnifier.get(round, material, 4),
                    "fIh", 'I', new UnificationEntry(ingot, material));
        }

        LATHE_RECIPES.recipeBuilder()
                .input(nugget, material)
                .output(round, material)
                .duration(100)
                .EUt(GTUtility.scaleVoltage(VA[ULV], workingTier))
                .buildAndRegister();

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_ROUND)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L / 9))
                    .output(roundPrefix, material)
                    .EUt(VA[ULV]).duration(5)
                    .buildAndRegister();
        }
    }

    private static long getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }
}
