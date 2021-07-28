package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class VanillaOverrideRecipes {

    public static void init() {
        plantRecipes();
        stoneRecipes();
        glassRecipes();
        redstoneRecipes();
        buildingBlockRecipes();
        metalRecipes();
        specialRecipes();
    }

    public static void stoneRecipes() {

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:furnace"));
        ModHandler.addShapedRecipe("furnace", new ItemStack(Blocks.FURNACE), "CCC", "CFC", "CCC", 'F', new ItemStack(Items.FLINT), 'C', new UnificationEntry(OrePrefix.cobblestone));
    }

    private static void plantRecipes() {
        if (ConfigHolder.vanillaRecipes.nerfStickCrafting)
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stick"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:lit_pumpkin"));
        ModHandler.addShapedRecipe("lit_pumpkin", new ItemStack(Blocks.LIT_PUMPKIN), "PT", "k ", 'P', new ItemStack(Blocks.PUMPKIN), 'T', new ItemStack(Blocks.TORCH));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:crafting_table"));
        ModHandler.addShapedRecipe("crafting_table", new ItemStack(Blocks.CRAFTING_TABLE), "FF", "WW", 'F', new ItemStack(Items.FLINT), 'W', new UnificationEntry(OrePrefix.log, Materials.Wood));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:ladder"));
        ModHandler.addShapedRecipe("ladder_0", new ItemStack(Blocks.LADDER), "STS", "SSS", "ShS", 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'T', new ItemStack(Items.STRING));
        ModHandler.addShapedRecipe("ladder_1", new ItemStack(Blocks.LADDER, 2), "SrS", "SRS", "ShS", 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'R', new UnificationEntry(OrePrefix.bolt, Materials.Wood));
        ModHandler.addShapedRecipe("ladder_2", new ItemStack(Blocks.LADDER, 4), "SdS", "SRS", "ShS", 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'R', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe("ladder_3", new ItemStack(Blocks.LADDER, 8), "SdS", "SRS", "ShS", 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'R', new UnificationEntry(OrePrefix.screw, Materials.Steel));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jukebox"));
        ModHandler.addShapedRecipe("jukebox", new ItemStack(Blocks.JUKEBOX), "LBL", "NRN", "LGL",
                'L', new UnificationEntry(OrePrefix.log, Materials.Wood),
                'B', new UnificationEntry(OrePrefix.bolt, Materials.Diamond),
                'N', new ItemStack(Blocks.NOTEBLOCK),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'G', new UnificationEntry(OrePrefix.gear, Materials.Iron)
        );
    }

    /**
     * + Adds Glass Handcrafting
     * - Removes Sand -> Glass Furnace Smelting
     */
    private static void glassRecipes() {
        ModHandler.addShapedRecipe("glass_dust_flint", OreDictUnifier.get(OrePrefix.dust, Materials.Glass), "QF",
                'Q', new UnificationEntry(OrePrefix.dust, Materials.QuartzSand),
                'F', new UnificationEntry(OrePrefix.dustTiny, Materials.Flint));

        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.SAND, 1, GTValues.W));
    }

    /**
     * + Adds harder redstone related recipes
     * - Removes old redstone related recipes
     */
    private static void redstoneRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dispenser"));

        ModHandler.addShapedRecipe("dispenser", new ItemStack(Blocks.DISPENSER), "CRC", "STS", "GAG",
                'C', OrePrefix.stoneCobble,
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.spring, Materials.Iron),
                'T', new ItemStack(Items.STRING),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'A', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:noteblock"));

        ModHandler.addShapedRecipe("noteblock", new ItemStack(Blocks.NOTEBLOCK), "PPP", "BGB", "PRP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Wood),
                'B', new ItemStack(Blocks.IRON_BARS),
                'G', new UnificationEntry(OrePrefix.gear, Materials.Wood),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:sticky_piston"));
        ModHandler.addShapedRecipe("sticky_piston", new ItemStack(Blocks.STICKY_PISTON, 1), "h", "R", "P",
                'R', "slimeball",
                'P', new ItemStack(Blocks.PISTON)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:piston"));

        //todo this doesn't show in jei for unknown reasons
//        ModHandler.addShapedRecipe("piston_iron", new ItemStack(Blocks.PISTON, 1), "PPP", "GFG", "SRS",
//                'P', OrePrefix.plank,
//                'G', OrePrefix.gearSmall, Materials.Iron,
//                'F', "fenceWood",
//                'S', OrePrefix.stoneCobble,
//                'R', OrePrefix.plate, Materials.RedAlloy
//        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Iron)
                .input(OrePrefix.gearSmall, Materials.Iron)
                .input("slabWood", 1)
                .input("cobblestone", 1)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L))
                .outputs(new ItemStack(Blocks.PISTON))
                .duration(240).EUt(8).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Steel)
                .input(OrePrefix.gearSmall, Materials.Steel)
                .input("slabWood", 2)
                .input("cobblestone", 2)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L * 2))
                .outputs(new ItemStack(Blocks.PISTON, 2))
                .duration(240).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Aluminium)
                .input(OrePrefix.gearSmall, Materials.Aluminium)
                .input("slabWood", 4)
                .input("cobblestone", 4)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L * 3))
                .outputs(new ItemStack(Blocks.PISTON, 4))
                .duration(240).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel)
                .input(OrePrefix.gearSmall, Materials.StainlessSteel)
                .input("slabWood", 8)
                .input("cobblestone", 8)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L * 4))
                .outputs(new ItemStack(Blocks.PISTON, 8))
                .duration(600).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Titanium)
                .input(OrePrefix.gearSmall, Materials.Titanium)
                .input("slabWood", 16)
                .input("cobblestone", 16)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L * 8))
                .outputs(new ItemStack(Blocks.PISTON, 16))
                .duration(800).EUt(30).buildAndRegister();


        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stone_pressure_plate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:wooden_pressure_plate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:heavy_weighted_pressure_plate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:light_weighted_pressure_plate"));

        ModHandler.addShapedRecipe("stone_pressure_plate", new ItemStack(Blocks.STONE_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'L', new ItemStack(Blocks.STONE_SLAB),
                'C', new UnificationEntry(OrePrefix.spring, Materials.Iron)
        );

        ModHandler.addShapedRecipe("wooden_pressure_plate", new ItemStack(Blocks.WOODEN_PRESSURE_PLATE), "SrS", "LCL", "SdS",
                'S', new UnificationEntry(OrePrefix.bolt, Materials.Wood),
                'L', new UnificationEntry(OrePrefix.plate, Materials.Wood),
                'C', new UnificationEntry(OrePrefix.spring, Materials.Iron)
        );

        ModHandler.addShapedRecipe("heavy_weighted_pressure_plate", new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Steel),
                'L', new UnificationEntry(OrePrefix.plate, Materials.Gold),
                'C', new UnificationEntry(OrePrefix.spring, Materials.Steel)
        );

        ModHandler.addShapedRecipe("light_weighted_pressure_plate", new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Steel),
                'L', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'C', new UnificationEntry(OrePrefix.spring, Materials.Steel)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.spring, Materials.Iron)
                .inputs(new ItemStack(Blocks.STONE_SLAB, 2))
                .outputs(new ItemStack(Blocks.STONE_PRESSURE_PLATE))
                .duration(100).EUt(7).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.spring, Materials.Iron)
                .input(OrePrefix.plank, Materials.Wood, 2)
                .outputs(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE))
                .duration(100).EUt(7).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.spring, Materials.Steel)
                .input(OrePrefix.plate, Materials.Gold)
                .outputs(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                .duration(200).EUt(16).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.spring, Materials.Steel)
                .input(OrePrefix.plate, Materials.Iron)
                .outputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .duration(200).EUt(16).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:daylight_detector"));
        ModHandler.addShapedRecipe("daylight_detector", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                'G', new ItemStack(Blocks.GLASS, 1, GTValues.W),
                'P', new UnificationEntry(OrePrefix.plate, Materials.NetherQuartz),
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.addShapedRecipe("daylight_detector_certus", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                'G', new ItemStack(Blocks.GLASS, 1, GTValues.W),
                'P', new UnificationEntry(OrePrefix.plate, Materials.CertusQuartz),
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:trapdoor"));
        ModHandler.addShapedRecipe("trapdoor", new ItemStack(Blocks.TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W),
                'R', new UnificationEntry(OrePrefix.stick, Materials.Wood)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:redstone_lamp"));
        ModHandler.addShapedRecipe("redstone_lamp", new ItemStack(Blocks.REDSTONE_LAMP), "PPP", "PGP", "PRP",
                'P', new ItemStack(Blocks.GLASS_PANE, 1, GTValues.W),
                'G', new UnificationEntry(OrePrefix.block, Materials.Glowstone),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:tripwire_hook"));
        ModHandler.addShapedRecipe("tripwire_hook", new ItemStack(Blocks.TRIPWIRE_HOOK), "IRI", "SRS", " S ",
                'I', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'R', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'S', new ItemStack(Items.STRING)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dropper"));
        ModHandler.addShapedRecipe("dropper", new ItemStack(Blocks.DROPPER), "CRC", "STS", "GAG",
                'C', new UnificationEntry(OrePrefix.stoneCobble),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.springSmall, Materials.Iron),
                'T', new ItemStack(Items.STRING),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'A', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

//        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_trapdoor"));
//        ModHandler.addShapedRecipe("iron_trapdoor", new ItemStack(Blocks.IRON_TRAPDOOR), "SPS", "STS", "sPd",
//                'X', new UnificationEntry(OrePrefix.plate, Materials.Iron)
//        );
    }

    /**
     * - Removes building block recipes
     */
    private static void buildingBlockRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:smooth_sandstone"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:smooth_red_sandstone"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stone_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:sandstone_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:cobblestone_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:brick_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stone_brick_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:nether_brick_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:quartz_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:oak_wooden_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:spruce_wooden_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:birch_wooden_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jungle_wooden_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:acacia_wooden_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dark_oak_wooden_slab"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stonebrick"));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.STONEBRICK));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:quartz_block"));
    }

    /**
     * + Changes vanilla recipes using metals to plates and other components
     */
    private static void metalRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_door"));
        ModHandler.addShapedRecipe("iron_door", new ItemStack(Items.IRON_DOOR, 3), "XX ", "XXh", "XX ", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:cauldron"));
        ModHandler.addShapedRecipe("cauldron", new ItemStack(Items.CAULDRON), "X X", "XhX", "XXX", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:hopper"));
        ModHandler.addShapedRecipe("hopper", new ItemStack(Blocks.HOPPER), "XCX", "XGX", "wXh",
                'X', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'C', "chestWood",
                'G', new UnificationEntry(OrePrefix.gear, Materials.Iron)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_bars"));
        ModHandler.addShapedRecipe("iron_bars", new ItemStack(Blocks.IRON_BARS, 8), " w ", "XXX", "XXX", 'X', new UnificationEntry(OrePrefix.stick, Materials.Iron));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:anvil"));
        ModHandler.addShapedRecipe("anvil", new ItemStack(Blocks.ANVIL), "BBB", "SBS", "PBP",
                'B', new UnificationEntry(OrePrefix.block, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron)
        );
    }

    /**
     * - Removes Vanilla TNT recipe
     */
    private static void specialRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:tnt"));
    }
}
