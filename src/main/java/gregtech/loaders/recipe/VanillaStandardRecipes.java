package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.SHAPE_MOLD_BLOCK;

public class VanillaStandardRecipes {

    public static void init() {
        compressingRecipes();
        glassRecipes();
        smashingRecipes();
        engraverRecipes();
        woodRecipes();
        cuttingRecipes();
        dyingCleaningRecipes();
        redstoneRecipes();
    }

    private static void compressingRecipes() {
        RecipeMaps.COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .input(OrePrefix.plate, Materials.Stone, 9)
                .outputs(new ItemStack(Blocks.STONE))
                .buildAndRegister();

        //todo autogenerate 2x2 recipes?
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Blocks.SAND, 4))
                .outputs(new ItemStack(Blocks.SANDSTONE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Blocks.SAND, 4, 1))
                .outputs(new ItemStack(Blocks.RED_SANDSTONE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Items.BRICK, 4))
                .outputs(new ItemStack(Blocks.BRICK_BLOCK))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Items.NETHERBRICK, 4))
                .outputs(new ItemStack(Blocks.NETHER_BRICK))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Blocks.SNOW))
                .outputs(new ItemStack(Blocks.ICE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Items.CLAY_BALL, 4))
                .outputs(new ItemStack(Blocks.CLAY))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Items.GLOWSTONE_DUST, 4))
                .outputs(new ItemStack(Blocks.GLOWSTONE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(Items.QUARTZ, 4))
                .outputs(new ItemStack(Blocks.QUARTZ_BLOCK))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().inputs(new ItemStack(Blocks.ICE, 2, GTValues.W)).outputs(new ItemStack(Blocks.PACKED_ICE)).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().input(OrePrefix.dust, Materials.Ice, 1).outputs(new ItemStack(Blocks.ICE)).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.WHEAT, 9))
                .inputs(new CountableIngredient(new IntCircuitIngredient(9), 0))
                .outputs(new ItemStack(Blocks.HAY_BLOCK))
                .duration(200).EUt(2)
                .buildAndRegister();

    }

    private static void glassRecipes() {
        ModHandler.addShapedRecipe("glass_dust_hammer", OreDictUnifier.get(dust, Materials.Glass), "hG", 'G', new ItemStack(Blocks.GLASS, 1, GTValues.W));

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(160).EUt(7)
                .input(dustSmall, Materials.Flint)
                .input(dust, Materials.Quartzite, 4)
                .output(dust, Materials.Glass, 5)
                .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(200).EUt(7)
                .input(dustSmall, Materials.Flint)
                .input(dust, Materials.QuartzSand, 4)
                .output(dust, Materials.Glass, 4)
                .buildAndRegister();

        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder().duration(20).EUt(30)
                .inputs(new ItemStack(Blocks.SAND, 1))
                .outputs(new ItemStack(Blocks.GLASS, 2))
                .buildAndRegister();

        RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder().duration(100).EUt(30)
                .input(dust, Materials.Glass)
                .notConsumable(SHAPE_MOLD_BLOCK.getStackForm())
                .outputs(new ItemStack(Blocks.GLASS, 1))
                .buildAndRegister();

        RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder().duration(64).EUt(4)
                .input(dust, Materials.Glass)
                .notConsumable(MetaItems.SHAPE_MOLD_BOTTLE)
                .outputs(new ItemStack(Items.GLASS_BOTTLE))
                .buildAndRegister();

        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder().duration(32).EUt(16)
                .input(dust, Materials.Glass)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_BOTTLE)
                .outputs(new ItemStack(Items.GLASS_BOTTLE))
                .buildAndRegister();

        RecipeMaps.FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(12).EUt(4)
                .fluidInputs(Materials.Glass.getFluid(144))
                .notConsumable(MetaItems.SHAPE_MOLD_BOTTLE)
                .outputs(new ItemStack(Items.GLASS_BOTTLE))
                .buildAndRegister();

        RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder().duration(200).EUt(16)
                .input(dust, Materials.Glass)
                .notConsumable(SHAPE_MOLD_BLOCK.getStackForm())
                .outputs(new ItemStack(Blocks.GLASS, 1))
                .buildAndRegister();

        for (int i = 0; i < 16; i++) {
            //todo config
            ModHandler.removeRecipes(new ItemStack(Blocks.STAINED_GLASS_PANE, 16, i));

            ModHandler.addShapedRecipe("stained_glass_pane_" + i, new ItemStack(Blocks.STAINED_GLASS_PANE, 2, i), "sG", 'G', new ItemStack(Blocks.STAINED_GLASS, 1, i));

            CUTTER_RECIPES.recipeBuilder().duration(50).EUt(8)
                    .inputs(new ItemStack(Blocks.STAINED_GLASS, 3, i))
                    .outputs(new ItemStack(Blocks.STAINED_GLASS_PANE, 8, i))
                    .buildAndRegister();
        }

        CUTTER_RECIPES.recipeBuilder().duration(50).EUt(8)
                .inputs(new ItemStack(Blocks.GLASS, 3))
                .outputs(new ItemStack(Blocks.GLASS_PANE, 8))
                .buildAndRegister();

    }

    private static void smashingRecipes() {
        ModHandler.addShapedRecipe("cobblestone_hammer", new ItemStack(Blocks.COBBLESTONE), "h", "C", 'C', new UnificationEntry(OrePrefix.stone));

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(stone.name(), 1)
                .outputs(new ItemStack(Blocks.COBBLESTONE, 1))
                .EUt(16).duration(10)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(cobblestone.name(), 1)
                .outputs(new ItemStack(Blocks.GRAVEL, 1))
                .EUt(16).duration(10)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GRAVEL, 1))
                .outputs(new ItemStack(Blocks.SAND))
                .EUt(16).duration(10)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GRAVEL, 1))
                .output(dust, Stone)
                .chancedOutput(new ItemStack(Items.FLINT), 1000, 1000)
                .EUt(2).duration(400)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SANDSTONE, 1, GTValues.W))
                .outputs(new ItemStack(Blocks.SAND, 1))
                .EUt(2).duration(400).buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_SANDSTONE, 1, GTValues.W))
                .outputs(new ItemStack(Blocks.SAND, 1, 1))
                .EUt(2).duration(400).buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONEBRICK))
                .outputs(new ItemStack(Blocks.STONEBRICK, 1, 2))
                .EUt(2).duration(400).buildAndRegister();
    }

    private static void engraverRecipes() {
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SANDSTONE, 1, 2))
                .notConsumable(craftingLens, MarkerMaterials.Color.Colorless)
                .outputs(new ItemStack(Blocks.SANDSTONE, 1, 1))
                .duration(50).EUt(16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 2))
                .notConsumable(craftingLens, MarkerMaterials.Color.Colorless)
                .outputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 1))
                .duration(50).EUt(16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE))
                .notConsumable(craftingLens, MarkerMaterials.Color.Colorless)
                .outputs(new ItemStack(Blocks.STONEBRICK, 1, 3))
                .duration(50).EUt(16).buildAndRegister();
    }

    private static void woodRecipes() {
        ModHandler.addShapedRecipe("stick_saw", new ItemStack(Items.STICK, 4), "s", "P", "P", 'P', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("stick_normal", new ItemStack(Items.STICK, 2), "P", "P", 'P', new UnificationEntry(OrePrefix.plank, Materials.Wood));

        MACERATOR_RECIPES.recipeBuilder()
                .input(log, Wood)
                .output(dust, Wood, 6)
                .chancedOutput(dust, Wood, 8000, 680)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .input(plank, Wood)
                .output(stick, Wood, 2)
                .duration(10).EUt(8)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .input(log, Wood)
                .output(stickLong, Wood, 4)
                .output(dust, Wood, 2)
                .duration(160).EUt(8)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SAPLING, 1, GTValues.W))
                .outputs(new ItemStack(Items.STICK))
                .output(dustTiny, Wood)
                .duration(16).EUt(8)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W))
                .outputs(new ItemStack(Items.BOWL))
                .output(dustSmall, Wood)
                .duration(50).EUt(8)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plank, Wood, 6)
                .inputs(new ItemStack(Items.BOOK, 3))
                .outputs(new ItemStack(Blocks.BOOKSHELF))
                .duration(400).EUt(4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plank, Wood, 3).circuitMeta(3)
                .outputs(new ItemStack(Blocks.TRAPDOOR, 2))
                .duration(300).EUt(4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plank, Wood, 8)
                .outputs(new ItemStack(Blocks.CHEST))
                .duration(800).EUt(4).circuitMeta(8)
                .buildAndRegister();

    }

    private static void cuttingRecipes() {
        //todo jungle slabs get 2 cutting recipe sets
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.STONE)).outputs(new ItemStack(Blocks.STONE_SLAB, 2)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.SANDSTONE)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 1)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.COBBLESTONE)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 3)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.BRICK_BLOCK)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 4)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.STONEBRICK)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 5)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.NETHER_BRICK)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 6)).buildAndRegister();
    }

    private static void dyingCleaningRecipes() {
        for (int i = 0; i < 16; i++) {
            MIXER_RECIPES.recipeBuilder().duration(200).EUt(7)
                    .inputs(new ItemStack(Blocks.SAND, 4))
                    .inputs(new ItemStack(Blocks.GRAVEL, 4))
                    .input(dye, MarkerMaterials.Color.VALUES[i])
                    .outputs(new ItemStack(Blocks.CONCRETE_POWDER, 8, i))
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(7)
                    .inputs(new ItemStack(Blocks.CONCRETE_POWDER, 1, i))
                    .fluidInputs(Water.getFluid(1000))
                    .outputs(new ItemStack(Blocks.CONCRETE, 1, i))
                    .buildAndRegister();
        }

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Blocks.WOOL, 1, true)
                .fluidInputs(Chlorine.getFluid(50))
                .output(Blocks.WOOL)
                .duration(400).EUt(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Blocks.CARPET, 1, true)
                .fluidInputs(Chlorine.getFluid(25))
                .output(Blocks.CARPET)
                .duration(400).EUt(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Blocks.STAINED_HARDENED_CLAY, 1, true)
                .fluidInputs(Chlorine.getFluid(50))
                .output(Blocks.HARDENED_CLAY)
                .duration(400).EUt(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Blocks.STAINED_GLASS, 1, true)
                .fluidInputs(Chlorine.getFluid(50))
                .output(Blocks.GLASS)
                .duration(400).EUt(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Blocks.STAINED_GLASS_PANE, 1, true)
                .fluidInputs(Chlorine.getFluid(20))
                .output(Blocks.GLASS_PANE)
                .duration(400).EUt(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Blocks.CONCRETE, 1, true)
                .fluidInputs(Chlorine.getFluid(20))
                .output(Blocks.CONCRETE)
                .duration(400).EUt(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STICKY_PISTON))
                .fluidInputs(Chlorine.getFluid(10))
                .outputs(new ItemStack(Blocks.PISTON))
                .duration(30).EUt(30).buildAndRegister();
    }

    private static void redstoneRecipes() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaItems.RUBBER_DROP.getStackForm())
                .inputs(new ItemStack(Blocks.PISTON))
                .outputs(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).EUt(4).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input("slimeball", 1)
                .inputs(new ItemStack(Blocks.PISTON))
                .outputs(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).EUt(4).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.PISTON))
                .fluidInputs(Glue.getFluid(100))
                .outputs(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).EUt(4).buildAndRegister();

        //todo redstone config
        ModHandler.addShapedRecipe("piston_bronze", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', OrePrefix.stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Bronze));

        ModHandler.addShapedRecipe("piston_steel", new ItemStack(Blocks.PISTON, 2), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', OrePrefix.stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Steel));

        ModHandler.addShapedRecipe("piston_aluminium", new ItemStack(Blocks.PISTON, 4), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', OrePrefix.stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Aluminium));

        ModHandler.addShapedRecipe("piston_titanium", new ItemStack(Blocks.PISTON, 8), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', OrePrefix.stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Titanium));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Gold, 2)
                .outputs(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1))
                .circuitMeta(2).duration(200).EUt(4).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Iron, 2)
                .outputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1))
                .circuitMeta(2).duration(200).EUt(4).buildAndRegister();

    }
}
