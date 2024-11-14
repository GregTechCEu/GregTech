package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.Mods;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.*;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.loaders.recipe.chemistry.AssemblerRecipeLoader;
import gregtech.loaders.recipe.chemistry.ChemistryRecipes;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.BRONZE_BRICKS;
import static gregtech.common.blocks.MetaBlocks.METAL_CASING;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gregtech.loaders.OreDictionaryLoader.OREDICT_BLOCK_FUEL_COKE;
import static gregtech.loaders.OreDictionaryLoader.OREDICT_FUEL_COKE;

public class MachineRecipeLoader {

    private MachineRecipeLoader() {}

    public static void init() {
        ChemistryRecipes.init();
        FuelRecipes.registerFuels();
        AssemblyLineLoader.init();
        FusionLoader.init();
        AssemblerRecipeLoader.init();
        ComponentRecipes.register();
        MiscRecipeLoader.init();
        BatteryRecipes.init();
        CircuitRecipes.init();
        ComputerRecipes.init();
        DecorationRecipes.init();
        WoodRecipeLoader.registerRecipes();

        registerDecompositionRecipes();
        registerBlastFurnaceRecipes();
        registerAssemblerRecipes();
        registerAlloyRecipes();
        registerBendingCompressingRecipes();
        registerCokeOvenRecipes();
        registerFluidRecipes();
        registerMixingCrystallizationRecipes();
        registerPrimitiveBlastFurnaceRecipes();
        registerRecyclingRecipes();
        registerStoneBricksRecipes();
        registerNBTRemoval();
        ConvertHatchToHatch();
    }

    private static void registerBendingCompressingRecipes() {
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, Fireclay)
                .outputs(COMPRESSED_FIRECLAY.getStackForm())
                .duration(80).volts(4)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .duration(100).volts(16)
                .notConsumable(MetaItems.SHAPE_MOLD_CREDIT.getStackForm())
                .inputItem(OrePrefix.plate, Materials.Cupronickel, 1)
                .outputs(MetaItems.CREDIT_CUPRONICKEL.getStackForm(4))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .duration(100).volts(16)
                .notConsumable(MetaItems.SHAPE_MOLD_CREDIT.getStackForm())
                .inputItem(OrePrefix.plate, Materials.Brass, 1)
                .outputs(MetaItems.COIN_DOGE.getStackForm(4))
                .buildAndRegister();

        for (MetaItem<?>.MetaValueItem shapeMold : SHAPE_MOLDS) {
            FORMING_PRESS_RECIPES.recipeBuilder()
                    .duration(120).volts(22)
                    .notConsumable(shapeMold.getStackForm())
                    .inputs(MetaItems.SHAPE_EMPTY.getStackForm())
                    .outputs(shapeMold.getStackForm())
                    .buildAndRegister();
        }

        for (MetaItem<?>.MetaValueItem shapeExtruder : SHAPE_EXTRUDERS) {
            if (shapeExtruder == null) continue;
            FORMING_PRESS_RECIPES.recipeBuilder()
                    .duration(120).volts(22)
                    .notConsumable(shapeExtruder.getStackForm())
                    .inputs(MetaItems.SHAPE_EMPTY.getStackForm())
                    .outputs(shapeExtruder.getStackForm())
                    .buildAndRegister();
        }

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(4)
                .inputItem(plate, Steel, 4)
                .outputs(SHAPE_EMPTY.getStackForm())
                .duration(180).volts(12)
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .inputItem(plate, Tin, 2)
                .outputs(FLUID_CELL.getStackForm())
                .duration(200).volts(VA[ULV])
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .inputItem(plate, Steel)
                .outputs(FLUID_CELL.getStackForm())
                .duration(100).volts(VA[ULV])
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .inputItem(plate, Polytetrafluoroethylene)
                .outputs(FLUID_CELL.getStackForm(4))
                .duration(100).volts(VA[ULV])
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .inputItem(plate, Polybenzimidazole)
                .outputs(FLUID_CELL.getStackForm(16))
                .duration(100).volts(VA[ULV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .inputItem(ingot, Tin, 2)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputs(FLUID_CELL.getStackForm())
                .duration(128).volts(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .inputItem(ingot, Steel)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputs(FLUID_CELL.getStackForm())
                .duration(128).volts(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .inputItem(ingot, Polytetrafluoroethylene)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputs(FLUID_CELL.getStackForm(4))
                .duration(128).volts(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .inputItem(ingot, Polybenzimidazole)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputs(FLUID_CELL.getStackForm(16))
                .duration(128).volts(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .inputItem(dust, Glass)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputs(FLUID_CELL_GLASS_VIAL.getStackForm(4))
                .duration(128).volts(VA[LV])
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, NetherQuartz)
                .outputItem(plate, NetherQuartz)
                .duration(400).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, CertusQuartz)
                .outputItem(plate, CertusQuartz)
                .duration(400).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, Quartzite)
                .outputItem(plate, Quartzite)
                .duration(400).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(COKE_OVEN_BRICK, 4)
                .outputs(METAL_CASING.getItemVariant(MetalCasingType.COKE_BRICKS))
                .duration(300).volts(2).buildAndRegister();
    }

    private static void registerPrimitiveBlastFurnaceRecipes() {
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, Iron).inputItem(gem, Coal, 2)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, Iron).inputItem(dust, Coal, 2)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, Iron).inputItem(gem, Charcoal, 2)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, Iron).inputItem(dust, Charcoal, 2)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, Iron).inputItem(OREDICT_FUEL_COKE)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, Ash).duration(1500).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, Iron).inputItem(dust, Coke)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, Ash).duration(1500).buildAndRegister();

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(block, Iron).inputItem(block, Coal, 2)
                .outputItem(block, Steel)
                .outputItem(dust, DarkAsh, 2).duration(16200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(block, Iron).inputItem(block, Charcoal, 2)
                .outputItem(block, Steel).outputItem(dust, DarkAsh, 2).duration(16200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(block, Iron).inputItem(OREDICT_BLOCK_FUEL_COKE)
                .outputItem(block, Steel).outputItem(dust, Ash).duration(13500).buildAndRegister();

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, WroughtIron).inputItem(gem, Coal, 2)
                .outputItem(ingot, Steel).outputItem(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, WroughtIron).inputItem(dust, Coal, 2)
                .outputItem(ingot, Steel).outputItem(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, WroughtIron).inputItem(gem, Charcoal, 2)
                .outputItem(ingot, Steel).outputItem(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, WroughtIron).inputItem(dust, Charcoal, 2)
                .outputItem(ingot, Steel).outputItem(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, WroughtIron).inputItem(OREDICT_FUEL_COKE)
                .outputItem(ingot, Steel).outputItem(dustTiny, Ash).duration(600).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(ingot, WroughtIron).inputItem(dust, Coke)
                .outputItem(ingot, Steel)
                .outputItem(dustTiny, Ash).duration(600).buildAndRegister();

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(block, WroughtIron).inputItem(block, Coal, 2)
                .outputItem(block, Steel).outputItem(dust, DarkAsh, 2).duration(7200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(block, WroughtIron).inputItem(block, Charcoal, 2)
                .outputItem(block, Steel).outputItem(dust, DarkAsh, 2).duration(7200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItem(block, WroughtIron).inputItem(OREDICT_BLOCK_FUEL_COKE)
                .outputItem(block, Steel).outputItem(dust, Ash).duration(5400).buildAndRegister();
    }

    private static void registerCokeOvenRecipes() {
        COKE_OVEN_RECIPES.recipeBuilder().inputItem(log, Wood).outputItem(gem, Charcoal)
                .fluidOutputs(Creosote.getFluid(250))
                .duration(900).buildAndRegister();
        COKE_OVEN_RECIPES.recipeBuilder().inputItem(gem, Coal).outputItem(gem, Coke)
                .fluidOutputs(Creosote.getFluid(500))
                .duration(900).buildAndRegister();
        COKE_OVEN_RECIPES.recipeBuilder().inputItem(block, Coal).outputItem(block, Coke)
                .fluidOutputs(Creosote.getFluid(4500))
                .duration(8100).buildAndRegister();
    }

    private static void registerStoneBricksRecipes() {
        // normal variant -> cobble variant
        EnumMap<StoneVariant, List<ItemStack>> variantListMap = new EnumMap<>(StoneVariant.class);
        for (StoneVariant shape : StoneVariant.values()) {
            StoneVariantBlock block = MetaBlocks.STONE_BLOCKS.get(shape);
            variantListMap.put(shape,
                    Arrays.stream(StoneVariantBlock.StoneType.values())
                            .map(block::getItemVariant)
                            .collect(Collectors.toList()));
        }
        List<ItemStack> cobbles = variantListMap.get(StoneVariant.COBBLE);
        List<ItemStack> mossCobbles = variantListMap.get(StoneVariant.COBBLE_MOSSY);
        List<ItemStack> smooths = variantListMap.get(StoneVariant.SMOOTH);
        List<ItemStack> polisheds = variantListMap.get(StoneVariant.POLISHED);
        List<ItemStack> bricks = variantListMap.get(StoneVariant.BRICKS);
        List<ItemStack> crackedBricks = variantListMap.get(StoneVariant.BRICKS_CRACKED);
        List<ItemStack> mossBricks = variantListMap.get(StoneVariant.BRICKS_MOSSY);
        List<ItemStack> chiseledBricks = variantListMap.get(StoneVariant.CHISELED);
        List<ItemStack> tiledBricks = variantListMap.get(StoneVariant.TILED);
        List<ItemStack> smallTiledBricks = variantListMap.get(StoneVariant.TILED_SMALL);
        List<ItemStack> windmillA = variantListMap.get(StoneVariant.WINDMILL_A);
        List<ItemStack> windmillB = variantListMap.get(StoneVariant.WINDMILL_B);
        List<ItemStack> squareBricks = variantListMap.get(StoneVariant.BRICKS_SQUARE);
        List<ItemStack> smallBricks = variantListMap.get(StoneVariant.BRICKS_SMALL);

        registerSmoothRecipe(cobbles, smooths);
        registerCobbleRecipe(smooths, cobbles);
        registerMossRecipe(cobbles, mossCobbles);
        registerSmoothRecipe(smooths, polisheds);
        registerBricksRecipe(polisheds, bricks, MarkerMaterials.Color.LightBlue);
        registerCobbleRecipe(bricks, crackedBricks);
        registerMossRecipe(bricks, mossBricks);
        registerBricksRecipe(polisheds, chiseledBricks, MarkerMaterials.Color.White);
        registerBricksRecipe(polisheds, tiledBricks, MarkerMaterials.Color.Red);
        registerBricksRecipe(tiledBricks, smallTiledBricks, MarkerMaterials.Color.Red);
        registerBricksRecipe(polisheds, windmillA, MarkerMaterials.Color.Blue);
        registerBricksRecipe(polisheds, windmillB, MarkerMaterials.Color.Yellow);
        registerBricksRecipe(polisheds, squareBricks, MarkerMaterials.Color.Green);
        registerBricksRecipe(polisheds, smallBricks, MarkerMaterials.Color.Pink);

        for (int i = 0; i < smooths.size(); i++) {
            EXTRUDER_RECIPES.recipeBuilder()
                    .inputs(smooths.get(i))
                    .notConsumable(SHAPE_EXTRUDER_INGOT.getStackForm())
                    .outputs(bricks.get(i))
                    .duration(24).volts(8).buildAndRegister();
        }
    }

    private static void registerMixingCrystallizationRecipes() {
        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, SiliconDioxide)
                .fluidInputs(DistilledWater.getFluid(250))
                .outputItemRoll(gem, Quartzite, 1000, 1000)
                .duration(1200).volts(24).buildAndRegister();

        // todo find UU-Matter replacement
        // RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
        // .input(OrePrefix.dust, Materials.NetherStar)
        // .fluidInputs(Materials.UUMatter.getFluid(576))
        // .chancedOutput(new ItemStack(Items.NETHER_STAR), 3333, 3333)
        // .duration(72000).EUt(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(crushedPurified, Sphalerite)
                .inputItem(crushedPurified, Galena)
                .fluidInputs(SulfuricAcid.getFluid(4000))
                .fluidOutputs(IndiumConcentrate.getFluid(1000))
                .duration(60).volts(150).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Coal)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Charcoal)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).volts(16).buildAndRegister();
    }

    private static final MaterialStack[][] alloySmelterList = {
            { new MaterialStack(Materials.Copper, 3L), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.Bronze, 4L) },
            { new MaterialStack(Materials.Copper, 3L), new MaterialStack(Materials.Zinc, 1),
                    new MaterialStack(Materials.Brass, 4L) },
            { new MaterialStack(Materials.Copper, 1), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Cupronickel, 2L) },
            { new MaterialStack(Materials.Copper, 1), new MaterialStack(Materials.Redstone, 4L),
                    new MaterialStack(Materials.RedAlloy, 1) },
            { new MaterialStack(Materials.AnnealedCopper, 3L), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.Bronze, 4L) },
            { new MaterialStack(Materials.AnnealedCopper, 3L), new MaterialStack(Materials.Zinc, 1),
                    new MaterialStack(Materials.Brass, 4L) },
            { new MaterialStack(Materials.AnnealedCopper, 1), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Cupronickel, 2L) },
            { new MaterialStack(Materials.AnnealedCopper, 1), new MaterialStack(Materials.Redstone, 4L),
                    new MaterialStack(Materials.RedAlloy, 1) },
            { new MaterialStack(Materials.Iron, 1), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.TinAlloy, 2L) },
            { new MaterialStack(Materials.WroughtIron, 1), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.TinAlloy, 2L) },
            { new MaterialStack(Materials.Iron, 2L), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Invar, 3L) },
            { new MaterialStack(Materials.WroughtIron, 2L), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Invar, 3L) },
            { new MaterialStack(Materials.Lead, 4L), new MaterialStack(Materials.Antimony, 1),
                    new MaterialStack(Materials.BatteryAlloy, 5L) },
            { new MaterialStack(Materials.Gold, 1), new MaterialStack(Materials.Silver, 1),
                    new MaterialStack(Materials.Electrum, 2L) },
            { new MaterialStack(Materials.Magnesium, 1), new MaterialStack(Materials.Aluminium, 2L),
                    new MaterialStack(Materials.Magnalium, 3L) },
            { new MaterialStack(Materials.Silver, 1), new MaterialStack(Materials.Electrotine, 4),
                    new MaterialStack(Materials.BlueAlloy, 1) } };

    private static void registerAlloyRecipes() {
        for (MaterialStack[] stack : alloySmelterList) {
            if (stack[0].material.hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .duration((int) stack[2].amount * 50).volts(16)
                        .inputItem(OrePrefix.ingot, stack[0].material, (int) stack[0].amount)
                        .inputItem(OrePrefix.dust, stack[1].material, (int) stack[1].amount)
                        .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                        .buildAndRegister();
            }
            if (stack[1].material.hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .duration((int) stack[2].amount * 50).volts(16)
                        .inputItem(OrePrefix.dust, stack[0].material, (int) stack[0].amount)
                        .inputItem(OrePrefix.ingot, stack[1].material, (int) stack[1].amount)
                        .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                        .buildAndRegister();
            }
            if (stack[0].material.hasProperty(PropertyKey.INGOT) && stack[1].material.hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .duration((int) stack[2].amount * 50).volts(16)
                        .inputItem(OrePrefix.ingot, stack[0].material, (int) stack[0].amount)
                        .inputItem(OrePrefix.ingot, stack[1].material, (int) stack[1].amount)
                        .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                        .buildAndRegister();
            }
            ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .duration((int) stack[2].amount * 50).volts(16)
                    .inputItem(OrePrefix.dust, stack[0].material, (int) stack[0].amount)
                    .inputItem(OrePrefix.dust, stack[1].material, (int) stack[1].amount)
                    .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                    .buildAndRegister();
        }

        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.CARBON_FIBERS.getStackForm(2))
                .outputs(MetaItems.CARBON_MESH.getStackForm()).duration(100).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.CARBON_MESH.getStackForm())
                .outputs(MetaItems.CARBON_FIBER_PLATE.getStackForm()).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(10).volts(VA[ULV])
                .inputItem(OrePrefix.ingot, Materials.Rubber, 2)
                .notConsumable(MetaItems.SHAPE_MOLD_PLATE).outputItem(OrePrefix.plate, Materials.Rubber)
                .buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV]).inputItem(OrePrefix.dust, Materials.Sulfur)
                .inputItem(OrePrefix.dust, Materials.RawRubber, 3).outputItem(OrePrefix.ingot, Materials.Rubber)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(150).volts(VA[ULV]).inputs(OreDictUnifier.get("sand"))
                .inputs(new ItemStack(Items.CLAY_BALL)).outputs(COKE_OVEN_BRICK.getStackForm(2)).buildAndRegister();
    }

    private static void registerAssemblerRecipes() {
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CANNER_RECIPES.recipeBuilder()
                    .inputs(SPRAY_EMPTY.getStackForm())
                    .fluidInputs(CHEMICAL_DYES[i].getFluid(L * 4))
                    .outputs(SPRAY_CAN_DYES[i].getStackForm()).volts(VA[ULV]).duration(200)
                    .buildAndRegister();

            EnumDyeColor color = EnumDyeColor.byMetadata(i);
            BlockLamp lamp = MetaBlocks.LAMPS.get(color);
            for (int lampMeta = 0; lampMeta < lamp.getItemMetadataStates(); lampMeta++) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItem(plate, Glass, 6)
                        .inputItem(dust, Glowstone, 1)
                        .fluidInputs(CHEMICAL_DYES[i].getFluid(L))
                        .outputs(new ItemStack(lamp, 6, lampMeta))
                        .circuitMeta(lampMeta + 1).volts(VA[ULV]).duration(40)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItem(lampGt, MarkerMaterials.Color.COLORS.get(color))
                        .outputs(new ItemStack(lamp, 1, lampMeta))
                        .circuitMeta(lampMeta + 1).volts(VA[ULV]).duration(10)
                        .buildAndRegister();
            }
            lamp = MetaBlocks.BORDERLESS_LAMPS.get(color);
            for (int lampMeta = 0; lampMeta < lamp.getItemMetadataStates(); lampMeta++) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItem(plate, Glass, 6)
                        .inputItem(dust, Glowstone, 1)
                        .fluidInputs(CHEMICAL_DYES[i].getFluid(L))
                        .outputs(new ItemStack(lamp, 6, lampMeta))
                        .circuitMeta(lampMeta + 9).volts(VA[ULV]).duration(40)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItem(lampGt, MarkerMaterials.Color.COLORS.get(color))
                        .outputs(new ItemStack(lamp, 1, lampMeta))
                        .circuitMeta(lampMeta + 9).volts(VA[ULV]).duration(10)
                        .buildAndRegister();
            }
        }

        CANNER_RECIPES.recipeBuilder()
                .inputItem(SPRAY_EMPTY)
                .fluidInputs(Acetone.getFluid(1000))
                .outputItem(SPRAY_SOLVENT).volts(VA[ULV]).duration(200)
                .buildAndRegister();

        Material material = Materials.Iron;

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_DOOR))
                .inputItem(plate, material, 2)
                .outputs(COVER_SHUTTER.getStackForm(2)).volts(16).duration(100)
                .buildAndRegister();

        FluidStack solder = SolderingAlloy.getFluid(L / 2);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.LEVER))
                .inputItem(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_MACHINE_CONTROLLER.getStackForm(1)).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Copper, 4)
                .inputItem(circuit, MarkerMaterials.Tier.LV)
                .inputItem(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_ENERGY_DETECTOR.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(COVER_ENERGY_DETECTOR)
                .inputItem(SENSOR_HV)
                .fluidInputs(solder)
                .outputItem(COVER_ENERGY_DETECTOR_ADVANCED).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH))
                .inputItem(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_ACTIVITY_DETECTOR.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, Gold, 4)
                .inputItem(circuit, MarkerMaterials.Tier.HV)
                .inputItem(plate, Aluminium)
                .fluidInputs(solder)
                .outputs(COVER_ACTIVITY_DETECTOR_ADVANCED.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .inputItem(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_FLUID_DETECTOR.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                .inputItem(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_ITEM_DETECTOR.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(COVER_FLUID_DETECTOR)
                .inputItem(SENSOR_HV)
                .fluidInputs(solder)
                .outputs(COVER_FLUID_DETECTOR_ADVANCED.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(COVER_ITEM_DETECTOR)
                .inputItem(SENSOR_HV)
                .fluidInputs(solder)
                .outputs(COVER_ITEM_DETECTOR_ADVANCED.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(EMITTER_LV)
                .inputItem(plate, Steel)
                .circuitMeta(1)
                .fluidInputs(solder)
                .outputs(COVER_MAINTENANCE_DETECTOR.getStackForm()).volts(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Glass)
                .inputItem(foil, Aluminium, 4)
                .inputItem(circuit, MarkerMaterials.Tier.LV)
                .inputItem(wireFine, Copper, 4)
                .outputs(COVER_SCREEN.getStackForm()).volts(16).duration(50)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_PUMP_HV, 2)
                .inputs(new ItemStack(Items.CAULDRON))
                .inputItem(circuit, MarkerMaterials.Tier.HV)
                .outputItem(COVER_INFINITE_WATER).volts(VA[HV]).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, EnderPearl, 9)
                .inputItem(plateDouble, StainlessSteel)
                .inputItem(SENSOR_HV)
                .inputItem(EMITTER_HV)
                .inputItem(ELECTRIC_PUMP_HV)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .outputItem(COVER_ENDER_FLUID_LINK).volts(VA[HV]).duration(320)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(OreDictNames.chestWood.toString())
                .inputItem(ELECTRIC_PISTON_LV)
                .inputItem(plate, Iron)
                .fluidInputs(SolderingAlloy.getFluid(72))
                .outputItem(COVER_STORAGE).volts(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, WroughtIron, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV)).circuitMeta(8).duration(25)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Steel, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Aluminium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, StainlessSteel, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Titanium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, TungstenSteel, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, RhodiumPlatedPalladium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, NaquadahAlloy, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Darmstadtium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Neutronium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UHV)).circuitMeta(8).duration(50)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LV])
                .inputItem(OrePrefix.wireGtDouble, Materials.Cupronickel, 8)
                .inputItem(OrePrefix.foil, Materials.Bronze, 8).fluidInputs(Materials.TinAlloy.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.CUPRONICKEL)).duration(200).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[MV]).inputItem(OrePrefix.wireGtDouble, Materials.Kanthal, 8)
                .inputItem(OrePrefix.foil, Materials.Aluminium, 8).fluidInputs(Materials.Copper.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.KANTHAL)).duration(300).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[HV])
                .inputItem(OrePrefix.wireGtDouble, Materials.Nichrome, 8)
                .inputItem(OrePrefix.foil, Materials.StainlessSteel, 8)
                .fluidInputs(Materials.Aluminium.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NICHROME)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[EV])
                .inputItem(OrePrefix.wireGtDouble, Materials.RTMAlloy, 8)
                .inputItem(OrePrefix.foil, Materials.VanadiumSteel, 8)
                .fluidInputs(Materials.Nichrome.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.RTM_ALLOY)).duration(500).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[IV]).inputItem(OrePrefix.wireGtDouble, Materials.HSSG, 8)
                .inputItem(OrePrefix.foil, Materials.TungstenCarbide, 8)
                .fluidInputs(Materials.Tungsten.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.HSS_G)).duration(600).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LuV])
                .inputItem(OrePrefix.wireGtDouble, Materials.Naquadah, 8)
                .inputItem(OrePrefix.foil, Materials.Osmium, 8)
                .fluidInputs(Materials.TungstenSteel.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NAQUADAH)).duration(700).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[ZPM])
                .inputItem(OrePrefix.wireGtDouble, Materials.Trinium, 8)
                .inputItem(OrePrefix.foil, Materials.NaquadahEnriched, 8)
                .fluidInputs(Materials.Naquadah.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRINIUM)).duration(800).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[UV])
                .inputItem(OrePrefix.wireGtDouble, Materials.Tritanium, 8)
                .inputItem(OrePrefix.foil, Materials.Naquadria, 8).fluidInputs(Materials.Trinium.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRITANIUM)).duration(900).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.Bronze, 6)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 1)).circuitMeta(6)
                .outputs(METAL_CASING.getItemVariant(BRONZE_BRICKS, ConfigHolder.recipes.casingsPerCraft)).duration(50)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.Invar, 6)
                .inputItem(OrePrefix.frameGt, Materials.Invar, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.INVAR_HEATPROOF, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.Steel, 6)
                .inputItem(OrePrefix.frameGt, Materials.Steel, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.STEEL_SOLID, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.Aluminium, 6)
                .inputItem(OrePrefix.frameGt, Materials.Aluminium, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.ALUMINIUM_FROSTPROOF, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.TungstenSteel, 6)
                .inputItem(OrePrefix.frameGt, Materials.TungstenSteel, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.TUNGSTENSTEEL_ROBUST, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.StainlessSteel, 6)
                .inputItem(OrePrefix.frameGt, Materials.StainlessSteel, 1).circuitMeta(6)
                .outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.STAINLESS_CLEAN, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.Titanium, 6)
                .inputItem(OrePrefix.frameGt, Materials.Titanium, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.TITANIUM_STABLE, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(plate, HSSE, 6).inputItem(frameGt, Europium)
                .circuitMeta(6)
                .outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.HSSE_STURDY,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(plate, Palladium, 6).inputItem(frameGt, Iridium)
                .circuitMeta(6)
                .outputs(METAL_CASING.getItemVariant(MetalCasingType.PALLADIUM_SUBSTATION,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(16)
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.STEEL_SOLID))
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(216)).circuitMeta(6)
                .outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.PTFE_INERT_CASING)).duration(50)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LuV])
                .inputItem(OrePrefix.wireGtDouble, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .inputItem(OrePrefix.foil, Materials.NiobiumTitanium, 32)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 24))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[ZPM])
                .inputItem(OrePrefix.wireGtDouble, Materials.UraniumRhodiumDinaquadide, 16)
                .inputItem(OrePrefix.foil, Materials.NiobiumTitanium, 16)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 16))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[UV])
                .inputItem(OrePrefix.wireGtDouble, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 8)
                .inputItem(OrePrefix.foil, Materials.NiobiumTitanium, 8)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 8))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[UV])
                .inputItem(OrePrefix.wireGtDouble, Materials.RutheniumTriniumAmericiumNeutronate, 4)
                .inputItem(OrePrefix.foil, Materials.NiobiumTitanium, 4)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 4))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(200).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[ZPM])
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .inputs(MetaItems.FIELD_GENERATOR_IV.getStackForm(2)).inputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                .inputs(MetaItems.NEUTRON_REFLECTOR.getStackForm(2))
                .inputItem(OrePrefix.circuit, MarkerMaterials.Tier.LuV, 4)
                .inputItem(OrePrefix.pipeSmallFluid, Materials.Naquadah, 4)
                .inputItem(OrePrefix.plate, Materials.Europium, 4)
                .fluidInputs(Materials.VanadiumGallium.getFluid(GTValues.L * 4))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LuV])
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .inputItem(OrePrefix.plate, Materials.Naquadah, 4).inputs(MetaItems.NEUTRON_REFLECTOR.getStackForm(4))
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS,
                        ConfigHolder.recipes.casingsPerCraft))
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L)).duration(50)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LuV])
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV))
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .inputs(MetaItems.NEUTRON_REFLECTOR.getStackForm()).inputs(MetaItems.ELECTRIC_PUMP_LuV.getStackForm())
                .inputItem(OrePrefix.plate, Materials.TungstenSteel, 6)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[ZPM])
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM))
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .inputs(MetaItems.VOLTAGE_COIL_ZPM.getStackForm(2)).inputs(MetaItems.FIELD_GENERATOR_LuV.getStackForm())
                .inputItem(OrePrefix.plate, Materials.Europium, 6)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 2))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK2,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[UV])
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV))
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .inputs(MetaItems.VOLTAGE_COIL_UV.getStackForm(2)).inputs(MetaItems.FIELD_GENERATOR_ZPM.getStackForm())
                .inputItem(OrePrefix.plate, Materials.Americium, 6)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 4))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK3,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plate, Materials.Magnalium, 6)
                .inputItem(OrePrefix.frameGt, Materials.BlueSteel, 1).circuitMeta(6).outputs(MetaBlocks.TURBINE_CASING
                        .getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16)
                .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING))
                .inputItem(OrePrefix.plate, Materials.StainlessSteel, 6).circuitMeta(6)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STAINLESS_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16)
                .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING))
                .inputItem(OrePrefix.plate, Materials.Titanium, 6).circuitMeta(6)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.TITANIUM_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16)
                .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING))
                .inputItem(OrePrefix.plate, Materials.TungstenSteel, 6).circuitMeta(6)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(48).inputItem(OrePrefix.frameGt, Materials.Steel)
                .inputItem(OrePrefix.plate, Materials.Polyethylene, 6).fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.PLASCRETE,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(200).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(48).inputItem(OrePrefix.frameGt, Materials.Steel)
                .inputItem(OrePrefix.plate, Materials.Polyethylene, 6).fluidInputs(Glass.getFluid(L))
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.CLEANROOM_GLASS,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(200).buildAndRegister();

        // If these recipes are changed, change the values in MaterialInfoLoader.java

        ASSEMBLER_RECIPES.recipeBuilder().duration(25).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV))
                .inputItem(OrePrefix.cableGtSingle, Materials.RedAlloy, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[0].getStackForm())
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV))
                .inputItem(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[1].getStackForm()).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV))
                .inputItem(OrePrefix.cableGtSingle, Materials.Copper, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[2].getStackForm()).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV))
                .inputItem(OrePrefix.cableGtSingle, Materials.AnnealedCopper, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[2].getStackForm())
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV))
                .inputItem(OrePrefix.cableGtSingle, Materials.Gold, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[3].getStackForm()).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV))
                .inputItem(OrePrefix.cableGtSingle, Materials.Aluminium, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[4].getStackForm())
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV))
                .inputItem(OrePrefix.cableGtSingle, Materials.Platinum, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[5].getStackForm())
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV))
                .inputItem(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[6].getStackForm())
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM))
                .inputItem(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2)).outputs(MetaTileEntities.HULL[7].getStackForm())
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV))
                .inputItem(cableGtSingle, Materials.YttriumBariumCuprate, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[8].getStackForm()).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(50).volts(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UHV))
                .inputItem(cableGtSingle, Materials.Europium, 2).fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[9].getStackForm()).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(2).inputItem(OreDictNames.chestWood.toString())
                .inputItem(plate, Iron, 5)
                .outputs(new ItemStack(Blocks.HOPPER)).duration(800).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(2).inputItem(OreDictNames.chestWood.toString())
                .inputItem(plate, WroughtIron, 5)
                .outputs(new ItemStack(Blocks.HOPPER)).duration(800).circuitMeta(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(OrePrefix.plank, Wood, 4).inputItem(screw, Iron, 4)
                .outputs(WOODEN_CRATE.getStackForm()).duration(100).circuitMeta(5).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Bronze, 4).inputItem(plate, Bronze, 4)
                .outputs(BRONZE_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Steel, 4).inputItem(plate, Steel, 4)
                .outputs(STEEL_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Aluminium, 4).inputItem(plate, Aluminium, 4)
                .outputs(ALUMINIUM_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, StainlessSteel, 4)
                .inputItem(plate, StainlessSteel, 4)
                .outputs(STAINLESS_STEEL_CRATE.getStackForm()).circuitMeta(1).duration(200).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Titanium, 4).inputItem(plate, Titanium, 4)
                .outputs(TITANIUM_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, TungstenSteel, 4)
                .inputItem(plate, TungstenSteel, 4)
                .outputs(TUNGSTENSTEEL_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Bronze, 2).inputItem(plate, Bronze, 4)
                .outputs(BRONZE_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Steel, 2).inputItem(plate, Steel, 4)
                .outputs(STEEL_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Aluminium, 2).inputItem(plate, Aluminium, 4)
                .outputs(ALUMINIUM_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, StainlessSteel, 2)
                .inputItem(plate, StainlessSteel, 4)
                .outputs(STAINLESS_STEEL_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Titanium, 2).inputItem(plate, Titanium, 4)
                .outputs(TITANIUM_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, TungstenSteel, 2)
                .inputItem(plate, TungstenSteel, 4)
                .outputs(TUNGSTENSTEEL_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(16).inputItem(stickLong, Gold, 2).inputItem(plate, Gold, 4)
                .outputs(GOLD_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LV]).inputItem(foil, Polyethylene, 4).inputItem(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(288)).outputItem(DUCT_TAPE).duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LV]).inputItem(foil, SiliconeRubber, 2).inputItem(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(288)).outputItem(DUCT_TAPE, 2).duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LV]).inputItem(foil, Polycaprolactam, 2).inputItem(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(144)).outputItem(DUCT_TAPE, 4).duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[LV]).inputItem(foil, Polybenzimidazole).inputItem(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(72)).outputItem(DUCT_TAPE, 8).duration(100).buildAndRegister();

        ModHandler.addShapedRecipe("basic_tape", BASIC_TAPE.getStackForm(), " P ", "PSP", " P ", 'P',
                new UnificationEntry(plate, Paper), 'S', STICKY_RESIN.getStackForm());
        ASSEMBLER_RECIPES.recipeBuilder().volts(VA[ULV]).inputItem(plate, Paper, 2).inputItem(STICKY_RESIN)
                .outputItem(BASIC_TAPE, 2)
                .duration(100).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plateDouble, Steel, 2)
                .inputItem(ring, Bronze, 2)
                .outputItem(FLUID_CELL_LARGE_STEEL)
                .duration(200).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plateDouble, Aluminium, 2)
                .inputItem(ring, Silver, 2)
                .outputItem(FLUID_CELL_LARGE_ALUMINIUM)
                .duration(200).volts(64).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plateDouble, StainlessSteel, 3)
                .inputItem(ring, Electrum, 3)
                .outputItem(FLUID_CELL_LARGE_STAINLESS_STEEL)
                .duration(200).volts(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plateDouble, Titanium, 3)
                .inputItem(ring, RoseGold, 3)
                .outputItem(FLUID_CELL_LARGE_TITANIUM)
                .duration(200).volts(256).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plateDouble, TungstenSteel, 4)
                .inputItem(ring, Platinum, 4)
                .outputItem(FLUID_CELL_LARGE_TUNGSTEN_STEEL)
                .duration(200).volts(VA[HV]).buildAndRegister();
    }

    private static void registerBlastFurnaceRecipes() {
        // Steel
        BLAST_RECIPES.recipeBuilder().duration(500).volts(VA[MV])
                .inputItem(ingot, Iron)
                .fluidInputs(Oxygen.getFluid(200))
                .outputItem(ingot, Steel).outputItemRoll(dust, Ash, 1111, 0)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(400).volts(VA[MV])
                .inputItem(dust, Iron)
                .fluidInputs(Oxygen.getFluid(200))
                .outputItem(ingot, Steel).outputItemRoll(dust, Ash, 1111, 0)
                .circuitMeta(2)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(300).volts(VA[MV])
                .inputItem(ingot, WroughtIron)
                .fluidInputs(Oxygen.getFluid(200))
                .outputItem(ingot, Steel).outputItemRoll(dust, Ash, 1111, 0)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(100).volts(VA[MV])
                .inputItem(dust, WroughtIron)
                .fluidInputs(Oxygen.getFluid(200))
                .outputItem(ingot, Steel).outputItemRoll(dust, Ash, 1111, 0)
                .circuitMeta(2)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(250).volts(VA[EV])
                .inputItem(dust, Iron, 4)
                .inputItem(dust, Carbon)
                .outputItem(ingot, Steel, 4).outputItemRoll(dust, Ash, 1111, 0)
                .blastFurnaceTemp(2000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(50).volts(VA[EV])
                .inputItem(dust, WroughtIron, 4)
                .inputItem(dust, Carbon)
                .outputItem(ingot, Steel, 4).outputItemRoll(dust, Ash, 1111, 0)
                .blastFurnaceTemp(2000)
                .buildAndRegister();

        // Aluminium from aluminium oxide gems
        BLAST_RECIPES.recipeBuilder().duration(400).volts(100).inputItem(dust, Ruby).outputItem(nugget, Aluminium, 3)
                .outputItemRoll(dust, Ash, 1111, 0).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).volts(100).inputItem(gem, Ruby).outputItem(nugget, Aluminium, 3)
                .outputItemRoll(dust, Ash, 1111, 0).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).volts(100).inputItem(dust, GreenSapphire)
                .outputItem(nugget, Aluminium, 3).outputItemRoll(dust, Ash, 1111, 0).blastFurnaceTemp(1200)
                .buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).volts(100).inputItem(gem, GreenSapphire)
                .outputItem(nugget, Aluminium, 3).outputItemRoll(dust, Ash, 1111, 0).blastFurnaceTemp(1200)
                .buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).volts(100).inputItem(dust, Sapphire)
                .outputItem(nugget, Aluminium, 3)
                .blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).volts(100).inputItem(gem, Sapphire).outputItem(nugget, Aluminium, 3)
                .blastFurnaceTemp(1200).buildAndRegister();

        // Tempered Glass
        BLAST_RECIPES.recipeBuilder()
                .inputItem(block, Glass)
                .fluidInputs(Oxygen.getFluid(100))
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS))
                .blastFurnaceTemp(1000)
                .duration(200).volts(VA[MV]).buildAndRegister();

        registerBlastFurnaceMetallurgyRecipes();
    }

    private static void registerBlastFurnaceMetallurgyRecipes() {
        createSulfurDioxideRecipe(Stibnite, AntimonyTrioxide, 1500);
        createSulfurDioxideRecipe(Sphalerite, Zincite, 1000);
        createSulfurDioxideRecipe(Pyrite, BandedIron, 2000);
        createSulfurDioxideRecipe(Pentlandite, Garnierite, 1000);

        BLAST_RECIPES.recipeBuilder().duration(120).volts(VA[MV]).blastFurnaceTemp(1200)
                .inputItem(dust, Tetrahedrite)
                .fluidInputs(Oxygen.getFluid(3000))
                .outputItem(dust, CupricOxide)
                .outputItem(dustTiny, AntimonyTrioxide, 3)
                .fluidOutputs(SulfurDioxide.getFluid(2000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).volts(VA[MV]).blastFurnaceTemp(1200)
                .inputItem(dust, Cobaltite)
                .fluidInputs(Oxygen.getFluid(3000))
                .outputItem(dust, CobaltOxide)
                .outputItem(dust, ArsenicTrioxide)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).volts(VA[MV]).blastFurnaceTemp(1200)
                .inputItem(dust, Galena)
                .fluidInputs(Oxygen.getFluid(3000))
                .outputItem(dust, Massicot)
                .outputItem(nugget, Silver, 6)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).volts(VA[MV]).blastFurnaceTemp(1200)
                .inputItem(dust, Chalcopyrite)
                .inputItem(dust, SiliconDioxide)
                .fluidInputs(Oxygen.getFluid(3000))
                .outputItem(dust, CupricOxide)
                .outputItem(dust, Ferrosilite)
                .fluidOutputs(SulfurDioxide.getFluid(2000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(240).volts(VA[MV]).blastFurnaceTemp(2273)
                .inputItem(dust, SiliconDioxide, 3)
                .inputItem(dust, Carbon, 2)
                .outputItem(ingotHot, Silicon).outputItemRoll(dust, Ash, 1111, 0)
                .fluidOutputs(CarbonMonoxide.getFluid(2000))
                .buildAndRegister();
    }

    private static void createSulfurDioxideRecipe(Material inputMaterial, Material outputMaterial,
                                                  int sulfurDioxideAmount) {
        BLAST_RECIPES.recipeBuilder().duration(120).volts(VA[MV]).blastFurnaceTemp(1200)
                .inputItem(dust, inputMaterial)
                .fluidInputs(Oxygen.getFluid(3000))
                .outputItem(dust, outputMaterial).outputItemRoll(dust, Ash, 1111, 0)
                .fluidOutputs(SulfurDioxide.getFluid(sulfurDioxideAmount))
                .buildAndRegister();
    }

    private static void registerDecompositionRecipes() {
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(STICKY_RESIN.getStackForm())
                .outputItem(dust, RawRubber, 3)
                .duration(150).volts(2)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(MetaBlocks.RUBBER_LEAVES, 16))
                .outputItem(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(MetaBlocks.RUBBER_LOG))
                .outputItem(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(MetaBlocks.RUBBER_SAPLING))
                .outputItem(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(150).volts(2)
                .inputs(new ItemStack(Items.SLIME_BALL))
                .outputItem(dust, RawRubber, 2)
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputItem("treeSapling", 8).outputItem(PLANT_BALL)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Items.WHEAT, 8))
                .outputItem(PLANT_BALL)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Items.POTATO, 8))
                .outputItem(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Items.CARROT, 8))
                .outputItem(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Blocks.CACTUS, 8))
                .outputItem(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Items.REEDS, 8))
                .outputItem(PLANT_BALL)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Blocks.BROWN_MUSHROOM, 8))
                .outputItem(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Blocks.RED_MUSHROOM, 8))
                .outputItem(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2).inputs(new ItemStack(Items.BEETROOT, 8))
                .outputItem(PLANT_BALL).buildAndRegister();
    }

    private static void registerRecyclingRecipes() {
        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Endstone)
                .outputItem(dust, Endstone).outputItemRoll(dust, Tungstate, 130, 30)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Netherrack)
                .outputItem(dust, Netherrack).outputItemRoll(nugget, Gold, 500, 120)
                .buildAndRegister();

        if (!OreDictionary.getOres("stoneSoapstone").isEmpty())
            MACERATOR_RECIPES.recipeBuilder()
                    .inputItem(stone, Soapstone)
                    .outputItem(dustImpure, Talc).outputItemRoll(dust, Chromite, 111, 30)
                    .buildAndRegister();

        if (!OreDictionary.getOres("stoneRedrock").isEmpty())
            MACERATOR_RECIPES.recipeBuilder()
                    .inputItem(stone, Redrock)
                    .outputItem(dust, Redrock).outputItemRoll(dust, Redrock, 1000, 380)
                    .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Marble)
                .outputItem(dust, Marble).outputItemRoll(dust, Marble, 1000, 380)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Basalt)
                .outputItem(dust, Basalt).outputItemRoll(dust, Basalt, 1000, 380)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, GraniteBlack)
                .outputItem(dust, GraniteBlack).outputItemRoll(dust, Thorium, 100, 40)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, GraniteRed)
                .outputItem(dust, GraniteRed).outputItemRoll(dust, Uranium, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Andesite)
                .outputItem(dust, Andesite).outputItemRoll(dust, Stone, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Diorite)
                .outputItem(dust, Diorite).outputItemRoll(dust, Stone, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(stone, Granite)
                .outputItem(dust, Granite).outputItemRoll(dust, Stone, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PORKCHOP))
                .outputItem(dust, Meat).outputItemRoll(dust, Meat, 5000, 0)
                .outputItem(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.FISH, 1, W))
                .outputItem(dust, Meat).outputItemRoll(dust, Meat, 5000, 0)
                .outputItem(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.CHICKEN))
                .outputItem(dust, Meat)
                .outputItem(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.BEEF))
                .outputItem(dust, Meat).outputItemRoll(dust, Meat, 5000, 0)
                .outputItem(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.RABBIT))
                .outputItem(dust, Meat).outputItemRoll(dust, Meat, 5000, 0)
                .outputItem(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MUTTON))
                .outputItem(dust, Meat)
                .outputItem(dustTiny, Bone)
                .duration(102).buildAndRegister();
    }

    private static void registerFluidRecipes() {
        FLUID_HEATER_RECIPES.recipeBuilder().duration(32).volts(4)
                .fluidInputs(Ice.getFluid(L))
                .circuitMeta(1)
                .fluidOutputs(Water.getFluid(L)).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .fluidInputs(Toluene.getFluid(100))
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItem(GELLED_TOLUENE)
                .duration(100).volts(16).buildAndRegister();

        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .fluidInputs(CHEMICAL_DYES[i].getFluid(L / 2))
                    .notConsumable(SHAPE_MOLD_BALL.getStackForm())
                    .outputs(DYE_ONLY_ITEMS[i].getStackForm())
                    .duration(100).volts(16).buildAndRegister();
        }

        FLUID_HEATER_RECIPES.recipeBuilder().duration(30).volts(VA[LV]).fluidInputs(Water.getFluid(6)).circuitMeta(1)
                .fluidOutputs(Steam.getFluid(960)).buildAndRegister();
        FLUID_HEATER_RECIPES.recipeBuilder().duration(30).volts(VA[LV]).fluidInputs(DistilledWater.getFluid(6))
                .circuitMeta(1).fluidOutputs(Steam.getFluid(960)).buildAndRegister();
    }

    private static void registerSmoothRecipe(List<ItemStack> roughStack, List<ItemStack> smoothStack) {
        for (int i = 0; i < roughStack.size(); i++) {
            ModHandler.addSmeltingRecipe(roughStack.get(i), smoothStack.get(i), 0.1f);

            EXTRUDER_RECIPES.recipeBuilder()
                    .inputs(roughStack.get(i))
                    .notConsumable(SHAPE_EXTRUDER_BLOCK.getStackForm())
                    .outputs(smoothStack.get(i))
                    .duration(24).volts(8).buildAndRegister();
        }
    }

    private static void registerCobbleRecipe(List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
        for (int i = 0; i < smoothStack.size(); i++) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputs(smoothStack.get(i))
                    .outputs(cobbleStack.get(i))
                    .duration(12).volts(4).buildAndRegister();
        }
    }

    private static void registerBricksRecipe(List<ItemStack> polishedStack, List<ItemStack> brickStack,
                                             MarkerMaterial color) {
        for (int i = 0; i < polishedStack.size(); i++) {
            LASER_ENGRAVER_RECIPES.recipeBuilder()
                    .inputs(polishedStack.get(i))
                    .notConsumable(craftingLens, color)
                    .outputs(brickStack.get(i))
                    .duration(50).volts(16).buildAndRegister();
        }
    }

    private static void registerMossRecipe(List<ItemStack> regularStack, List<ItemStack> mossStack) {
        for (int i = 0; i < regularStack.size(); i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(regularStack.get(i))
                    .fluidInputs(Water.getFluid(100))
                    .outputs(mossStack.get(i))
                    .duration(50).volts(16).buildAndRegister();
        }
    }

    private static void registerNBTRemoval() {
        for (MetaTileEntityQuantumChest chest : MetaTileEntities.QUANTUM_CHEST)
            if (chest != null) {
                ModHandler.addShapelessNBTClearingRecipe("quantum_chest_nbt_" + chest.getTier() + chest.getMetaName(),
                        chest.getStackForm(), chest.getStackForm());
            }

        for (MetaTileEntityQuantumTank tank : MetaTileEntities.QUANTUM_TANK)
            if (tank != null) {
                ModHandler.addShapelessNBTClearingRecipe("quantum_tank_nbt_" + tank.getTier() + tank.getMetaName(),
                        tank.getStackForm(), tank.getStackForm());
            }

        // Drums
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_wood", MetaTileEntities.WOODEN_DRUM.getStackForm(),
                MetaTileEntities.WOODEN_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_bronze", MetaTileEntities.BRONZE_DRUM.getStackForm(),
                MetaTileEntities.BRONZE_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_steel", MetaTileEntities.STEEL_DRUM.getStackForm(),
                MetaTileEntities.STEEL_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_aluminium", MetaTileEntities.ALUMINIUM_DRUM.getStackForm(),
                MetaTileEntities.ALUMINIUM_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_stainless_steel",
                MetaTileEntities.STAINLESS_STEEL_DRUM.getStackForm(),
                MetaTileEntities.STAINLESS_STEEL_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_titanium", MetaTileEntities.TITANIUM_DRUM.getStackForm(),
                MetaTileEntities.TITANIUM_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_tungstensteel",
                MetaTileEntities.TUNGSTENSTEEL_DRUM.getStackForm(), MetaTileEntities.TUNGSTENSTEEL_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_gold", MetaTileEntities.GOLD_DRUM.getStackForm(),
                MetaTileEntities.GOLD_DRUM.getStackForm());

        // Cells
        ModHandler.addShapedNBTClearingRecipe("cell_nbt_regular", MetaItems.FLUID_CELL.getStackForm(), " C", "  ", 'C',
                MetaItems.FLUID_CELL.getStackForm());
        ModHandler.addShapedNBTClearingRecipe("cell_nbt_universal", MetaItems.FLUID_CELL_UNIVERSAL.getStackForm(), " C",
                "  ", 'C', MetaItems.FLUID_CELL_UNIVERSAL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_steel", MetaItems.FLUID_CELL_LARGE_STEEL.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_STEEL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_aluminium",
                MetaItems.FLUID_CELL_LARGE_ALUMINIUM.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_ALUMINIUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_stainless_steel",
                MetaItems.FLUID_CELL_LARGE_STAINLESS_STEEL.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_STAINLESS_STEEL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_titanium",
                MetaItems.FLUID_CELL_LARGE_TITANIUM.getStackForm(), MetaItems.FLUID_CELL_LARGE_TITANIUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_tungstensteel",
                MetaItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_vial_nbt", MetaItems.FLUID_CELL_GLASS_VIAL.getStackForm(),
                MetaItems.FLUID_CELL_GLASS_VIAL.getStackForm());

        // Data Items
        ModHandler.addShapelessNBTClearingRecipe("data_stick_nbt", TOOL_DATA_STICK.getStackForm(),
                TOOL_DATA_STICK.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("data_orb_nbt", TOOL_DATA_ORB.getStackForm(),
                TOOL_DATA_ORB.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("data_module_nbt", TOOL_DATA_MODULE.getStackForm(),
                TOOL_DATA_MODULE.getStackForm());

        // Filters
        ModHandler.addShapelessNBTClearingRecipe("clear_item_filter",
                ITEM_FILTER.getStackForm(), ITEM_FILTER);
        ModHandler.addShapelessNBTClearingRecipe("clear_fluid_filter",
                FLUID_FILTER.getStackForm(), FLUID_FILTER);
        ModHandler.addShapelessNBTClearingRecipe("clear_smart_filter",
                SMART_FILTER.getStackForm(), SMART_FILTER);
        ModHandler.addShapelessNBTClearingRecipe("clear_oredict_filter",
                ORE_DICTIONARY_FILTER.getStackForm(), ORE_DICTIONARY_FILTER);

        // Jetpacks
        ModHandler.addShapelessRecipe("fluid_jetpack_clear", SEMIFLUID_JETPACK.getStackForm(),
                SEMIFLUID_JETPACK.getStackForm());

        // ClipBoard
        ModHandler.addShapelessNBTClearingRecipe("clipboard_nbt", CLIPBOARD.getStackForm(), CLIPBOARD.getStackForm());
    }

    private static void ConvertHatchToHatch() {
        for (int i = 0; i < FLUID_IMPORT_HATCH.length; i++) {
            if (FLUID_IMPORT_HATCH[i] != null && FLUID_EXPORT_HATCH[i] != null) {

                ModHandler.addShapedRecipe("fluid_hatch_output_to_input_" + FLUID_IMPORT_HATCH[i].getTier(),
                        FLUID_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', FLUID_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe("fluid_hatch_input_to_output_" + FLUID_EXPORT_HATCH[i].getTier(),
                        FLUID_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', FLUID_IMPORT_HATCH[i].getStackForm());
            }
        }
        for (int i = 0; i < ITEM_IMPORT_BUS.length; i++) {
            if (ITEM_IMPORT_BUS[i] != null && ITEM_EXPORT_BUS[i] != null) {

                ModHandler.addShapedRecipe("item_bus_output_to_input_" + ITEM_IMPORT_BUS[i].getTier(),
                        ITEM_IMPORT_BUS[i].getStackForm(),
                        "d", "B", 'B', ITEM_EXPORT_BUS[i].getStackForm());
                ModHandler.addShapedRecipe("item_bus_input_to_output_" + ITEM_EXPORT_BUS[i].getTier(),
                        ITEM_EXPORT_BUS[i].getStackForm(),
                        "d", "B", 'B', ITEM_IMPORT_BUS[i].getStackForm());
            }
        }

        for (int i = 0; i < QUADRUPLE_IMPORT_HATCH.length; i++) {
            if (QUADRUPLE_IMPORT_HATCH[i] != null && QUADRUPLE_EXPORT_HATCH[i] != null) {
                ModHandler.addShapedRecipe(
                        "quadruple_fluid_hatch_output_to_input_" + QUADRUPLE_IMPORT_HATCH[i].getTier(),
                        QUADRUPLE_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', QUADRUPLE_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe(
                        "quadruple_fluid_hatch_input_to_output_" + QUADRUPLE_EXPORT_HATCH[i].getTier(),
                        QUADRUPLE_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', QUADRUPLE_IMPORT_HATCH[i].getStackForm());
            }
        }

        for (int i = 0; i < NONUPLE_IMPORT_HATCH.length; i++) {
            if (NONUPLE_IMPORT_HATCH[i] != null && NONUPLE_EXPORT_HATCH[i] != null) {
                ModHandler.addShapedRecipe("nonuple_fluid_hatch_output_to_input_" + NONUPLE_IMPORT_HATCH[i].getTier(),
                        NONUPLE_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', NONUPLE_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe("nonuple_fluid_hatch_input_to_output_" + NONUPLE_EXPORT_HATCH[i].getTier(),
                        NONUPLE_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', NONUPLE_IMPORT_HATCH[i].getStackForm());
            }
        }

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            ModHandler.addShapedRecipe("me_fluid_hatch_output_to_input", FLUID_IMPORT_HATCH_ME.getStackForm(), "d", "B",
                    'B', FLUID_EXPORT_HATCH_ME.getStackForm());
            ModHandler.addShapedRecipe("me_fluid_hatch_input_to_output", FLUID_EXPORT_HATCH_ME.getStackForm(), "d", "B",
                    'B', FLUID_IMPORT_HATCH_ME.getStackForm());
            ModHandler.addShapedRecipe("me_item_bus_output_to_input", ITEM_IMPORT_BUS_ME.getStackForm(), "d", "B", 'B',
                    ITEM_EXPORT_BUS_ME.getStackForm());
            ModHandler.addShapedRecipe("me_item_bus_input_to_output", ITEM_EXPORT_BUS_ME.getStackForm(), "d", "B", 'B',
                    ITEM_IMPORT_BUS_ME.getStackForm());
        }

        if (STEAM_EXPORT_BUS != null && STEAM_IMPORT_BUS != null) {
            // Steam
            ModHandler.addShapedRecipe("steam_bus_output_to_input_" + STEAM_EXPORT_BUS.getTier(),
                    STEAM_EXPORT_BUS.getStackForm(),
                    "d", "B", 'B', STEAM_IMPORT_BUS.getStackForm());
            ModHandler.addShapedRecipe("steam_bus_input_to_output_" + STEAM_IMPORT_BUS.getTier(),
                    STEAM_IMPORT_BUS.getStackForm(),
                    "d", "B", 'B', STEAM_EXPORT_BUS.getStackForm());
        }
    }
}
