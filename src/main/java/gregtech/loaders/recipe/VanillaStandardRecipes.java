package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import static gregtech.api.GTValues.*;
import static gregtech.api.items.OreDictNames.cobblestone;
import static gregtech.api.items.OreDictNames.stoneCobble;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

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
        metalRecipes();
        miscRecipes();
        mixingRecipes();
        dyeRecipes();
    }

    /**
     * + Adds compression recipes for vanilla items
     */
    private static void compressingRecipes() {
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputItem(OrePrefix.plate, Materials.Stone, 9)
                .outputs(new ItemStack(Blocks.STONE))
                .buildAndRegister();

        // todo autogenerate 2x2 recipes?
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Blocks.SAND, 4))
                .outputs(new ItemStack(Blocks.SANDSTONE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Blocks.SAND, 4, 1))
                .outputs(new ItemStack(Blocks.RED_SANDSTONE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Items.BRICK, 4))
                .outputs(new ItemStack(Blocks.BRICK_BLOCK))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Items.NETHERBRICK, 4))
                .outputs(new ItemStack(Blocks.NETHER_BRICK))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Blocks.SNOW))
                .outputs(new ItemStack(Blocks.ICE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Items.CLAY_BALL, 4))
                .outputs(new ItemStack(Blocks.CLAY))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).volts(2)
                .inputs(new ItemStack(Items.GLOWSTONE_DUST, 4))
                .outputs(new ItemStack(Blocks.GLOWSTONE))
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().inputs(new ItemStack(Blocks.ICE, 2, GTValues.W))
                .outputs(new ItemStack(Blocks.PACKED_ICE)).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().inputItem(OrePrefix.dust, Materials.Ice, 1)
                .outputs(new ItemStack(Blocks.ICE))
                .buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.WHEAT, 9))
                .circuitMeta(9)
                .outputs(new ItemStack(Blocks.HAY_BLOCK))
                .duration(200).volts(2)
                .buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MELON, 9))
                .circuitMeta(9)
                .outputs(new ItemStack(Blocks.MELON_BLOCK))
                .duration(200).volts(2)
                .buildAndRegister();
    }

    /**
     * + Adds new glass related recipes
     * + Adds steam age manual glass recipes
     * - Removes some glass related recipes based on configs
     */
    private static void glassRecipes() {
        ModHandler.addShapedRecipe("glass_dust_hammer", OreDictUnifier.get(dust, Materials.Glass), "hG", 'G',
                new ItemStack(Blocks.GLASS, 1, GTValues.W));

        ModHandler.addShapelessRecipe("glass_dust_handcrafting", OreDictUnifier.get(dust, Glass), "dustSand",
                "dustFlint");

        ModHandler.addShapedRecipe("quartz_sand", OreDictUnifier.get(OrePrefix.dust, Materials.QuartzSand), "S", "m",
                'S', new ItemStack(Blocks.SAND));

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SAND))
                .outputItem(OrePrefix.dust, Materials.QuartzSand)
                .duration(30).buildAndRegister();

        ModHandler.addShapelessRecipe("glass_dust_flint", OreDictUnifier.get(OrePrefix.dust, Materials.Glass),
                new UnificationEntry(OrePrefix.dust, Materials.QuartzSand),
                new UnificationEntry(OrePrefix.dustTiny, Materials.Flint));

        MIXER_RECIPES.recipeBuilder().duration(160).volts(VA[ULV])
                .inputItem(dustSmall, Materials.Flint)
                .inputItem(dust, Materials.Quartzite, 4)
                .outputItem(dust, Materials.Glass, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .inputItem(dustSmall, Materials.Flint)
                .inputItem(dust, Materials.QuartzSand, 4)
                .outputItem(dust, Materials.Glass, 4)
                .buildAndRegister();

        ARC_FURNACE_RECIPES.recipeBuilder().duration(20).volts(VA[LV])
                .inputs(new ItemStack(Blocks.SAND, 1))
                .outputs(new ItemStack(Blocks.GLASS, 2))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(80).volts(VA[LV])
                .inputItem(dust, Materials.Glass)
                .notConsumable(SHAPE_MOLD_BLOCK.getStackForm())
                .outputs(new ItemStack(Blocks.GLASS, 1))
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(64).volts(4)
                .inputItem(dust, Materials.Glass)
                .notConsumable(MetaItems.SHAPE_MOLD_BOTTLE)
                .outputs(new ItemStack(Items.GLASS_BOTTLE))
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder().duration(32).volts(16)
                .inputItem(dust, Materials.Glass)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_BOTTLE)
                .outputs(new ItemStack(Items.GLASS_BOTTLE))
                .buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(12).volts(4)
                .fluidInputs(Materials.Glass.getFluid(L))
                .notConsumable(MetaItems.SHAPE_MOLD_BOTTLE)
                .outputs(new ItemStack(Items.GLASS_BOTTLE))
                .buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(12).volts(4)
                .fluidInputs(Glass.getFluid(L))
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputs(new ItemStack(Blocks.GLASS))
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(120).volts(16)
                .inputItem(dust, Materials.Glass)
                .notConsumable(SHAPE_MOLD_BLOCK.getStackForm())
                .outputs(new ItemStack(Blocks.GLASS, 1))
                .buildAndRegister();

        for (int i = 0; i < 16; i++) {
            // nerf glass panes
            if (ConfigHolder.recipes.hardGlassRecipes) {
                ModHandler.removeRecipeByOutput(new ItemStack(Blocks.STAINED_GLASS_PANE, 16, i));
            }

            ModHandler.addShapedRecipe("stained_glass_pane_" + i, new ItemStack(Blocks.STAINED_GLASS_PANE, 2, i), "sG",
                    'G', new ItemStack(Blocks.STAINED_GLASS, 1, i));

            CUTTER_RECIPES.recipeBuilder().duration(50).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.STAINED_GLASS, 3, i))
                    .outputs(new ItemStack(Blocks.STAINED_GLASS_PANE, 8, i))
                    .buildAndRegister();
        }

        if (ConfigHolder.recipes.hardGlassRecipes)
            ModHandler.removeRecipeByOutput(new ItemStack(Blocks.GLASS_PANE, 16));

        ModHandler.addShapedRecipe("glass_pane", new ItemStack(Blocks.GLASS_PANE, 2), "sG", 'G',
                new ItemStack(Blocks.GLASS));

        CUTTER_RECIPES.recipeBuilder().duration(50).volts(VA[ULV])
                .inputs(new ItemStack(Blocks.GLASS, 3))
                .outputs(new ItemStack(Blocks.GLASS_PANE, 8))
                .buildAndRegister();
    }

    /**
     * Adds smashing related recipes for vanilla blocks and items
     */
    private static void smashingRecipes() {
        ModHandler.addShapedRecipe("cobblestone_hammer", new ItemStack(Blocks.COBBLESTONE), "h", "C", 'C',
                new UnificationEntry(OrePrefix.stone));

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputItem(stone.name(), 1)
                .outputs(new ItemStack(Blocks.COBBLESTONE, 1)).volts(16).duration(10)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputItem(cobblestone.name(), 1)
                .outputs(new ItemStack(Blocks.GRAVEL, 1)).volts(16).duration(10)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GRAVEL, 1))
                .outputs(new ItemStack(Blocks.SAND)).volts(16).duration(10)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GRAVEL, 1))
                .outputItem(dust, Stone).outputsRolled(1000, 1000, new ItemStack(Items.FLINT))
                .duration(400)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SANDSTONE, 1, W))
                .outputs(new ItemStack(Blocks.SAND, 1)).volts(2).duration(400).buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_SANDSTONE, 1, W))
                .outputs(new ItemStack(Blocks.SAND, 1, 1)).volts(2).duration(400).buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONEBRICK))
                .outputs(new ItemStack(Blocks.STONEBRICK, 1, 2)).volts(2).duration(400).buildAndRegister();

        if (!ConfigHolder.recipes.disableManualCompression) {
            ModHandler.addShapelessRecipe("nether_quartz_block_to_nether_quartz", new ItemStack(Items.QUARTZ, 4),
                    Blocks.QUARTZ_BLOCK);
        }
        ModHandler.addShapelessRecipe("clay_block_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Clay), 'm',
                Blocks.CLAY);
        ModHandler.addShapelessRecipe("clay_ball_to_dust", OreDictUnifier.get(OrePrefix.dustSmall, Materials.Clay), 'm',
                Items.CLAY_BALL);
        ModHandler.addShapelessRecipe("brick_block_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Brick), 'm',
                Blocks.BRICK_BLOCK);
        ModHandler.addShapelessRecipe("brick_to_dust", OreDictUnifier.get(OrePrefix.dustSmall, Materials.Brick), 'm',
                Items.BRICK);
        ModHandler.addShapelessRecipe("wheat_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Wheat), 'm',
                Items.WHEAT);
        ModHandler.addShapelessRecipe("gravel_to_flint", new ItemStack(Items.FLINT), 'm', Blocks.GRAVEL);
        ModHandler.addShapelessRecipe("bone_to_bone_meal", new ItemStack(Items.DYE, 4, 15), 'm', Items.BONE);
        ModHandler.addShapelessRecipe("blaze_rod_to_powder", new ItemStack(Items.BLAZE_POWDER, 3), 'm',
                Items.BLAZE_ROD);

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage()))
                .outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Cocoa, 1))
                .duration(400)
                .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.REEDS, 1))
                .outputs(new ItemStack(Items.SUGAR, 1))
                .duration(400)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.MELON_BLOCK, 1, 0))
                .outputs(new ItemStack(Items.MELON, 8, 0))
                .outputsRolled(8000, 500, new ItemStack(Items.MELON_SEEDS, 1))
                .duration(400)
                .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.PUMPKIN, 1, 0))
                .outputs(new ItemStack(Items.PUMPKIN_SEEDS, 4, 0))
                .duration(400)
                .buildAndRegister();

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MELON, 1, 0))
                .outputs(new ItemStack(Items.MELON_SEEDS, 1, 0))
                .duration(400)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputItem("wool")
                .outputs(new ItemStack(Items.STRING, 1))
                .outputsRolled(9000, 0, new ItemStack(Items.STRING, 1))
                .outputsRolled(5000, 0, new ItemStack(Items.STRING, 1))
                .outputsRolled(2000, 0, new ItemStack(Items.STRING, 1))
                .duration(200)
                .buildAndRegister();
    }

    /**
     * + Adds Laser Engraver recipes for vanilla blocks
     */
    private static void engraverRecipes() {
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SANDSTONE, 1, 2))
                .notConsumable(craftingLens, MarkerMaterials.Color.White)
                .outputs(new ItemStack(Blocks.SANDSTONE, 1, 1))
                .duration(50).volts(16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 2))
                .notConsumable(craftingLens, MarkerMaterials.Color.White)
                .outputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 1))
                .duration(50).volts(16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE))
                .notConsumable(craftingLens, MarkerMaterials.Color.White)
                .outputs(new ItemStack(Blocks.STONEBRICK, 1, 3))
                .duration(50).volts(16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.QUARTZ_BLOCK))
                .notConsumable(craftingLens, MarkerMaterials.Color.White)
                .outputs(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 1))
                .duration(50).volts(16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.PURPUR_BLOCK))
                .notConsumable(craftingLens, MarkerMaterials.Color.White)
                .outputs(new ItemStack(Blocks.PURPUR_PILLAR, 1))
                .duration(50).volts(16).buildAndRegister();
    }

    /**
     * + Adds new recipes for wood related items and blocks
     */
    private static void woodRecipes() {
        MACERATOR_RECIPES.recipeBuilder()
                .inputItem(log, Wood)
                .outputItem(dust, Wood, 6).outputItemRoll(dust, Wood, 8000, 680)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputItem(plank, Wood)
                .outputItem(stick, Wood, 2)
                .duration(10).volts(VA[ULV])
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputItem(log, Wood)
                .outputItem(stickLong, Wood, 4)
                .outputItem(dust, Wood, 2)
                .duration(160).volts(VA[ULV])
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputItem("treeSapling")
                .outputs(new ItemStack(Items.STICK))
                .outputItem(dustTiny, Wood)
                .duration(16).volts(VA[ULV])
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputItem(slab, Wood)
                .outputs(new ItemStack(Items.BOWL))
                .outputItem(dustSmall, Wood)
                .duration(50).volts(VA[ULV])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plank, Wood, 6)
                .inputs(new ItemStack(Items.BOOK, 3))
                .outputs(new ItemStack(Blocks.BOOKSHELF))
                .duration(100).volts(4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plank, Wood, 3).circuitMeta(3)
                .outputs(new ItemStack(Blocks.TRAPDOOR, 2))
                .duration(100).volts(4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plank, Wood, 8)
                .outputs(new ItemStack(Blocks.CHEST))
                .duration(100).volts(4).circuitMeta(8)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.COAL, 1, W))
                .inputItem(stick, Wood, 1)
                .outputs(new ItemStack(Blocks.TORCH, 4))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Coal)
                .inputItem(stick, Wood, 1)
                .outputs(new ItemStack(Blocks.TORCH, 4))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Charcoal)
                .inputItem(stick, Wood, 1)
                .outputs(new ItemStack(Blocks.TORCH, 4))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(gem, Coke)
                .inputItem(stick, Wood, 1)
                .outputs(new ItemStack(Blocks.TORCH, 8))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Coke)
                .inputItem(stick, Wood, 1)
                .outputs(new ItemStack(Blocks.TORCH, 8))
                .duration(100).volts(1).buildAndRegister();

        ModHandler.addShapedRecipe("sticky_resin_torch", new ItemStack(Blocks.TORCH, 3), "X", "Y", 'X',
                MetaItems.STICKY_RESIN, 'Y', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_sulfur", new ItemStack(Blocks.TORCH, 2), "C", "S", 'C',
                new UnificationEntry(OrePrefix.dust, Materials.Sulfur), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_phosphorus", new ItemStack(Blocks.TORCH, 6), "C", "S", 'C',
                new UnificationEntry(OrePrefix.dust, Materials.Phosphorus), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_coal_dust", new ItemStack(Blocks.TORCH, 4), "C", "S", 'C',
                new UnificationEntry(OrePrefix.dust, Materials.Coal), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_charcoal_dust", new ItemStack(Blocks.TORCH, 4), "C", "S", 'C',
                new UnificationEntry(OrePrefix.dust, Materials.Charcoal), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_coke", new ItemStack(Blocks.TORCH, 8), "C", "S", 'C',
                new UnificationEntry(OrePrefix.gem, Materials.Coke), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_coke_dust", new ItemStack(Blocks.TORCH, 8), "C", "S", 'C',
                new UnificationEntry(OrePrefix.dust, Materials.Coke), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_creosote", new ItemStack(Blocks.TORCH, 16), "WB", "S ", 'W',
                OreDictUnifier.get("wool"), 'S', new UnificationEntry(stick, Wood), 'B',
                FluidUtil.getFilledBucket(Creosote.getFluid(1000)));

        ASSEMBLER_RECIPES.recipeBuilder().volts(1).inputItem(dust, Redstone).inputItem(stick, Wood)
                .outputs(new ItemStack(Blocks.REDSTONE_TORCH, 1)).duration(100).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).inputItem(stick, Wood).inputItem(dust, Sulfur)
                .outputs(new ItemStack(Blocks.TORCH, 2)).duration(100).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).inputItem(stick, Wood).inputItem(dust, Phosphorus)
                .outputs(new ItemStack(Blocks.TORCH, 6)).duration(100).circuitMeta(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(40).circuitMeta(7).inputs(new ItemStack(Items.STICK, 7))
                .outputs(new ItemStack(Blocks.LADDER, ConfigHolder.recipes.hardWoodRecipes ? 2 : 3)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(4).duration(100).inputs(new ItemStack(Items.MINECART))
                .inputs(OreDictUnifier.get("chestWood")).outputs(new ItemStack(Items.CHEST_MINECART))
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).duration(100).inputs(new ItemStack(Items.MINECART))
                .inputs(new ItemStack(Blocks.FURNACE)).outputs(new ItemStack(Items.FURNACE_MINECART))
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).duration(100).inputs(new ItemStack(Items.MINECART))
                .inputs(new ItemStack(Blocks.TNT)).outputs(new ItemStack(Items.TNT_MINECART)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).duration(100).inputs(new ItemStack(Items.MINECART))
                .inputs(new ItemStack(Blocks.HOPPER)).outputs(new ItemStack(Items.HOPPER_MINECART)).buildAndRegister();
    }

    /**
     * + Adds cutting recipes for vanilla blocks
     */
    private static void cuttingRecipes() {
        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SANDSTONE))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2, 1))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2, 3))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.BRICK_BLOCK))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2, 4))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONEBRICK))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2, 5))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.NETHER_BRICK))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2, 6))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 1))
                .outputs(new ItemStack(Blocks.STONE_SLAB, 2, 7))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 0))
                .outputs(new ItemStack(Blocks.STONE_SLAB2, 2, 0))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.PURPUR_BLOCK, 1, 0))
                .outputs(new ItemStack(Blocks.PURPUR_SLAB, 2, 0))
                .duration(25).volts(VA[ULV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SNOW, 1))
                .outputs(new ItemStack(Blocks.SNOW_LAYER, 16))
                .duration(25).volts(VA[ULV]).buildAndRegister();
    }

    /**
     * + Adds dying and cleaning recipes for vanilla blocks
     */
    private static void dyingCleaningRecipes() {
        for (int i = 0; i < 16; i++) {
            MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.SAND, 4))
                    .inputs(new ItemStack(Blocks.GRAVEL, 4))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L))
                    .outputs(new ItemStack(Blocks.CONCRETE_POWDER, 8, i))
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.CONCRETE_POWDER, 1, i))
                    .fluidInputs(Water.getFluid(1000))
                    .outputs(new ItemStack(Blocks.CONCRETE, 1, i))
                    .buildAndRegister();

            if (i != 0) {
                CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                        .inputs(new ItemStack(Blocks.CONCRETE))
                        .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L / 8))
                        .outputs(new ItemStack(Blocks.CONCRETE, 1, i))
                        .buildAndRegister();
            }

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.HARDENED_CLAY))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L / 8))
                    .outputs(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i))
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.GLASS))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L / 8))
                    .outputs(new ItemStack(Blocks.STAINED_GLASS, 1, i))
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.GLASS_PANE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L / 8))
                    .outputs(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, i))
                    .buildAndRegister();

            if (i != 0) {
                CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                        .inputs(new ItemStack(Blocks.WOOL))
                        .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L))
                        .outputs(new ItemStack(Blocks.WOOL, 1, i))
                        .buildAndRegister();
            }

            CUTTER_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                    .inputs(new ItemStack(Blocks.WOOL, 1, i))
                    .outputs(new ItemStack(Blocks.CARPET, 2, i))
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(20).volts(VA[ULV])
                    .circuitMeta(6)
                    .inputs(new ItemStack(Items.STICK))
                    .inputs(new ItemStack(Blocks.WOOL, 6, i))
                    .outputs(new ItemStack(Items.BANNER, 1, 16 - 1 - i))
                    .buildAndRegister();
        }

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Blocks.WOOL, 1)
                .fluidInputs(Chlorine.getFluid(50))
                .outputItem(Blocks.WOOL)
                .duration(400).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Blocks.CARPET, 1)
                .fluidInputs(Chlorine.getFluid(25))
                .outputItem(Blocks.CARPET)
                .duration(400).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Blocks.STAINED_HARDENED_CLAY, 1)
                .fluidInputs(Chlorine.getFluid(50))
                .outputItem(Blocks.HARDENED_CLAY)
                .duration(400).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Blocks.STAINED_GLASS, 1)
                .fluidInputs(Chlorine.getFluid(50))
                .outputItem(Blocks.GLASS)
                .duration(400).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Blocks.STAINED_GLASS_PANE, 1)
                .fluidInputs(Chlorine.getFluid(20))
                .outputItem(Blocks.GLASS_PANE)
                .duration(400).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Blocks.CONCRETE, 1)
                .fluidInputs(Chlorine.getFluid(20))
                .outputItem(Blocks.CONCRETE)
                .duration(400).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STICKY_PISTON))
                .fluidInputs(Chlorine.getFluid(10))
                .outputs(new ItemStack(Blocks.PISTON))
                .duration(30).volts(VA[LV]).buildAndRegister();
    }

    /**
     * + Adds more redstone related recipes
     */
    private static void redstoneRecipes() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(STICKY_RESIN.getStackForm())
                .inputs(new ItemStack(Blocks.PISTON))
                .outputs(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem("slimeball", 1)
                .inputs(new ItemStack(Blocks.PISTON))
                .outputs(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.PISTON))
                .fluidInputs(Glue.getFluid(100))
                .outputs(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Wood, 2)
                .inputItem(ring, Iron, 2)
                .outputs(new ItemStack(Blocks.TRIPWIRE_HOOK, 1))
                .duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Wood, 2)
                .inputItem(ring, WroughtIron, 2)
                .outputs(new ItemStack(Blocks.TRIPWIRE_HOOK, 1))
                .duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Redstone, 4)
                .inputItem(dust, Glowstone, 4)
                .outputs(new ItemStack(Blocks.REDSTONE_LAMP))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH, 2))
                .inputItem(dust, Redstone)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(new ItemStack(Items.REPEATER))
                .duration(100).volts(10).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH, 3))
                .inputItem(gem, NetherQuartz)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(new ItemStack(Items.COMPARATOR))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH, 3))
                .inputItem(gem, CertusQuartz)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(new ItemStack(Items.COMPARATOR))
                .duration(100).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH, 3))
                .inputItem(gem, Quartzite)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(new ItemStack(Items.COMPARATOR))
                .duration(100).volts(1).buildAndRegister();

        if (ConfigHolder.recipes.hardRedstoneRecipes)
            return;

        ModHandler.addShapedRecipe("piston_bronze", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Bronze));

        ModHandler.addShapedRecipe("piston_steel", new ItemStack(Blocks.PISTON, 2), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Steel));

        ModHandler.addShapedRecipe("piston_aluminium", new ItemStack(Blocks.PISTON, 4), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Aluminium));

        ModHandler.addShapedRecipe("piston_titanium", new ItemStack(Blocks.PISTON, 8), "WWW", "CBC", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', stoneCobble,
                'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                'B', new UnificationEntry(OrePrefix.ingot, Materials.Titanium));

        ModHandler.addShapedRecipe("sticky_piston_resin", new ItemStack(Blocks.STICKY_PISTON), "h", "R", "P",
                'R', STICKY_RESIN.getStackForm(),
                'P', new ItemStack(Blocks.PISTON));

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(16).inputItem(plate, Iron)
                .inputs(new ItemStack(Blocks.PLANKS, 3, GTValues.W)).inputs(new ItemStack(Blocks.COBBLESTONE, 4))
                .inputItem(dust, Redstone).outputs(new ItemStack(Blocks.PISTON)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(16).inputItem(plate, Bronze)
                .inputs(new ItemStack(Blocks.PLANKS, 3, GTValues.W)).inputs(new ItemStack(Blocks.COBBLESTONE, 4))
                .inputItem(dust, Redstone).outputs(new ItemStack(Blocks.PISTON)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(16).inputItem(plate, Steel)
                .inputs(new ItemStack(Blocks.PLANKS, 3, GTValues.W)).inputs(new ItemStack(Blocks.COBBLESTONE, 4))
                .inputItem(dust, Redstone).outputs(new ItemStack(Blocks.PISTON, 2)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(16).inputItem(plate, Aluminium)
                .inputs(new ItemStack(Blocks.PLANKS, 3, GTValues.W)).inputs(new ItemStack(Blocks.COBBLESTONE, 4))
                .inputItem(dust, Redstone).outputs(new ItemStack(Blocks.PISTON, 4)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(16).inputItem(plate, Titanium)
                .inputs(new ItemStack(Blocks.PLANKS, 3, GTValues.W)).inputs(new ItemStack(Blocks.COBBLESTONE, 4))
                .inputItem(dust, Redstone).outputs(new ItemStack(Blocks.PISTON, 8)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Gold, 2)
                .outputs(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1))
                .circuitMeta(3).duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Iron, 2)
                .outputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1))
                .circuitMeta(2).duration(100).volts(4).buildAndRegister();

        ModHandler.addShapedRecipe("comparator_certus", new ItemStack(Items.COMPARATOR), " T ", "TQT", "SSS",
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'Q', new UnificationEntry(OrePrefix.gem, Materials.CertusQuartz),
                'S', new UnificationEntry(OrePrefix.stone));

        ModHandler.addShapedRecipe("comparator_quartzite", new ItemStack(Items.COMPARATOR), " T ", "TQT", "SSS",
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'Q', new UnificationEntry(OrePrefix.gem, Materials.Quartzite),
                'S', new UnificationEntry(OrePrefix.stone));

        ModHandler.addShapedRecipe("daylight_detector_certus", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "CCC",
                "PPP",
                'G', new ItemStack(Blocks.GLASS, 1, GTValues.W),
                'C', new UnificationEntry(gem, CertusQuartz),
                'P', new UnificationEntry(OrePrefix.slab, Materials.Wood));

        ModHandler.addShapedRecipe("daylight_detector_quartzite", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "CCC",
                "PPP",
                'G', new ItemStack(Blocks.GLASS, 1, GTValues.W),
                'C', new UnificationEntry(gem, Quartzite),
                'P', new UnificationEntry(OrePrefix.slab, Materials.Wood));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plank, Wood, 8)
                .inputItem(dust, Redstone)
                .outputs(new ItemStack(Blocks.NOTEBLOCK))
                .circuitMeta(9).duration(100).volts(16).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plank, Wood, 8)
                .inputItem(gem, Diamond)
                .outputs(new ItemStack(Blocks.JUKEBOX))
                .duration(100).volts(16).buildAndRegister();
    }

    /**
     * + Adds metal related recipes
     * + Adds horse armor and chainmail recipes
     * + Replaces minecart recipe
     */
    private static void metalRecipes() {
        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .inputItem(plate, Iron, 3)
                .outputs(new ItemStack(Items.BUCKET))
                .duration(100).volts(4)
                .buildAndRegister();

        if (!ConfigHolder.recipes.hardToolArmorRecipes) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(dust, Redstone)
                    .inputItem(plate, Iron, 4)
                    .circuitMeta(1)
                    .outputs(new ItemStack(Items.COMPASS))
                    .duration(100).volts(4).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(dust, Redstone)
                    .inputItem(plate, Gold, 4)
                    .outputs(new ItemStack(Items.CLOCK))
                    .duration(100).volts(4).buildAndRegister();
        }

        ModHandler.addShapedRecipe("iron_horse_armor", new ItemStack(Items.IRON_HORSE_ARMOR), "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.IRON_HELMET),
                'P', new UnificationEntry(plate, Materials.Iron),
                'C', new ItemStack(Items.IRON_CHESTPLATE),
                'L', new ItemStack(Items.IRON_LEGGINGS),
                'S', new UnificationEntry(screw, Materials.Iron));

        ModHandler.addShapedRecipe("golden_horse_armor", new ItemStack(Items.GOLDEN_HORSE_ARMOR), "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.GOLDEN_HELMET),
                'P', new UnificationEntry(plate, Materials.Gold),
                'C', new ItemStack(Items.GOLDEN_CHESTPLATE),
                'L', new ItemStack(Items.GOLDEN_LEGGINGS),
                'S', new UnificationEntry(screw, Materials.Gold));

        ModHandler.addShapedRecipe("diamond_horse_armor", new ItemStack(Items.DIAMOND_HORSE_ARMOR), "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.DIAMOND_HELMET),
                'P', new UnificationEntry(plate, Materials.Diamond),
                'C', new ItemStack(Items.DIAMOND_CHESTPLATE),
                'L', new ItemStack(Items.DIAMOND_LEGGINGS),
                'S', new UnificationEntry(bolt, Materials.Diamond));

        ModHandler.addShapedRecipe("chainmail_helmet", new ItemStack(Items.CHAINMAIL_HELMET), "PPP", "PhP",
                'P', new UnificationEntry(OrePrefix.ring, Materials.Iron));

        ModHandler.addShapedRecipe("chainmail_chestplate", new ItemStack(Items.CHAINMAIL_CHESTPLATE), "PhP", "PPP",
                "PPP",
                'P', new UnificationEntry(OrePrefix.ring, Materials.Iron));

        ModHandler.addShapedRecipe("chainmail_leggings", new ItemStack(Items.CHAINMAIL_LEGGINGS), "PPP", "PhP", "P P",
                'P', new UnificationEntry(OrePrefix.ring, Materials.Iron));

        ModHandler.addShapedRecipe("chainmail_boots", new ItemStack(Items.CHAINMAIL_BOOTS), "P P", "PhP",
                'P', new UnificationEntry(OrePrefix.ring, Materials.Iron));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Iron, 7)
                .outputs(new ItemStack(Items.CAULDRON, 1))
                .circuitMeta(7)
                .duration(700).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iron, 3)
                .outputs(new ItemStack(Blocks.IRON_BARS, 4))
                .circuitMeta(3)
                .duration(300).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Iron, 4)
                .circuitMeta(4)
                .outputs(new ItemStack(Blocks.IRON_TRAPDOOR))
                .duration(100).volts(16).buildAndRegister();

        if (!ConfigHolder.recipes.hardAdvancedIronRecipes) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(plate, Iron, 6)
                    .circuitMeta(6)
                    .outputs(new ItemStack(Items.IRON_DOOR, 3))
                    .duration(100).volts(16).buildAndRegister();
        }

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:minecart"));
        ModHandler.addShapedRecipe("minecart_iron", new ItemStack(Items.MINECART), " h ", "PwP", "WPW",
                'W', MetaItems.IRON_MINECART_WHEELS.getStackForm(),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron));
        ModHandler.addShapedRecipe("minecart_steel", new ItemStack(Items.MINECART), " h ", "PwP", "WPW",
                'W', MetaItems.STEEL_MINECART_WHEELS.getStackForm(),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Steel));
    }

    /**
     * Adds miscellaneous vanilla recipes
     * Adds vanilla fluid solidification recipes
     * Adds anvil recipes
     * Adds Slime to rubber
     * Adds alternative gunpowder recipes
     * Adds polished stone variant autoclave recipes
     */
    private static void miscRecipes() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PAPER, 3))
                .inputs(new ItemStack(Items.LEATHER))
                .fluidInputs(Glue.getFluid(20))
                .outputs(new ItemStack(Items.BOOK))
                .duration(32).volts(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PAPER, 3))
                .inputItem(foil, PolyvinylChloride)
                .fluidInputs(Glue.getFluid(20))
                .outputs(new ItemStack(Items.BOOK))
                .duration(20).volts(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PAPER, 8))
                .inputs(new ItemStack(Items.COMPASS))
                .outputs(new ItemStack(Items.MAP))
                .duration(100).volts(VA[ULV]).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .inputItem(dust, Netherrack)
                .notConsumable(SHAPE_MOLD_INGOT)
                .outputs(new ItemStack(Items.NETHERBRICK))
                .duration(200).volts(2).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.CLAY_BALL))
                .notConsumable(SHAPE_MOLD_INGOT)
                .outputs(new ItemStack(Items.BRICK))
                .duration(200).volts(2).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.STRING, 4, W))
                .inputs(new ItemStack(Items.SLIME_BALL, 1, W))
                .outputs(new ItemStack(Items.LEAD, 2))
                .duration(100).volts(2).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.LEATHER))
                .inputs(new ItemStack(Items.LEAD))
                .fluidInputs(Glue.getFluid(100))
                .outputs(new ItemStack(Items.NAME_TAG))
                .duration(100).volts(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.STRING, 3, W))
                .inputItem(stick, Wood, 3)
                .outputs(new ItemStack(Items.BOW, 1))
                .duration(100).volts(4).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(128).volts(4).notConsumable(SHAPE_MOLD_BALL)
                .fluidInputs(Water.getFluid(250)).outputs(new ItemStack(Items.SNOWBALL)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(128).volts(4).notConsumable(SHAPE_MOLD_BALL)
                .fluidInputs(DistilledWater.getFluid(250)).outputs(new ItemStack(Items.SNOWBALL)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(512).volts(4).notConsumable(SHAPE_MOLD_BLOCK)
                .fluidInputs(Water.getFluid(1000)).outputs(new ItemStack(Blocks.SNOW)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(512).volts(4).notConsumable(SHAPE_MOLD_BLOCK)
                .fluidInputs(DistilledWater.getFluid(1000)).outputs(new ItemStack(Blocks.SNOW)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(1024).volts(16).notConsumable(SHAPE_MOLD_BLOCK)
                .fluidInputs(Lava.getFluid(1000)).outputs(new ItemStack(Blocks.OBSIDIAN)).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(1680).volts(16).notConsumable(SHAPE_MOLD_ANVIL)
                .fluidInputs(Iron.getFluid(L * 31)).outputs(new ItemStack(Blocks.ANVIL)).buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder().inputItem(ingot, Iron, 31).notConsumable(SHAPE_MOLD_ANVIL)
                .outputs(new ItemStack(Blocks.ANVIL)).duration(1680).volts(16).buildAndRegister();

        ModHandler.addSmeltingRecipe(new ItemStack(Items.SLIME_BALL), STICKY_RESIN.getStackForm(), 0.3f);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.STRING, 4))
                .circuitMeta(4)
                .outputs(new ItemStack(Blocks.WOOL, 1, 0))
                .duration(100).volts(4).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cobblestone.name(), 1)
                .inputs(new ItemStack(Blocks.VINE))
                .outputs(new ItemStack(Blocks.MOSSY_COBBLESTONE))
                .duration(40).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONEBRICK, 1, 0))
                .inputs(new ItemStack(Blocks.VINE))
                .outputs(new ItemStack(Blocks.STONEBRICK, 1, 1))
                .duration(40).volts(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7).inputItem(stoneCobble.name(), 6)
                .outputs(new ItemStack(Blocks.STONE_STAIRS, 4)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 6)).outputs(new ItemStack(Blocks.BRICK_STAIRS, 4))
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7)
                .inputs(new ItemStack(Blocks.STONEBRICK, 6, GTValues.W))
                .outputs(new ItemStack(Blocks.STONE_BRICK_STAIRS, 4)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7)
                .inputs(new ItemStack(Blocks.NETHER_BRICK, 6)).outputs(new ItemStack(Blocks.NETHER_BRICK_STAIRS, 4))
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7)
                .inputs(new ItemStack(Blocks.SANDSTONE, 6))
                .outputs(new ItemStack(Blocks.SANDSTONE_STAIRS, 4)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7)
                .inputs(new ItemStack(Blocks.QUARTZ_BLOCK, 6)).outputs(new ItemStack(Blocks.QUARTZ_STAIRS, 4))
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(7)
                .inputs(new ItemStack(Blocks.PURPUR_BLOCK, 6)).outputs(new ItemStack(Blocks.PURPUR_STAIRS, 4))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(1).duration(100).circuitMeta(2)
                .inputs(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0)).outputs(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 2))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(4).inputs(new ItemStack(Blocks.STONE))
                .outputs(new ItemStack(Blocks.STONEBRICK, 1, 0)).circuitMeta(4).duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).inputs(new ItemStack(Blocks.END_STONE))
                .outputs(new ItemStack(Blocks.END_BRICKS, 1, 0)).circuitMeta(4).duration(50).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(4).inputs(new ItemStack(Blocks.SANDSTONE))
                .outputs(new ItemStack(Blocks.SANDSTONE, 1, 2)).circuitMeta(1).duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).inputs(new ItemStack(Blocks.RED_SANDSTONE))
                .outputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 2)).circuitMeta(1).duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).inputs(new ItemStack(Blocks.SANDSTONE, 1, 2))
                .outputs(new ItemStack(Blocks.SANDSTONE, 1, 0)).circuitMeta(1).duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().volts(4).inputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 2))
                .outputs(new ItemStack(Blocks.RED_SANDSTONE, 1, 0)).circuitMeta(1).duration(50).buildAndRegister();

        CANNER_RECIPES.recipeBuilder().volts(4).duration(100).inputs(new ItemStack(Blocks.PUMPKIN))
                .inputs(new ItemStack(Blocks.TORCH)).outputs(new ItemStack(Blocks.LIT_PUMPKIN)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().volts(4).duration(40).inputs(new ItemStack(Items.PRISMARINE_CRYSTALS, 5))
                .inputs(new ItemStack(Items.PRISMARINE_SHARD, 4)).outputs(new ItemStack(Blocks.SEA_LANTERN))
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().volts(4).duration(40).inputs(new ItemStack(Items.NETHERBRICK, 2))
                .inputs(new ItemStack(Items.NETHER_WART, 2)).outputs(new ItemStack(Blocks.RED_NETHER_BRICK))
                .buildAndRegister();

        if (!ConfigHolder.recipes.hardMiscRecipes) {
            ASSEMBLER_RECIPES.recipeBuilder().duration(80).volts(6).circuitMeta(4).inputItem("plankWood", 4)
                    .outputs(new ItemStack(Blocks.CRAFTING_TABLE)).buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().circuitMeta(8).inputItem(stoneCobble.name(), 8)
                    .outputs(new ItemStack(Blocks.FURNACE)).duration(100).volts(VA[ULV]).buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().inputs(new ItemStack(Blocks.OBSIDIAN, 4)).inputItem(gem, Diamond, 2)
                    .inputs(new ItemStack(Items.BOOK)).outputs(new ItemStack(Blocks.ENCHANTING_TABLE)).duration(100)
                    .volts(VA[ULV]).buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[LV]).circuitMeta(1)
                    .inputs(new ItemStack(Blocks.COBBLESTONE, 7)).inputs(new ItemStack(Items.BOW))
                    .inputItem(dust, Redstone)
                    .outputs(new ItemStack(Blocks.DISPENSER)).buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[LV]).circuitMeta(2)
                    .inputs(new ItemStack(Blocks.COBBLESTONE, 7)).inputItem(dust, Redstone)
                    .outputs(new ItemStack(Blocks.DROPPER)).buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[LV]).inputs(new ItemStack(Blocks.COBBLESTONE, 6))
                    .inputItem(dust, Redstone, 2).inputItem(plate, NetherQuartz).outputs(new ItemStack(Blocks.OBSERVER))
                    .buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[LV]).inputs(new ItemStack(Blocks.COBBLESTONE, 6))
                    .inputItem(dust, Redstone, 2).inputItem(plate, CertusQuartz).outputs(new ItemStack(Blocks.OBSERVER))
                    .buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[LV]).inputs(new ItemStack(Blocks.COBBLESTONE, 6))
                    .inputItem(dust, Redstone, 2).inputItem(plate, Quartzite).outputs(new ItemStack(Blocks.OBSERVER))
                    .buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(4).inputs(new ItemStack(Blocks.OBSIDIAN, 8))
                    .inputs(new ItemStack(Items.ENDER_EYE)).outputs(new ItemStack(Blocks.ENDER_CHEST))
                    .buildAndRegister();
            ASSEMBLER_RECIPES.recipeBuilder().duration(30).volts(VA[ULV]).inputs(new ItemStack(Blocks.STONE_SLAB, 1, 0))
                    .inputs(new ItemStack(Items.STICK, 6)).outputs(new ItemStack(Items.ARMOR_STAND)).buildAndRegister();
        }

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(4).circuitMeta(3)
                .inputs(new ItemStack(Blocks.NETHER_BRICK))
                .outputs(new ItemStack(Blocks.NETHER_BRICK_FENCE)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV]).circuitMeta(6)
                .inputs(new ItemStack(Blocks.COBBLESTONE, 1, 0)).outputs(new ItemStack(Blocks.COBBLESTONE_WALL, 1, 0))
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV]).circuitMeta(6)
                .inputs(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, 0))
                .outputs(new ItemStack(Blocks.COBBLESTONE_WALL, 1, 1)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(4).inputs(new ItemStack(Items.CHORUS_FRUIT_POPPED))
                .inputs(new ItemStack(Items.BLAZE_ROD)).outputs(new ItemStack(Blocks.END_ROD, 4)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV]).inputItem("chestWood", 1)
                .inputs(new ItemStack(Items.SHULKER_SHELL, 2)).outputs(new ItemStack(Blocks.PURPLE_SHULKER_BOX))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(4).circuitMeta(1).inputItem("wool", 1)
                .inputs(new ItemStack(Items.STICK, 8)).outputs(new ItemStack(Items.PAINTING)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(4).inputs(new ItemStack(Items.LEATHER))
                .inputs(new ItemStack(Items.STICK, 8)).outputs(new ItemStack(Items.ITEM_FRAME)).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(4).inputItem("plankWood", 6)
                .inputs(new ItemStack(Items.STICK))
                .circuitMeta(9).outputs(new ItemStack(Items.SIGN, 3)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(10).volts(2).inputs(new ItemStack(Items.BRICK, 3))
                .outputs(new ItemStack(Items.FLOWER_POT)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(30).volts(16).inputs(new ItemStack(Items.GHAST_TEAR))
                .inputs(new ItemStack(Items.ENDER_EYE)).outputs(new ItemStack(Items.END_CRYSTAL))
                .fluidInputs(Glass.getFluid(GTValues.L * 7)).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iron, 12)
                .inputItem(stick, Wood)
                .circuitMeta(1)
                .outputs(new ItemStack(Blocks.RAIL, 32))
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Gold, 12)
                .inputItem(stick, Wood)
                .inputItem(dust, Redstone)
                .circuitMeta(2)
                .outputs(new ItemStack(Blocks.GOLDEN_RAIL, 12))
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iron, 12)
                .inputItem(stick, Wood)
                .inputItem(dust, Redstone)
                .circuitMeta(5)
                .outputs(new ItemStack(Blocks.DETECTOR_RAIL, 12))
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iron, 12)
                .inputItem(stick, Wood, 2)
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH))
                .circuitMeta(5)
                .outputs(new ItemStack(Blocks.ACTIVATOR_RAIL, 12))
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Iron, 3)
                .inputItem(IRON_MINECART_WHEELS, 2)
                .outputs(new ItemStack(Items.MINECART))
                .duration(100).volts(20).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Steel, 3)
                .inputItem(STEEL_MINECART_WHEELS, 2)
                .outputs(new ItemStack(Items.MINECART))
                .duration(100).volts(20).buildAndRegister();

        ModHandler.addShapedRecipe("saddle", new ItemStack(Items.SADDLE), "LLL", "LCL", "RSR",
                'L', new ItemStack(Items.LEATHER),
                'C', new ItemStack(Blocks.CARPET, 1, GTValues.W),
                'R', new UnificationEntry(ring, Iron),
                'S', new ItemStack(Items.STRING));

        for (FluidStack fluidStack : new FluidStack[] { Water.getFluid(200), DistilledWater.getFluid(36) }) {
            AUTOCLAVE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.STONE, 1, 1))
                    .fluidInputs(fluidStack)
                    .outputs(new ItemStack(Blocks.STONE, 1, 2))
                    .duration(100).volts(VA[ULV]).buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.STONE, 1, 3))
                    .fluidInputs(fluidStack)
                    .outputs(new ItemStack(Blocks.STONE, 1, 4))
                    .duration(100).volts(VA[ULV]).buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.STONE, 1, 5))
                    .fluidInputs(fluidStack)
                    .outputs(new ItemStack(Blocks.STONE, 1, 6))
                    .duration(100).volts(VA[ULV]).buildAndRegister();
        }

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Clay)
                .fluidInputs(Water.getFluid(250))
                .outputs(new ItemStack(Items.CLAY_BALL))
                .duration(600).volts(24).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Clay)
                .fluidInputs(DistilledWater.getFluid(250))
                .outputs(new ItemStack(Items.CLAY_BALL))
                .duration(300).volts(24).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, Redstone, 9)
                .outputItem(block, Redstone)
                .duration(300).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, Bone, 9)
                .outputItem(block, Bone)
                .duration(300).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.CHORUS_FRUIT_POPPED, 4))
                .outputs(new ItemStack(Blocks.PURPUR_BLOCK, 4))
                .duration(300).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MAGMA_CREAM, 4))
                .outputs(new ItemStack(Blocks.MAGMA))
                .duration(300).volts(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.SLIME_BALL, 9))
                .outputs(new ItemStack(Blocks.SLIME_BLOCK))
                .duration(300).volts(2).buildAndRegister();

        if (ConfigHolder.recipes.harderRods) {
            LATHE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.COBBLESTONE))
                    .outputItem(stick, Stone, 1)
                    .outputItem(dustSmall, Stone, 2)
                    .duration(20).volts(VA[ULV])
                    .buildAndRegister();
            LATHE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.STONE))
                    .outputItem(stick, Stone, 1)
                    .outputItem(dustSmall, Stone, 2)
                    .duration(20).volts(VA[ULV])
                    .buildAndRegister();
        } else {
            LATHE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.COBBLESTONE))
                    .outputItem(stick, Stone, 2)
                    .duration(20).volts(VA[ULV])
                    .buildAndRegister();
            LATHE_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.STONE))
                    .outputItem(stick, Stone, 2)
                    .duration(20).volts(VA[ULV])
                    .buildAndRegister();
        }

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.NETHER_WART, 9))
                .circuitMeta(9)
                .outputs(new ItemStack(Blocks.NETHER_WART_BLOCK))
                .duration(200).volts(2).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PRISMARINE_SHARD, 4))
                .circuitMeta(4)
                .outputs(new ItemStack(Blocks.PRISMARINE))
                .duration(100).volts(2).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PRISMARINE_SHARD, 9))
                .circuitMeta(9)
                .outputs(new ItemStack(Blocks.PRISMARINE, 1, 1))
                .duration(200).volts(2).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .fluidInputs(Blaze.getFluid(L))
                .inputItem(gem, EnderPearl)
                .outputs(new ItemStack(Items.ENDER_EYE))
                .duration(50).volts(VA[HV]).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .inputItem(dust, Blaze, 4)
                .outputItem(stick, Blaze)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(Blocks.TRIPWIRE_HOOK)
                .inputItem(Blocks.CHEST)
                .outputItem(Blocks.TRAPPED_CHEST)
                .duration(200).volts(4).buildAndRegister();

        // All 16 bed colors
        for (int i = 0; i <= 15; i++) {
            addBedRecipe(i);
        }
    }

    private static void addBedRecipe(int meta) {
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.WOOL, 3, meta))
                .inputItem(plank, Wood, 3)
                .outputs(new ItemStack(Items.BED, 1, meta))
                .duration(100).volts(VA[ULV]).buildAndRegister();
    }

    /**
     * Adds various mixer recipes for vanilla items and blocks
     */
    private static void mixingRecipes() {
        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Coal)
                .inputItem(dust, Gunpowder)
                .inputItem(dust, Blaze)
                .outputs(new ItemStack(Items.FIRE_CHARGE, 3))
                .duration(100).volts(VA[LV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GRAVEL))
                .inputs(new ItemStack(Blocks.DIRT))
                .outputs(new ItemStack(Blocks.DIRT, 2, 1))
                .duration(100).volts(4).buildAndRegister();
    }

    private static void dyeRecipes() {
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 0))
                .outputs(new ItemStack(Items.DYE, 2, 1))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 1))
                .outputs(new ItemStack(Items.DYE, 2, 12))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 2))
                .outputs(new ItemStack(Items.DYE, 2, 13))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 3))
                .outputs(new ItemStack(Items.DYE, 2, 7))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 4))
                .outputs(new ItemStack(Items.DYE, 2, 1))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 5))
                .outputs(new ItemStack(Items.DYE, 2, 14))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 6))
                .outputs(new ItemStack(Items.DYE, 2, 7))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 7))
                .outputs(new ItemStack(Items.DYE, 2, 9))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.RED_FLOWER, 1, 8))
                .outputs(new ItemStack(Items.DYE, 2, 7))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.YELLOW_FLOWER, 1, 0))
                .outputs(new ItemStack(Items.DYE, 2, 11))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 0))
                .outputs(new ItemStack(Items.DYE, 3, 11))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 1))
                .outputs(new ItemStack(Items.DYE, 3, 13))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 4))
                .outputs(new ItemStack(Items.DYE, 3, 1))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.DOUBLE_PLANT, 1, 5))
                .outputs(new ItemStack(Items.DYE, 3, 9))
                .buildAndRegister();

        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.BEETROOT, 1))
                .outputs(new ItemStack(Items.DYE, 2, 1))
                .buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PRISMARINE_SHARD, 8))
                .fluidInputs(DyeBlack.getFluid(144))
                .outputs(new ItemStack(Blocks.PRISMARINE, 1, 2))
                .duration(20).volts(VA[ULV]).buildAndRegister();
    }
}
