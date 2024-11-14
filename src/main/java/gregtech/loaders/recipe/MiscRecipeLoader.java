package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.category.RecipeCategories;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class MiscRecipeLoader {

    public static void init() {
        // Basic Terminal Recipe
        ModHandler.addShapedRecipe(true, "basic_terminal", TERMINAL.getStackForm(),
                "SGS", "PBP", "PWP", 'S', new UnificationEntry(screw, WroughtIron), 'G',
                OreDictUnifier.get("paneGlass"), 'B', new ItemStack(Items.BOOK),
                'P', new UnificationEntry(plate, WroughtIron), 'W', new UnificationEntry(wireGtSingle, RedAlloy));

        // Multiblock Builder
        ModHandler.addShapedRecipe(true, "multiblock_builder", MULTIBLOCK_BUILDER.getStackForm(),
                "wCE", "SRC", "RSd",
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'E', new UnificationEntry(gem, EnderEye),
                'S', new UnificationEntry(screw, StainlessSteel),
                'R', new UnificationEntry(stick, StainlessSteel));

        // Potin Recipe
        ModHandler.addShapelessRecipe("potin_dust", OreDictUnifier.get(dust, Potin, 8),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Tin),
                new UnificationEntry(dust, Tin),
                new UnificationEntry(dust, Lead));

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Sugar)
                .inputs(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Sugar)
                .inputs(new ItemStack(Blocks.RED_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        SIFTER_RECIPES.recipeBuilder().duration(100).volts(16)
                .inputs(new ItemStack(Blocks.GRAVEL))
                .outputItem(gem, Flint)
                .chancedOutput(gem, Flint, 9000, 0)
                .chancedOutput(gem, Flint, 8000, 0)
                .chancedOutput(gem, Flint, 6000, 0)
                .chancedOutput(gem, Flint, 3300, 0)
                .chancedOutput(gem, Flint, 2500, 0)
                .buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(TOOL_MATCHES.getStackForm(16))
                .inputItem(plate, Paper)
                .outputs(TOOL_MATCHBOX.getStackForm())
                .duration(64).volts(16)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.COBBLESTONE))
                .duration(16).volts(VA[ULV])
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 1, 0))
                .outputs(new ItemStack(Blocks.STONE, 1, 0))
                .duration(16).volts(VA[ULV])
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Andesite)
                .outputItem(stone, Andesite)
                .duration(16).volts(60)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Granite)
                .outputItem(stone, Granite)
                .duration(16).volts(60)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Diorite)
                .outputItem(stone, Diorite)
                .duration(16).volts(60)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(dust, Redstone)
                .outputs(new ItemStack(Blocks.OBSIDIAN, 1))
                .duration(16).volts(240)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Marble)
                .outputItem(stone, Marble)
                .duration(16).volts(240)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Basalt)
                .outputItem(stone, Basalt)
                .duration(16).volts(240)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, GraniteRed)
                .outputItem(stone, GraniteRed)
                .duration(16).volts(960)
                .buildAndRegister();

        ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, GraniteBlack)
                .outputItem(stone, GraniteBlack)
                .duration(16).volts(960)
                .buildAndRegister();

        // Jetpacks
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).volts(30)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                .inputItem(ring, Aluminium, 2)
                .inputItem(stick, Aluminium)
                .inputItem(rotor, Steel)
                .inputItem(cableGtSingle, Copper, 2)
                .outputs(POWER_THRUSTER.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).volts(30)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                .inputItem(ring, StainlessSteel, 2)
                .inputItem(stick, StainlessSteel)
                .inputItem(rotor, Chrome)
                .inputItem(cableGtSingle, Gold, 2)
                .outputs(POWER_THRUSTER_ADVANCED.getStackForm())
                .buildAndRegister();

        // QuarkTech Suite
        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).volts(GTValues.VA[GTValues.IV])
                .inputItem(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItem(wireGtQuadruple, Tungsten, 5)
                .inputItem(ENERGY_LAPOTRONIC_ORB)
                .inputs(SENSOR_IV.getStackForm())
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .inputItem(screw, TungstenSteel, 4)
                .inputItem(plate, Iridium, 5)
                .inputItem(foil, Ruthenium, 20)
                .inputItem(wireFine, Rhodium, 32)
                .fluidInputs(Titanium.getFluid(L * 10))
                .outputs(QUANTUM_HELMET.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).volts(GTValues.VA[GTValues.IV])
                .inputItem(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItem(wireGtQuadruple, Tungsten, 8)
                .inputItem(ENERGY_LAPOTRONIC_ORB)
                .inputs(EMITTER_IV.getStackForm(2))
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .inputItem(screw, TungstenSteel, 4)
                .inputItem(plate, Iridium, 8)
                .inputItem(foil, Ruthenium, 32)
                .inputItem(wireFine, Rhodium, 48)
                .fluidInputs(Titanium.getFluid(L * 16))
                .outputs(QUANTUM_CHESTPLATE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).volts(GTValues.VA[GTValues.IV])
                .inputItem(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItem(wireGtQuadruple, Tungsten, 7)
                .inputItem(ENERGY_LAPOTRONIC_ORB)
                .inputs(ELECTRIC_MOTOR_IV.getStackForm(4))
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .inputItem(screw, TungstenSteel, 4)
                .inputItem(plate, Iridium, 7)
                .inputItem(foil, Ruthenium, 28)
                .inputItem(wireFine, Rhodium, 40)
                .fluidInputs(Titanium.getFluid(L * 14))
                .outputs(QUANTUM_LEGGINGS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).volts(GTValues.VA[GTValues.IV])
                .inputItem(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItem(wireGtQuadruple, Tungsten, 4)
                .inputItem(ENERGY_LAPOTRONIC_ORB)
                .inputs(ELECTRIC_PISTON_IV.getStackForm(2))
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .inputItem(screw, TungstenSteel, 4)
                .inputItem(plate, Iridium, 4)
                .inputItem(foil, Ruthenium, 16)
                .inputItem(wireFine, Rhodium, 16)
                .fluidInputs(Titanium.getFluid(L * 8))
                .outputs(QUANTUM_BOOTS.getStackForm())
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(1000).volts(GTValues.VA[GTValues.LuV])
                .inputItem(((ArmorMetaItem<?>) QUANTUM_CHESTPLATE.getStackForm().getItem())
                        .getItem(QUANTUM_CHESTPLATE.getStackForm()))
                .inputs(HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                .inputItem(wireFine, NiobiumTitanium, 64)
                .inputItem(wireGtQuadruple, Osmium, 6)
                .inputItem(plateDouble, Iridium, 4)
                .inputs(GRAVITATION_ENGINE.getStackForm(2))
                .inputItem(circuit, MarkerMaterials.Tier.ZPM)
                .inputItem(plateDense, RhodiumPlatedPalladium, 2)
                .inputItem(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .inputs(FIELD_GENERATOR_LuV.getStackForm(2))
                .inputs(ELECTRIC_MOTOR_LuV.getStackForm(2))
                .inputItem(screw, HSSS, 8)
                .outputs(QUANTUM_CHESTPLATE_ADVANCED.getStackForm())
                .scannerResearch(GRAVITATION_ENGINE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).volts(VA[HV])
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputs((ItemStack) CraftingComponent.HULL.getIngredient(1))
                .inputItem(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(MetaTileEntities.MONITOR_SCREEN.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[HV])
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputs((ItemStack) CraftingComponent.HULL.getIngredient(3))
                .inputItem(circuit, MarkerMaterials.Tier.HV, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(MetaTileEntities.CENTRAL_MONITOR.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[HV])
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputItem(plate, Aluminium)
                .inputItem(circuit, MarkerMaterials.Tier.MV)
                .inputItem(screw, StainlessSteel, 4)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(COVER_DIGITAL_INTERFACE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[HV])
                .inputs(COVER_DIGITAL_INTERFACE.getStackForm())
                .inputs(WIRELESS.getStackForm())
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(COVER_DIGITAL_INTERFACE_WIRELESS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).volts(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputItem(circuit, MarkerMaterials.Tier.LV)
                .inputItem(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_TEXT.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).volts(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputItem(circuit, MarkerMaterials.Tier.LV)
                .inputItem(wireFine, Silver, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_ONLINE_PIC.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).volts(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputItem(circuit, MarkerMaterials.Tier.LV)
                .inputItem(wireFine, Gold, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_FAKE_GUI.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).volts(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputItem(circuit, MarkerMaterials.Tier.HV)
                .inputItem(wireFine, Aluminium, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_ADVANCED_MONITOR.getStackForm())
                .buildAndRegister();

        // terminal
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[MV])
                .inputItem(circuit, MarkerMaterials.Tier.MV, 4)
                .inputItem(EMITTER_MV, 2)
                .inputItem(SENSOR_MV, 2)
                .inputItem(plate, StainlessSteel)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(WIRELESS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[LV])
                .inputItem(ELECTRIC_PISTON_LV, 2)
                .inputItem(EMITTER_LV)
                .inputItem(lens, Glass)
                .inputItem(lens, Diamond)
                .inputItem(circuit, MarkerMaterials.Tier.LV, 4)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .outputs(CAMERA.getStackForm())
                .buildAndRegister();

        // Tempered Glass in Arc Furnace
        ARC_FURNACE_RECIPES.recipeBuilder().duration(60).volts(VA[LV])
                .inputItem(block, Glass)
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS))
                .buildAndRegister();

        // Dyed Lens Decomposition
        for (MetaValueItem item : GLASS_LENSES.values()) {
            EXTRACTOR_RECIPES.recipeBuilder().volts(VA[LV]).duration(15)
                    .inputItem(item)
                    .fluidOutputs(Glass.getFluid(108))
                    .category(RecipeCategories.EXTRACTOR_RECYCLING)
                    .buildAndRegister();

            MACERATOR_RECIPES.recipeBuilder().duration(15)
                    .inputItem(item)
                    .outputItem(dustSmall, Glass, 3)
                    .category(RecipeCategories.MACERATOR_RECYCLING)
                    .buildAndRegister();
        }

        // Glass Fluid Extraction
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GLASS))
                .fluidOutputs(Glass.getFluid(L))
                .duration(20).volts(30).buildAndRegister();

        // Glass Plate in Alloy Smelter
        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .inputItem(dust, Glass, 2)
                .notConsumable(SHAPE_MOLD_PLATE.getStackForm())
                .outputItem(plate, Glass)
                .duration(40).volts(6).buildAndRegister();

        // Dyed Lens Recipes
        RecipeBuilder<?> builder = CHEMICAL_BATH_RECIPES.recipeBuilder().volts(VA[HV]).duration(200).inputItem(
                craftingLens,
                Glass);
        final int dyeAmount = 288;

        builder.copy().fluidInputs(DyeWhite.getFluid(dyeAmount)).outputItem(lens, Glass).buildAndRegister();
        builder.copy().fluidInputs(DyeOrange.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Orange))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeMagenta.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Magenta))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeLightBlue.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.LightBlue))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeYellow.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Yellow))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeLime.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Lime))
                .buildAndRegister();
        builder.copy().fluidInputs(DyePink.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Pink))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeGray.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Gray))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeLightGray.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.LightGray))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeCyan.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Cyan))
                .buildAndRegister();
        builder.copy().fluidInputs(DyePurple.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Purple))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeBlue.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Blue))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeBrown.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Brown))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeGreen.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Green))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeRed.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Red))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeBlack.getFluid(dyeAmount)).outputItem(GLASS_LENSES.get(Color.Black))
                .buildAndRegister();

        // NAN Certificate
        EXTRUDER_RECIPES.recipeBuilder()
                .inputItem(block, Neutronium, 64)
                .inputItem(block, Neutronium, 64)
                .outputItem(NAN_CERTIFICATE)
                .duration(Integer.MAX_VALUE).volts(VA[ULV]).buildAndRegister();

        // Fertilizer
        MIXER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.DIRT))
                .inputItem(dust, Wood, 2)
                .inputs(new ItemStack(Blocks.SAND, 4))
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 4)
                .duration(100).volts(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcite).inputItem(dust, Sulfur)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 2).duration(200).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcite).inputItem(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 3).duration(300).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcite).inputItem(dust, Phosphate)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 2).duration(200).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcite).inputItem(dust, Ash, 3)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 1).duration(100).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcite).inputItem(dust, DarkAsh)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 1).duration(100).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcium).inputItem(dust, Sulfur)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 3).duration(300).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcium).inputItem(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 4).duration(400).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcium).inputItem(dust, Phosphate)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 3).duration(300).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcium).inputItem(dust, Ash, 3)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 2).duration(200).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Calcium).inputItem(dust, DarkAsh)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 2).duration(200).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Apatite).inputItem(dust, Sulfur)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 3).duration(300).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Apatite).inputItem(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 4).duration(400).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Apatite).inputItem(dust, Phosphate)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 3).duration(300).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Apatite).inputItem(dust, Ash, 3)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 2).duration(200).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, Apatite).inputItem(dust, DarkAsh)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(FERTILIZER, 2).duration(200).volts(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, GlauconiteSand).inputItem(dust, Sulfur)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 3).duration(300).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, GlauconiteSand).inputItem(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 4).duration(400).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, GlauconiteSand).inputItem(dust, Phosphate)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 3).duration(300).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, GlauconiteSand).inputItem(dust, Ash, 3)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 2).duration(200).volts(VA[LV])
                .buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().inputItem(dust, GlauconiteSand).inputItem(dust, DarkAsh)
                .fluidInputs(Water.getFluid(1000)).outputItem(FERTILIZER, 2).duration(200).volts(VA[LV])
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(FERTILIZER)
                .outputItem(dust, Calcite)
                .outputItem(dust, Carbon)
                .fluidOutputs(Water.getFluid(1000))
                .duration(100).volts(VA[LV]).buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS, 2))
                .inputItem(plate, PolyvinylButyral)
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .duration(200).volts(VA[HV]).buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputItem(plank, TreatedWood)
                .outputItem(stick, TreatedWood, 2)
                .duration(10).volts(VA[ULV])
                .buildAndRegister();

        // Coke Brick and Firebrick decomposition
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.COKE_BRICKS))
                .outputItem(COKE_OVEN_BRICK, 4)
                .duration(300).volts(2)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                .outputItem(FIRECLAY_BRICK, 4)
                .duration(300).volts(2)
                .buildAndRegister();

        // Minecart wheels
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iron)
                .inputItem(ring, Iron, 2)
                .outputItem(IRON_MINECART_WHEELS)
                .duration(100).volts(20).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Steel)
                .inputItem(ring, Steel, 2)
                .outputItem(STEEL_MINECART_WHEELS)
                .duration(60).volts(20).buildAndRegister();
    }
}
