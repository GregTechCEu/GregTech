package gregtech.loaders.recipe.handlers;

import gregtech.api.fluids.GTFluid;
import gregtech.api.recipes.FluidCellInput;
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
import net.minecraftforge.fluids.Fluid;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.util.DyeUtil.determineDyeColor;

public class PartsRecipeHandler {

    private PartsRecipeHandler() {}

    public static void register() {
        OrePrefix.stick.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processStick);
        OrePrefix.plate.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processPlate);

        OrePrefix.turbineBlade.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processTurbine);
        OrePrefix.rotor.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processRotor);
        OrePrefix.bolt.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processBolt);
        OrePrefix.screw.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processScrew);
        OrePrefix.wireFine.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processFineWire);
        OrePrefix.foil.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processFoil);
        OrePrefix.lens.addProcessingHandler(PropertyKey.GEM, PartsRecipeHandler::processLens);

        OrePrefix.gear.addProcessingHandler(PropertyKey.DUST, PartsRecipeHandler::processGear);
        OrePrefix.ring.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processRing);
        OrePrefix.spring.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processSpring);
        OrePrefix.round.addProcessingHandler(PropertyKey.INGOT, PartsRecipeHandler::processRound);
    }

    public static void processBolt(OrePrefix boltPrefix, Material material, DustProperty property) {
        ItemStack boltStack = OreDictUnifier.get(boltPrefix, material);

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .input(OrePrefix.screw, material)
                .outputs(boltStack)
                .duration(20)
                .EUt(24)
                .buildAndRegister();
    }

    public static void processScrew(OrePrefix screwPrefix, Material material, DustProperty property) {
        ItemStack screwStack = OreDictUnifier.get(screwPrefix, material);

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
                .input(OrePrefix.bolt, material)
                .outputs(screwStack)
                .duration((int) Math.max(1, material.getMass() / 8L))
                .EUt(4)
                .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("screw_%s", material),
                screwStack, "fX", "X ",
                'X', new UnificationEntry(OrePrefix.bolt, material));
    }

    public static void processFoil(OrePrefix foilPrefix, Material material, IngotProperty property) {
        if (!material.hasFlag(NO_SMASHING))
            ModHandler.addShapedRecipe(String.format("foil_%s", material),
                    OreDictUnifier.get(foilPrefix, material, 2),
                    "hP ", 'P', new UnificationEntry(plate, material));

        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(plate, material)
                .output(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(1)
                .buildAndRegister();

        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(ingot, material)
                .output(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(10)
                .buildAndRegister();

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(ingot, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_FOIL)
                    .output(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .buildAndRegister();

            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(dust, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_FOIL)
                    .output(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .buildAndRegister();
        }
    }

    public static void processFineWire(OrePrefix fineWirePrefix, Material material, IngotProperty property) {
        if (!OreDictUnifier.get(foil, material).isEmpty())
            ModHandler.addShapelessRecipe(String.format("fine_wire_%s", material.toString()),
                    OreDictUnifier.get(fineWirePrefix, material),
                    'x', new UnificationEntry(OrePrefix.foil, material));

        if (material.hasProperty(PropertyKey.WIRE)) {
            RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
                    .input(OrePrefix.wireGtSingle, material)
                    .circuitMeta(1)
                    .output(fineWirePrefix, material, 4)
                    .duration((int) material.getMass() * 3 / 2)
                    .EUt(VA[ULV])
                    .buildAndRegister();
        }

        RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, material)
                .circuitMeta(3)
                .output(fineWirePrefix, material, 8)
                .duration((int) material.getMass() * 2)
                .EUt(VA[ULV])
                .buildAndRegister();
    }

    public static void processGear(OrePrefix gearPrefix, Material material, DustProperty property) {
        ItemStack stack = OreDictUnifier.get(gearPrefix, material);
        if (gearPrefix == OrePrefix.gear && material.hasProperty(PropertyKey.INGOT)) {
            int voltageMultiplier = getVoltageMultiplier(material);
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.ingot, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR)
                    .outputs(OreDictUnifier.get(gearPrefix, material))
                    .duration((int) material.getMass() * 5)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();

            RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material, 1)
                    .circuitMeta(2)
                    .outputs(OreDictUnifier.get(gearPrefix, material))
                    .duration((int) material.getMass() * 5)
                    .EUt(6 * voltageMultiplier)
                    .buildAndRegister();

            RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR)
                    .outputs(OreDictUnifier.get(gearPrefix, material))
                    .duration((int) material.getMass() * 3)
                    .EUt(8 * voltageMultiplier)
                    .buildAndRegister();

            if (material.hasFlag(NO_SMASHING)) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.dust, material, 1)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_GEAR)
                        .outputs(OreDictUnifier.get(gearPrefix, material))
                        .duration((int) material.getMass() * 5)
                        .EUt(8 * voltageMultiplier)
                        .buildAndRegister();
            }
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_GEAR)
                    .fluidInputs(
                            material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                    .outputs(stack)
                    .duration(100)
                    .EUt(VA[ULV])
                    .buildAndRegister();
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ModHandler.addShapedRecipe(String.format("gear_%s", material), stack,
                    "wP ", "PhP", " Pf",
                    'P', new UnificationEntry(OrePrefix.plate, material));
        }
    }

    public static void processLens(OrePrefix lensPrefix, Material material, GemProperty property) {
        ItemStack stack = OreDictUnifier.get(lensPrefix, material);

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
                .input(plate, material)
                .output(lens, material)
                .chancedOutput(dust, material, 2500, 0)
                .duration(1200).EUt(120).buildAndRegister();

        if (!OreDictUnifier.get(gemExquisite, material).isEmpty()) {
            RecipeMaps.LATHE_RECIPES.recipeBuilder()
                    .input(gemExquisite, material)
                    .output(lens, material)
                    .output(dust, material, 2)
                    .duration(2400).EUt(30).buildAndRegister();
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
                    .EUt(VA[ULV])
                    .buildAndRegister();
        }
    }

    public static void processRing(OrePrefix ringPrefix, Material material, IngotProperty property) {
        if (material.hasFlag(GENERATE_PLATE)) {
            RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_RING)
                    .outputs(OreDictUnifier.get(ringPrefix, material, 4))
                    .duration((int) material.getMass() * 2)
                    .EUt(VA[LV])
                    .buildAndRegister();
        }

        if (!material.hasFlag(NO_SMASHING)) {
            ModHandler.addShapedRecipe(String.format("ring_%s", material),
                    OreDictUnifier.get(ringPrefix, material),
                    "h ", " X",
                    'X', new UnificationEntry(OrePrefix.stick, material));

            RecipeMaps.BENDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.stick, material, 1)
                    .outputs(OreDictUnifier.get(ringPrefix, material))
                    .circuitMeta(2)
                    .duration(120)
                    .EUt(24)
                    .buildAndRegister();
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_RING)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                    .outputs(OreDictUnifier.get(ringPrefix, material, 4))
                    .duration(80)
                    .EUt(VA[ULV])
                    .buildAndRegister();
        }
    }

    public static void processSpring(OrePrefix springPrefix, Material material, IngotProperty property) {
        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(stick, material, 2)
                .outputs(OreDictUnifier.get(OrePrefix.spring, material))
                .circuitMeta(1)
                .duration(200)
                .EUt(16)
                .buildAndRegister();

        ModHandler.addShapedRecipe(String.format("spring_%s", material.toString()),
                OreDictUnifier.get(spring, material),
                " s ", "fRx", " R ", 'R', new UnificationEntry(stick, material));
    }

    public static void processRotor(OrePrefix rotorPrefix, Material material, IngotProperty property) {
        ItemStack stack = OreDictUnifier.get(rotorPrefix, material);
        ModHandler.addShapedRecipe(String.format("rotor_%s", material.toString()), stack,
                "ChC", " R ", "CfC",
                'C', new UnificationEntry(plate, material),
                'R', new UnificationEntry(ring, material));

        if (material.hasFlag(GENERATE_PLATE)) {
            RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_ROTOR)
                    .outputs(OreDictUnifier.get(rotorPrefix, material))
                    .duration((int) material.getMass() * 2)
                    .EUt(VA[LV])
                    .buildAndRegister();

            RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material, 1)
                    .circuitMeta(1)
                    .outputs(OreDictUnifier.get(rotorPrefix, material))
                    .duration((int) material.getMass() * 3)
                    .EUt(24)
                    .buildAndRegister();
        }

        if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
            RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .notConsumable(MetaItems.SHAPE_MOLD_ROTOR)
                    .fluidInputs(material.getProperty(PropertyKey.FLUID).solidifiesFrom(L))
                    .outputs(GTUtility.copy(stack))
                    .duration(120)
                    .EUt(20)
                    .buildAndRegister();
        }

        if (material.hasFlag(NO_SMASHING)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(dust, material)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_ROTOR)
                    .outputs(GTUtility.copy(stack))
                    .duration((int) material.getMass() * 4)
                    .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                    .buildAndRegister();
        }
    }

    public static void processStick(OrePrefix stickPrefix, Material material, DustProperty property) {
        if (material.hasProperty(PropertyKey.GEM) || material.hasProperty(PropertyKey.INGOT)) {
            int voltageMultiplier = getVoltageMultiplier(material);

            RecipeBuilder<?> builder = RecipeMaps.LATHE_RECIPES.recipeBuilder()
                    .input(material.hasProperty(PropertyKey.GEM) ? OrePrefix.gem : OrePrefix.ingot, material)
                    .duration((int) Math.max(material.getMass() * 2, 1))
                    .EUt(16);

            if (ConfigHolder.recipes.harderRods) {
                builder.output(OrePrefix.stick, material);
                builder.chancedOutput(OrePrefix.dust, material, 2500, 0);
            } else {
                builder.output(OrePrefix.stick, material, 2);
            }
            builder.buildAndRegister();

            if (material.hasProperty(PropertyKey.INGOT)) {
                RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                        .input(OrePrefix.ingot, material, 1)
                        .notConsumable(MetaItems.SHAPE_EXTRUDER_ROD)
                        .outputs(OreDictUnifier.get(stickPrefix, material, 2))
                        .duration((int) Math.max(material.getMass() * 2, 1))
                        .EUt(8 * voltageMultiplier)
                        .buildAndRegister();
            }
        }

        if (material.hasFlag(GENERATE_BOLT_SCREW)) {
            ItemStack boltStack = OreDictUnifier.get(OrePrefix.bolt, material);

            RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                    .input(stickPrefix, material)
                    .outputs(GTUtility.copy(4, boltStack))
                    .duration((int) Math.max(material.getMass() * 2L, 1L))
                    .EUt(4)
                    .buildAndRegister();

            ModHandler.addShapedRecipe(String.format("bolt_saw_%s", material),
                    GTUtility.copy(2, boltStack),
                    "s ", " X",
                    'X', new UnificationEntry(OrePrefix.stick, material));
        }
    }

    public static void processTurbine(OrePrefix toolPrefix, Material material, IngotProperty property) {
        ItemStack rotorStack = MetaItems.TURBINE_ROTOR.getStackForm();
        AbstractMaterialPartBehavior.setPartMaterial(rotorStack, material);

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.turbineBlade, material, 8)
                .input(OrePrefix.stick, Materials.Magnalium)
                .outputs(rotorStack)
                .duration(200)
                .EUt(400)
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, material, 10)
                .input(OrePrefix.screw, material, 2)
                .outputs(OreDictUnifier.get(toolPrefix, material))
                .duration(20)
                .EUt(256)
                .buildAndRegister();
    }

    public static void processRound(OrePrefix roundPrefix, Material material, IngotProperty property) {
        if (!material.hasFlag(NO_SMASHING)) {

            ModHandler.addShapedRecipe(String.format("round_%s", material),
                    OreDictUnifier.get(round, material),
                    "fN", "Nh", 'N', new UnificationEntry(nugget, material));

            ModHandler.addShapedRecipe(String.format("round_from_ingot_%s", material),
                    OreDictUnifier.get(round, material, 4),
                    "fIh", 'I', new UnificationEntry(ingot, material));
        }

        RecipeMaps.LATHE_RECIPES.recipeBuilder().EUt(VA[ULV]).duration(100)
                .input(nugget, material)
                .output(round, material)
                .buildAndRegister();
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }
}
