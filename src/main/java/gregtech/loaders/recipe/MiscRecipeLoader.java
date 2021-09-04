package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockTransparentCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.L;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.MarkerMaterials.Tier.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class MiscRecipeLoader {

    public static void init() {

        // Potin Recipe
        ModHandler.addShapelessRecipe("potin_dust", OreDictUnifier.get(dust, Potin, 5),
                new UnificationEntry(dust, Lead),
                new UnificationEntry(dust, Lead),
                new UnificationEntry(dust, Bronze),
                new UnificationEntry(dust, Bronze),
                new UnificationEntry(dust, Tin));

        // Mixed Metal Ingots
        final MaterialStack[] firstMetal = {
                new MaterialStack(Materials.Iron, 1),
                new MaterialStack(Materials.Nickel, 1),
                new MaterialStack(Materials.Invar, 2),
                new MaterialStack(Materials.Steel, 2),
                new MaterialStack(Materials.StainlessSteel, 3),
                new MaterialStack(Materials.Titanium, 3),
                new MaterialStack(Materials.Tungsten, 4),
                new MaterialStack(Materials.TungstenSteel, 5)
        };

        final MaterialStack[] lastMetal = {
                new MaterialStack(Materials.Tin, 0),
                new MaterialStack(Materials.Zinc, 0),
                new MaterialStack(Materials.Aluminium, 1)
        };

        int multiplier;
        for (MaterialStack metal1 : firstMetal) {
            Material material1 = metal1.material;
            int multiplier1 = (int) metal1.amount;
            for (MaterialStack metal2 : lastMetal) {
                Material material2 = metal2.material;
                if ((int) metal1.amount == 1) multiplier = 0;
                else multiplier = (int) metal2.amount;
                ModHandler.addShapedRecipe("mixed_metal_1_" + material1.toString() + "_" + material2.toString(), MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier),
                        "F", "M", "L",
                        'F', new UnificationEntry(OrePrefix.plate, material1),
                        'M', new UnificationEntry(OrePrefix.plate, Bronze),
                        'L', new UnificationEntry(OrePrefix.plate, material2));

                ModHandler.addShapedRecipe("mixed_metal_2_" + material1.toString() + "_" + material2.toString(), MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier),
                        "F", "M", "L",
                        'F', new UnificationEntry(OrePrefix.plate, material1),
                        'M', new UnificationEntry(OrePrefix.plate, Brass),
                        'L', new UnificationEntry(OrePrefix.plate, material2));

                FORMING_PRESS_RECIPES.recipeBuilder().duration(40 * multiplier1 + multiplier * 40).EUt(8)
                        .input(OrePrefix.plate, material1)
                        .input(OrePrefix.plank, Bronze)
                        .input(OrePrefix.plate, material2)
                        .outputs(MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier))
                        .buildAndRegister();

                FORMING_PRESS_RECIPES.recipeBuilder().duration(40 * multiplier1 + multiplier * 40).EUt(8)
                        .input(OrePrefix.plate, material1)
                        .input(OrePrefix.plate, Brass)
                        .input(OrePrefix.plate, material2)
                        .outputs(MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier))
                        .buildAndRegister();
            }
        }

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(8)
                .input(dust, Sugar)
                .inputs(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(8)
                .input(dust, Sugar)
                .inputs(new ItemStack(Blocks.RED_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        RecipeMaps.SIFTER_RECIPES.recipeBuilder().duration(800).EUt(16)
                .inputs(new ItemStack(Blocks.GRAVEL))
                .outputs(new ItemStack(Items.FLINT))
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(TOOL_MATCHES.getStackForm(16))
                .input(OrePrefix.plate, Materials.Paper)
                .outputs(TOOL_MATCHBOX.getStackForm())
                .duration(64)
                .EUt(16)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.COBBLESTONE))
                .duration(16)
                .EUt(30)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 1, 0))
                .outputs(new ItemStack(Blocks.STONE, 1, 0))
                .duration(16)
                .EUt(30)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 1, 1))
                .outputs(new ItemStack(Blocks.STONE, 1, 1))
                .duration(16)
                .EUt(30)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 1, 3))
                .outputs(new ItemStack(Blocks.STONE, 1, 3))
                .duration(16)
                .EUt(30)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 1, 5))
                .outputs(new ItemStack(Blocks.STONE, 1, 5))
                .duration(16)
                .EUt(30)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .input(dust, Redstone)
                .outputs(new ItemStack(Blocks.OBSIDIAN, 1))
                .duration(128)
                .EUt(30)
                .buildAndRegister();

        //armor
        // Nightvision Goggles
        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(128)
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .inputs(EMITTER_MV.getStackForm(2))
                .inputs(DUCT_TAPE.getStackForm(2))
                .inputs(BATTERY_HV_LITHIUM.getStackForm())
                .outputs(NIGHTVISION_GOGGLES.getStackForm())
                .circuitMeta(3)
                .buildAndRegister();

        // NanoMuscle Suite
        ASSEMBLER_RECIPES.recipeBuilder().duration(1200).EUt(512)
                .input(circuit, Advanced)
                .inputs(CARBON_PLATE.getStackForm(7))
                .inputs(BATTERY_HV_LITHIUM.getStackForm())
                .circuitMeta(0)
                .outputs(NANO_MUSCLE_SUITE_CHESTPLATE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1200).EUt(512)
                .input(circuit, Advanced)
                .inputs(CARBON_PLATE.getStackForm(6))
                .inputs(BATTERY_HV_LITHIUM.getStackForm())
                .circuitMeta(1)
                .outputs(NANO_MUSCLE_SUITE_LEGGINGS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1200).EUt(512)
                .input(circuit, Advanced)
                .inputs(CARBON_PLATE.getStackForm(4))
                .inputs(BATTERY_HV_LITHIUM.getStackForm())
                .circuitMeta(2)
                .outputs(NANO_MUSCLE_SUITE_BOOTS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1200).EUt(512)
                .input(circuit, Advanced, 2)
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .inputs(NIGHTVISION_GOGGLES.getStackForm())
                .inputs(CARBON_PLATE.getStackForm(5))
                .inputs(BATTERY_HV_LITHIUM.getStackForm())
                .circuitMeta(3)
                .outputs(NANO_MUSCLE_SUITE_HELMET.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(1024)
                .input(circuit, Advanced, 2)
                .inputs(NANO_MUSCLE_SUITE_CHESTPLATE.getStackForm())
                .inputs(ADVANCED_IMPELLER_JETPACK.getStackForm())
                .inputs(DUCT_TAPE.getStackForm(2))
                .inputs(POWER_INTEGRATED_CIRCUIT.getStackForm(4))
                .outputs(ADVANCED_NANO_MUSCLE_CHESTPLATE.getStackForm())
                .buildAndRegister();
        // Jetpacks
        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(100)
                .input(circuit, Good, 6)
                .inputs(MetaTileEntities.STEEL_TANK.getStackForm())
                .inputs(ELECTRIC_PUMP_MV.getStackForm(2))
                .input(pipeSmallFluid, Polyethylene, 2)
                .input(pipeNormalFluid, Steel, 2)
                .input(plate, Aluminium)
                .input(screw, Aluminium, 4)
                .input(stick, Aluminium, 2)
                .outputs(SEMIFLUID_JETPACK.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(100)
                .input(circuit, Good, 6)
                .inputs(BATTERY_MV_CADMIUM.getStackForm(6))
                .inputs(IMPELLER_MV.getStackForm(4))
                .input(plate, Aluminium)
                .input(screw, Aluminium, 4)
                .input(stick, Aluminium, 2)
                .outputs(IMPELLER_JETPACK.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(60)
                .input(cableGtSingle, Copper)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                .input(stick, Steel)
                .input(rotor, Polyethylene, 2)
                .input(pipeNormalFluid, Polyethylene)
                .outputs(IMPELLER_MV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(60)
                .input(cableGtSingle, Gold)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                .input(stick, StainlessSteel)
                .input(rotor, Polyethylene, 2)
                .input(pipeNormalFluid, Polyethylene)
                .outputs(IMPELLER_HV.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(800).EUt(400)
                .input(circuit, Good, 4)
                .input(circuit, Advanced)
                .inputs(BATPACK_HV.getStackForm())
                .inputs(IMPELLER_HV.getStackForm(6))
                .inputs(BATTERY_HV_CADMIUM.getStackForm())
                .input(plate, Aluminium)
                .input(screw, Aluminium, 4)
                .input(stick, Aluminium, 2)
                .outputs(ADVANCED_IMPELLER_JETPACK.getStackForm())
                .buildAndRegister();

        // Battery Packs
        ModHandler.addShapedRecipe("battery_pack.lv", BATPACK_LV.getStackForm(),
                "BPB", "BCB", "B B",
                'B', BATTERY_LV_LITHIUM,
                'C', new UnificationEntry(circuit, Basic),
                'P', new UnificationEntry(plate, Steel));

        ModHandler.addShapedRecipe("battery_pack.mv", BATPACK_MV.getStackForm(),
                "BPB", "BCB", "B B",
                'B', BATTERY_MV_LITHIUM,
                'C', new UnificationEntry(circuit, Good),
                'P', new UnificationEntry(plate, Aluminium));

        ModHandler.addShapedRecipe("battery_pack.hv", BATPACK_HV.getStackForm(),
                "BPB", "BCB", "B B",
                'B', BATTERY_HV_LITHIUM,
                'C', new UnificationEntry(circuit, Advanced),
                'P', new UnificationEntry(plate, StainlessSteel));

        // QuarkTech Suite
        ASSEMBLER_RECIPES.recipeBuilder().duration(2400).EUt(1600)
                .input(circuit, Extreme, 2)
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(PLATE_IRIDIUM_ALLOY.getStackForm(4))
                .inputs(ELECTRIC_PISTON_EV.getStackForm(2))
                .inputs(NANO_MUSCLE_SUITE_BOOTS.getStackForm())
                .outputs(QUARK_TECH_SUITE_BOOTS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(2400).EUt(1600)
                .input(circuit, Extreme, 4)
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(PLATE_IRIDIUM_ALLOY.getStackForm(6))
                .inputs(CONVEYOR_MODULE_EV.getStackForm(2))
                .inputs(NANO_MUSCLE_SUITE_LEGGINGS.getStackForm())
                .outputs(QUARK_TECH_SUITE_LEGGINGS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(2400).EUt(1600)
                .input(circuit, Extreme, 4)
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(PLATE_IRIDIUM_ALLOY.getStackForm(8))
                .inputs(FIELD_GENERATOR_EV.getStackForm(2))
                .inputs(NANO_MUSCLE_SUITE_CHESTPLATE.getStackForm())
                .outputs(QUARK_TECH_SUITE_CHESTPLATE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(2400).EUt(1600)
                .input(circuit, Extreme, 2)
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(LAPOTRON_CRYSTAL.getStackForm())
                .inputs(PLATE_IRIDIUM_ALLOY.getStackForm(4))
                .inputs(SENSOR_EV.getStackForm())
                .inputs(EMITTER_EV.getStackForm())
                .inputs(NANO_MUSCLE_SUITE_HELMET.getStackForm())
                .outputs(QUARK_TECH_SUITE_HELMET.getStackForm())
                .buildAndRegister();
//TODO add superconductors when added
        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(1800).EUt(7100)
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .inputs(FIELD_GENERATOR_EV.getStackForm(2))
                .input(circuit, Master, 4)
                // .input(wireGtSingle, IVSuperconductor, 4)
                .inputs(POWER_INTEGRATED_CIRCUIT.getStackForm(4))
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .outputs(GRAVITATION_ENGINE.getStackForm())
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(3600).EUt(8192)
                .inputs(HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(16))
                // .input(wireGtSingle, IVSuperconductor, 8)
                .inputs(GRAVITATION_ENGINE.getStackForm(2))
                .inputs(PLATE_IRIDIUM_ALLOY.getStackForm(12))
                .input(circuit, Elite, 4)
                .inputs(QUARK_TECH_SUITE_CHESTPLATE.getStackForm())
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .outputs(ADVANCED_QUARK_TECH_SUITE_CHESTPLATE.getStackForm())
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(3600).EUt(8192)
                .inputs(HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(8))
                // .input(wireGtSingle, IVSuperconductor, 8)
                .inputs(GRAVITATION_ENGINE.getStackForm(2))
                .inputs(PLATE_IRIDIUM_ALLOY.getStackForm(16))
                .input(circuit, Elite, 2)
                .inputs(ADVANCED_NANO_MUSCLE_CHESTPLATE.getStackForm())
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .outputs(ADVANCED_QUARK_TECH_SUITE_CHESTPLATE.getStackForm())
                .buildAndRegister();
        ModHandler.addShapelessRecipe("fluid_jetpack_clear", SEMIFLUID_JETPACK.getStackForm(), SEMIFLUID_JETPACK.getStackForm());

    }
}
