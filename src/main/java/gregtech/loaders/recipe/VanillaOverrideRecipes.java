package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;

public class VanillaOverrideRecipes {

    public static void init() {
        woodRecipes();
        if (ConfigHolder.recipes.hardGlassRecipes)
            glassRecipes();
        if (ConfigHolder.recipes.hardRedstoneRecipes)
            redstoneRecipes();
        if (ConfigHolder.recipes.hardIronRecipes)
            metalRecipes();
        if (ConfigHolder.recipes.hardMiscRecipes)
            miscRecipes();
        if (ConfigHolder.recipes.hardDyeRecipes)
            dyeRecipes();
        if (ConfigHolder.recipes.harderBrickRecipes) {
            ModHandler.removeFurnaceSmelting(new ItemStack(Items.CLAY_BALL, 1, GTValues.W));
            ModHandler.removeRecipeByName("minecraft:brick_block");
            ModHandler.addShapedRecipe("brick_from_water", new ItemStack(Blocks.BRICK_BLOCK, 2), "BBB", "BWB", "BBB",
                    'B', new ItemStack(Items.BRICK),
                    'W', new ItemStack(Items.WATER_BUCKET));
        }
        removeCompressionRecipes();
        toolArmorRecipes();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:tnt"));
    }

    private static void woodRecipes() {
        if (ConfigHolder.recipes.nerfWoodCrafting) {
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stick"));
            ModHandler.addShapedRecipe("stick_saw", new ItemStack(Items.STICK, 4), "s", "P", "P", 'P', new UnificationEntry(OrePrefix.plank, Materials.Wood));
            ModHandler.addShapedRecipe("stick_normal", new ItemStack(Items.STICK, 2), "P", "P", 'P', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        }

        if (ConfigHolder.recipes.nerfPaperCrafting) {
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:paper"));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:sugar"));
            ModHandler.addShapedRecipe("paper_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Paper, 2), "SSS", " m ", 'S', new ItemStack(Items.REEDS));
            ModHandler.addShapedRecipe("sugar", OreDictUnifier.get(OrePrefix.dust, Materials.Sugar, 1), "Sm ", 'S', new ItemStack(Items.REEDS));
            ItemStack paperStack = OreDictUnifier.get(OrePrefix.plate, Materials.Paper, 2);
            Object[] paperRecipeIngredients = ModHandler.finalizeShapedRecipeInput(" C ", "SSS", " C ", 'S', OreDictUnifier.get(OrePrefix.dust, Materials.Paper, 1), 'C', new ItemStack(Blocks.STONE_SLAB));
            ForgeRegistries.RECIPES.register(new CustomItemReturnShapedOreRecipeRecipe(null, paperStack,
                    stack -> Block.getBlockFromItem(stack.getItem()) == Blocks.STONE_SLAB, paperRecipeIngredients)
                    .setMirrored(false).setRegistryName("paper"));
        }

        if (!ConfigHolder.recipes.hardWoodRecipes)
            return;

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:ladder"));
        ModHandler.addShapedRecipe("ladder", new ItemStack(Blocks.LADDER, 2), "SrS", "SRS", "ShS", 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'R', new UnificationEntry(OrePrefix.bolt, Materials.Wood));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:wooden_door"));
        ModHandler.addShapedRecipe("wooden_door", new ItemStack(Items.OAK_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.PLANKS, 1, 0),
                'T', new ItemStack(Blocks.TRAPDOOR),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .inputs(new ItemStack(Blocks.PLANKS, 4, 0))
                .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.OAK_DOOR))
                .duration(400).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:spruce_door"));
        ModHandler.addShapedRecipe("spruce_door", new ItemStack(Items.SPRUCE_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.PLANKS, 1, 1),
                'T', new ItemStack(Blocks.TRAPDOOR),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .inputs(new ItemStack(Blocks.PLANKS, 4, 1))
                .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.SPRUCE_DOOR))
                .duration(400).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:birch_door"));
        ModHandler.addShapedRecipe("birch_door", new ItemStack(Items.BIRCH_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.PLANKS, 1, 2),
                'T', new ItemStack(Blocks.TRAPDOOR),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .inputs(new ItemStack(Blocks.PLANKS, 4, 2))
                .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.BIRCH_DOOR))
                .duration(400).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jungle_door"));
        ModHandler.addShapedRecipe("jungle_door", new ItemStack(Items.JUNGLE_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.PLANKS, 1, 3),
                'T', new ItemStack(Blocks.TRAPDOOR),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .inputs(new ItemStack(Blocks.PLANKS, 4, 3))
                .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.JUNGLE_DOOR))
                .duration(400).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:acacia_door"));
        ModHandler.addShapedRecipe("acacia_door", new ItemStack(Items.ACACIA_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.PLANKS, 1, 4),
                'T', new ItemStack(Blocks.TRAPDOOR),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .inputs(new ItemStack(Blocks.PLANKS, 4, 4))
                .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.ACACIA_DOOR))
                .duration(400).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dark_oak_door"));
        ModHandler.addShapedRecipe("dark_oak_door", new ItemStack(Items.DARK_OAK_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.PLANKS, 1, 5),
                'T', new ItemStack(Blocks.TRAPDOOR),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .inputs(new ItemStack(Blocks.PLANKS, 4, 5))
                .fluidInputs(Materials.Iron.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.DARK_OAK_DOOR))
                .duration(400).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:trapdoor"));
        ModHandler.addShapedRecipe("trapdoor", new ItemStack(Blocks.TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W),
                'R', new UnificationEntry(OrePrefix.stick, Materials.Wood)
        );

        ModHandler.addShapedRecipe("bowl", new ItemStack(Items.BOWL), "k", "X", 'X', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:bowl"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:chest"));
        ModHandler.addShapedRecipe("chest", new ItemStack(Blocks.CHEST), "LPL", "PFP", "LPL",
                'L', new UnificationEntry(OrePrefix.log, Materials.Wood),
                'P', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'F', new ItemStack(Items.FLINT));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:boat"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:spruce_boat"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:birch_boat"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jungle_boat"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:acacia_boat"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dark_oak_boat"));
        ModHandler.addShapedRecipe("boat", new ItemStack(Items.BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.PLANKS, 1, 0), 'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 0), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        ModHandler.addShapedRecipe("spruce_boat", new ItemStack(Items.SPRUCE_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.PLANKS, 1, 1), 'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 1), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        ModHandler.addShapedRecipe("birch_boat", new ItemStack(Items.BIRCH_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.PLANKS, 1, 2), 'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 2), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        ModHandler.addShapedRecipe("jungle_boat", new ItemStack(Items.JUNGLE_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.PLANKS, 1, 3), 'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 3), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        ModHandler.addShapedRecipe("acacia_boat", new ItemStack(Items.ACACIA_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.PLANKS, 1, 4), 'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 4), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        ModHandler.addShapedRecipe("dark_oak_boat", new ItemStack(Items.DARK_OAK_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.PLANKS, 1, 5), 'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 5), 'H', new ItemStack(Items.WOODEN_SHOVEL));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:fence"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:spruce_fence"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:birch_fence"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jungle_fence"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:acacia_fence"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dark_oak_fence"));
        ModHandler.addShapedRecipe("fence", new ItemStack(Blocks.OAK_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.PLANKS, 1, 0), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("spruce_fence", new ItemStack(Blocks.SPRUCE_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.PLANKS, 1, 1), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("birch_fence", new ItemStack(Blocks.BIRCH_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.PLANKS, 1, 2), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("jungle_fence", new ItemStack(Blocks.JUNGLE_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.PLANKS, 1, 3), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("acacia_fence", new ItemStack(Blocks.ACACIA_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.PLANKS, 1, 4), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("dark_oak_fence", new ItemStack(Blocks.DARK_OAK_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.PLANKS, 1, 5), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:fence_gate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:spruce_fence_gate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:birch_fence_gate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jungle_fence_gate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:acacia_fence_gate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dark_oak_fence_gate"));
        ModHandler.addShapedRecipe("fence_gate", new ItemStack(Blocks.OAK_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 0), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'F', new ItemStack(Items.FLINT));
        ModHandler.addShapedRecipe("spruce_fence_gate", new ItemStack(Blocks.SPRUCE_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 1), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'F', new ItemStack(Items.FLINT));
        ModHandler.addShapedRecipe("birch_fence_gate", new ItemStack(Blocks.BIRCH_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 2), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'F', new ItemStack(Items.FLINT));
        ModHandler.addShapedRecipe("jungle_fence_gate", new ItemStack(Blocks.JUNGLE_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 3), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'F', new ItemStack(Items.FLINT));
        ModHandler.addShapedRecipe("acacia_fence_gate", new ItemStack(Blocks.ACACIA_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 4), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'F', new ItemStack(Items.FLINT));
        ModHandler.addShapedRecipe("dark_oak_fence_gate", new ItemStack(Blocks.DARK_OAK_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 5), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'F', new ItemStack(Items.FLINT));

        ModHandler.addShapedRecipe("fence_gate_screws", new ItemStack(Blocks.OAK_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 0), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe("spruce_fence_gate_screws", new ItemStack(Blocks.SPRUCE_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 1), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe("birch_fence_gate_screws", new ItemStack(Blocks.BIRCH_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 2), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe("jungle_fence_gate_screws", new ItemStack(Blocks.JUNGLE_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 3), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe("acacia_fence_gate_screws", new ItemStack(Blocks.ACACIA_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 4), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe("dark_oak_fence_gate_screws", new ItemStack(Blocks.DARK_OAK_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.PLANKS, 1, 5), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood), 'I', new UnificationEntry(OrePrefix.screw, Materials.Iron));

    }

    /**
     * + Adds Glass Handcrafting
     * - Removes Sand -> Glass Furnace Smelting
     * - Removes Glass Bottle Crafting
     */
    private static void glassRecipes() {
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.SAND, 1, GTValues.W));
        ModHandler.removeRecipes(new ItemStack(Items.GLASS_BOTTLE, 3));
    }

    /**
     * Replaces redstone related recipes with harder versions
     */
    private static void redstoneRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:dispenser"));

        ModHandler.addShapedRecipe("dispenser", new ItemStack(Blocks.DISPENSER), "CRC", "STS", "GAG",
                'C', OreDictNames.stoneCobble,
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.spring, Materials.Iron),
                'T', new ItemStack(Items.STRING),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'A', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .input(OrePrefix.ring, Materials.Iron)
                .input(OrePrefix.spring, Materials.Iron, 2)
                .input(OrePrefix.gearSmall, Materials.Iron, 2)
                .input(OrePrefix.stick, Materials.RedAlloy)
                .inputs(new ItemStack(Items.STRING))
                .outputs(new ItemStack(Blocks.DISPENSER))
                .buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:sticky_piston"));
        ModHandler.addShapedRecipe("sticky_piston", new ItemStack(Blocks.STICKY_PISTON, 1), "h", "R", "P",
                'R', "slimeball",
                'P', new ItemStack(Blocks.PISTON)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:piston"));

        ModHandler.addShapedRecipe("piston_iron", new ItemStack(Blocks.PISTON), "WWW", "GFG", "CRC",
                'W', new UnificationEntry(OrePrefix.plank, Materials.Wood),
                'C', OreDictNames.stoneCobble,
                'R', new UnificationEntry(OrePrefix.plate, Materials.RedAlloy),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'F', "fenceWood"
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Iron)
                .input(OrePrefix.gearSmall, Materials.Iron)
                .input("slabWood", 1)
                .input("cobblestone", 1)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L))
                .outputs(new ItemStack(Blocks.PISTON))
                .duration(240).EUt(VA[ULV]).buildAndRegister();

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
                .duration(240).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel)
                .input(OrePrefix.gearSmall, Materials.StainlessSteel)
                .input("slabWood", 8)
                .input("cobblestone", 8)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L * 4))
                .outputs(new ItemStack(Blocks.PISTON, 8))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Titanium)
                .input(OrePrefix.gearSmall, Materials.Titanium)
                .input("slabWood", 16)
                .input("cobblestone", 16)
                .fluidInputs(Materials.RedAlloy.getFluid(GTValues.L * 8))
                .outputs(new ItemStack(Blocks.PISTON, 16))
                .duration(800).EUt(VA[LV]).buildAndRegister();


        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stone_pressure_plate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:wooden_pressure_plate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:heavy_weighted_pressure_plate"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:light_weighted_pressure_plate"));

        ModHandler.addShapedRecipe("stone_pressure_plate", new ItemStack(Blocks.STONE_PRESSURE_PLATE, 2), "ShS", "LCL", "SdS",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'L', new ItemStack(Blocks.STONE_SLAB),
                'C', new UnificationEntry(OrePrefix.spring, Materials.Iron)
        );

        ModHandler.addShapedRecipe("wooden_pressure_plate", new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
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
                .outputs(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 2))
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.spring, Materials.Iron)
                .input(OrePrefix.plank, Materials.Wood, 2)
                .outputs(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 2))
                .duration(100).EUt(VA[ULV]).buildAndRegister();

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

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stone_button"));
        ModHandler.addShapedRecipe("stone_button", new ItemStack(Blocks.STONE_BUTTON, 6), "sP",
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:wooden_button"));
        ModHandler.addShapedRecipe("wooden_button", new ItemStack(Blocks.WOODEN_BUTTON, 6), "sP",
                'P', new ItemStack(Blocks.WOODEN_PRESSURE_PLATE));

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE_PRESSURE_PLATE))
                .outputs(new ItemStack(Blocks.STONE_BUTTON, 12))
                .duration(25).EUt(VA[ULV]).buildAndRegister();

        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE))
                .outputs(new ItemStack(Blocks.WOODEN_BUTTON, 12))
                .duration(25).EUt(VA[ULV]).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:lever"));
        ModHandler.addShapedRecipe("lever", new ItemStack(Blocks.LEVER), "B", "S",
                'B', new ItemStack(Blocks.STONE_BUTTON),
                'S', new ItemStack(Items.STICK)
        );

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

        ModHandler.addShapedRecipe("daylight_detector_quartzite", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                'G', new ItemStack(Blocks.GLASS, 1, GTValues.W),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Quartzite),
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
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
                'C', OreDictNames.stoneCobble,
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.springSmall, Materials.Iron),
                'T', new ItemStack(Items.STRING),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'A', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:observer"));
        ModHandler.addShapedRecipe("observer", new ItemStack(Blocks.OBSERVER), "RCR", "CQC", "GSG",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'C', OreDictNames.stoneCobble,
                'Q', new UnificationEntry(OrePrefix.plate, Materials.NetherQuartz),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.addShapedRecipe("observer_certus", new ItemStack(Blocks.OBSERVER), "RCR", "CQC", "GSG",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'C', OreDictNames.stoneCobble,
                'Q', new UnificationEntry(OrePrefix.plate, Materials.CertusQuartz),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.addShapedRecipe("observer_quartzite", new ItemStack(Blocks.OBSERVER), "RCR", "CQC", "GSG",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'C', OreDictNames.stoneCobble,
                'Q', new UnificationEntry(OrePrefix.plate, Materials.Quartzite),
                'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:repeater"));
        ModHandler.addShapedRecipe("repeater", new ItemStack(Items.REPEATER), "S S", "TdT", "PRP",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:comparator"));
        ModHandler.addShapedRecipe("comparator", new ItemStack(Items.COMPARATOR), "STS", "TQT", "PdP",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'Q', new UnificationEntry(OrePrefix.plate, Materials.NetherQuartz),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE)
        );

        ModHandler.addShapedRecipe("comparator_certus", new ItemStack(Items.COMPARATOR), "STS", "TQT", "PdP",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'Q', new UnificationEntry(OrePrefix.plate, Materials.CertusQuartz),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE)
        );

        ModHandler.addShapedRecipe("comparator_quartzite", new ItemStack(Items.COMPARATOR), "STS", "TQT", "PdP",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'T', new ItemStack(Blocks.REDSTONE_TORCH),
                'Q', new UnificationEntry(OrePrefix.plate, Materials.Quartzite),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:golden_rail"));
        ModHandler.addShapedRecipe("golden_rail", new ItemStack(Blocks.GOLDEN_RAIL, 6), "SPS", "IWI", "GdG",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Steel),
                'P', new UnificationEntry(OrePrefix.plate, Materials.RedAlloy),
                'I', new UnificationEntry(OrePrefix.stick, Materials.Iron),
                'W', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'G', new UnificationEntry(OrePrefix.stick, Materials.Gold)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:detector_rail"));
        ModHandler.addShapedRecipe("detector_rail", new ItemStack(Blocks.DETECTOR_RAIL, 6), "SPS", "IWI", "IdI",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'P', new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
                'I', new UnificationEntry(OrePrefix.stick, Materials.Iron),
                'W', new UnificationEntry(OrePrefix.stick, Materials.Wood)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:rail"));
        ModHandler.addShapedRecipe("rail", new ItemStack(Blocks.RAIL, 8), "ShS", "IWI", "IdI",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'I', new UnificationEntry(OrePrefix.stick, Materials.Iron),
                'W', new UnificationEntry(OrePrefix.stick, Materials.Wood)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:activator_rail"));
        ModHandler.addShapedRecipe("activator_rail", new ItemStack(Blocks.ACTIVATOR_RAIL, 6), "SPS", "IWI", "IdI",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'P', new ItemStack(Blocks.REDSTONE_TORCH),
                'I', new UnificationEntry(OrePrefix.stick, Materials.Iron),
                'W', new UnificationEntry(OrePrefix.stick, Materials.Wood)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:redstone_torch"));
        ModHandler.addShapedRecipe("redstone_torch", new ItemStack(Blocks.REDSTONE_TORCH), "R", "T", 'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone), 'T', new UnificationEntry(OrePrefix.stick, Materials.Wood));
    }

    /**
     * Changes vanilla recipes using metals to plates and other components
     */
    private static void metalRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_door"));
        ModHandler.addShapedRecipe("iron_door", new ItemStack(Items.IRON_DOOR), "PTh", "PRS", "PPd",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'T', new ItemStack(Blocks.IRON_BARS),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Steel),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Steel)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.IRON_BARS))
                .input(OrePrefix.plate, Materials.Iron, 4)
                .fluidInputs(Materials.Steel.getFluid(GTValues.L / 9))
                .outputs(new ItemStack(Items.IRON_DOOR))
                .duration(400).EUt(VA[ULV]).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:cauldron"));
        ModHandler.addShapedRecipe("cauldron", new ItemStack(Items.CAULDRON), "X X", "XhX", "XXX",
                'X', new UnificationEntry(OrePrefix.plate, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Iron, 7)
                .outputs(new ItemStack(Items.CAULDRON, 1))
                .circuitMeta(7)
                .duration(700).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:hopper"));
        ModHandler.addShapedRecipe("hopper", new ItemStack(Blocks.HOPPER), "XCX", "XGX", "wXh",
                'X', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'C', "chestWood",
                'G', new UnificationEntry(OrePrefix.gear, Materials.Iron)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_bars"));
        ModHandler.addShapedRecipe("iron_bars", new ItemStack(Blocks.IRON_BARS, 8), " h ", "XXX", "XXX",
                'X', new UnificationEntry(OrePrefix.stick, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Iron, 3)
                .outputs(new ItemStack(Blocks.IRON_BARS, 4))
                .circuitMeta(3)
                .duration(300).EUt(4).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:anvil"));
        ModHandler.addShapedRecipe("anvil", new ItemStack(Blocks.ANVIL), "BBB", "SBS", "PBP",
                'B', new UnificationEntry(OrePrefix.block, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_trapdoor"));
        ModHandler.addShapedRecipe("iron_trapdoor", new ItemStack(Blocks.IRON_TRAPDOOR), "SPS", "PTP", "sPd",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'T', new ItemStack(Blocks.TRAPDOOR)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Iron, 4)
                .inputs(new ItemStack(Blocks.TRAPDOOR))
                .outputs(new ItemStack(Blocks.IRON_TRAPDOOR))
                .duration(100).EUt(16).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:minecart"));
        ModHandler.addShapedRecipe("minecart", new ItemStack(Items.MINECART), "RhR", "PwP", "RPR",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron)
        );

        ModHandler.addShapedRecipe("iron_bucket", new ItemStack(Items.BUCKET), "XhX", " X ", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:bucket"));

    }

    /**
     * Replaces Vanilla Beacon Recipe
     * Replaces Vanilla Jack-o-lantern Recipe
     * Replaces Vanilla Book Recipe
     * Replaces Vanilla Brewing Stand Recipe
     * Replaces Vanilla Enchantment Table recipe
     * Replaces Vanilla Jukebox recipe
     * Replaces Vanilla Note Block recipe
     * Replaces Vanilla Furnace
     * - Removes Vanilla TNT recipe
     * - Removes Vanilla Golden Apple Recipe
     * - Removes Vanilla Ender Eye Recipe
     * - Removes Vanilla Glistering Melon Recipe
     * - Removes Vanilla Golden Carrot Recipe
     * - Removes Vanilla Magma Cream Recipe
     * - Removes Vanilla Polished Stone Variant Recipes
     * - Removes Vanilla Brick Smelting Recipe
     */
    private static void miscRecipes() {
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:tnt"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:beacon"));
        ModHandler.addShapedRecipe("beacon", new ItemStack(Blocks.BEACON), "GLG", "GSG", "OOO",
                'G', new ItemStack(Blocks.GLASS),
                'L', new UnificationEntry(OrePrefix.lens, Materials.NetherStar),
                'S', new ItemStack(Items.NETHER_STAR),
                'O', new UnificationEntry(OrePrefix.plate, Materials.Obsidian)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:lit_pumpkin"));
        ModHandler.addShapedRecipe("lit_pumpkin", new ItemStack(Blocks.LIT_PUMPKIN), "PT", "k ", 'P', new ItemStack(Blocks.PUMPKIN), 'T', new ItemStack(Blocks.TORCH));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:golden_apple"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:book"));
        ModHandler.addShapedRecipe("book", new ItemStack(Items.BOOK), "SPL", "SPG", "SPL",
                'S', new ItemStack(Items.STRING),
                'P', new ItemStack(Items.PAPER),
                'L', new ItemStack(Items.LEATHER),
                'G', MetaItems.STICKY_RESIN.getStackForm().copy()
        );

        ModHandler.removeRecipeByName(new ResourceLocation("brewing_stand"));
        ModHandler.addShapedRecipe("brewing_stand", new ItemStack(Items.BREWING_STAND), "RBR", "ABA", "SCS",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Aluminium),
                'B', new UnificationEntry(OrePrefix.stick, Materials.Blaze),
                'A', new UnificationEntry(OrePrefix.stick, Materials.Aluminium),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Aluminium),
                'C', new ItemStack(Items.CAULDRON));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:ender_eye"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:speckled_melon"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:golden_carrot"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:magma_cream"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:enchanting_table"));
        ModHandler.addShapedRecipe("enchanting_table", new ItemStack(Blocks.ENCHANTING_TABLE), "DCD", "PBP", "DPD",
                'D', new UnificationEntry(OrePrefix.gem, Materials.Diamond),
                'C', new ItemStack(Blocks.CARPET, 1, 14),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Obsidian),
                'B', new ItemStack(Blocks.BOOKSHELF)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:jukebox"));
        ModHandler.addShapedRecipe("jukebox", new ItemStack(Blocks.JUKEBOX), "LBL", "NRN", "LGL",
                'L', new UnificationEntry(OrePrefix.log, Materials.Wood),
                'B', new UnificationEntry(OrePrefix.bolt, Materials.Diamond),
                'N', new ItemStack(Blocks.NOTEBLOCK),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'G', new UnificationEntry(OrePrefix.gear, Materials.Iron)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.bolt, Materials.Diamond)
                .input(OrePrefix.gear, Materials.Iron)
                .input(OrePrefix.ring, Materials.Iron)
                .input(OrePrefix.plate, Materials.Wood, 4)
                .inputs(new ItemStack(Blocks.NOTEBLOCK, 2))
                .outputs(new ItemStack(Blocks.JUKEBOX))
                .duration(100).EUt(16).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:noteblock"));
        ModHandler.addShapedRecipe("noteblock", new ItemStack(Blocks.NOTEBLOCK), "PPP", "BGB", "PRP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Wood),
                'B', new ItemStack(Blocks.IRON_BARS),
                'G', new UnificationEntry(OrePrefix.gear, Materials.Wood),
                'R', new UnificationEntry(OrePrefix.stick, Materials.RedAlloy)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Wood, 4)
                .input(OrePrefix.gear, Materials.Wood)
                .input(OrePrefix.stick, Materials.RedAlloy)
                .inputs(new ItemStack(Blocks.IRON_BARS, 2))
                .outputs(new ItemStack(Blocks.NOTEBLOCK))
                .duration(100).EUt(16).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:furnace"));
        ModHandler.addShapedRecipe("furnace", new ItemStack(Blocks.FURNACE), "CCC", "FFF", "CCC",
                'F', new ItemStack(Items.FLINT),
                'C', OreDictNames.stoneCobble
        );

        ASSEMBLER_RECIPES.recipeBuilder()
                .circuitMeta(8)
                .input(OreDictNames.stoneCobble.name(), 8)
                .inputs(new ItemStack(Items.FLINT))
                .outputs(new ItemStack(Blocks.FURNACE))
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:crafting_table"));
        ModHandler.addShapedRecipe("crafting_table", new ItemStack(Blocks.CRAFTING_TABLE), "FF", "WW", 'F', new ItemStack(Items.FLINT), 'W', new UnificationEntry(OrePrefix.log, Materials.Wood));
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(6).input("logWood", 1).inputs(new ItemStack(Items.FLINT)).outputs(new ItemStack(Blocks.CRAFTING_TABLE)).buildAndRegister();

        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.STONEBRICK));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:polished_granite"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:polished_diorite"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:polished_andesite"));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:lead"));
        ModHandler.addShapedRecipe("lead", new ItemStack(Items.LEAD), "SSS", "SBS", "SSS", 'S', new ItemStack(Items.STRING), 'B', new ItemStack(Items.SLIME_BALL));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:bow"));
        ModHandler.addShapedRecipe("bow", new ItemStack(Items.BOW), "hLS", "LRS", "fLS",
                'L', new UnificationEntry(OrePrefix.stickLong, Materials.Wood),
                'S', new ItemStack(Items.STRING),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:item_frame"));
        ModHandler.addShapedRecipe("item_frame", new ItemStack(Items.ITEM_FRAME), "SRS", "TLT", "TTT",
                'S', new ItemStack(Items.STRING),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'T', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'L', new ItemStack(Items.LEATHER));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:painting"));
        ModHandler.addShapedRecipe("painting", new ItemStack(Items.PAINTING), "SRS", "TCT", "TTT",
                'S', new ItemStack(Items.STRING),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'T', new UnificationEntry(OrePrefix.stick, Materials.Wood),
                'C', new ItemStack(Blocks.CARPET, 1, W));

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:chest_minecart"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:furnace_minecart"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:tnt_minecart"));
        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:hopper_minecart"));
        ModHandler.addShapedRecipe("chest_minecart", new ItemStack(Items.CHEST_MINECART), "hIw", " M ", " d ", 'I', "chestWood", 'M', new ItemStack(Items.MINECART));
        ModHandler.addShapedRecipe("furnace_minecart", new ItemStack(Items.FURNACE_MINECART), "hIw", " M ", " d ", 'I', new ItemStack(Blocks.FURNACE), 'M', new ItemStack(Items.MINECART));
        ModHandler.addShapedRecipe("tnt_minecart", new ItemStack(Items.TNT_MINECART), "hIw", " M ", " d ", 'I', new ItemStack(Blocks.TNT), 'M', new ItemStack(Items.MINECART));
        ModHandler.addShapedRecipe("hopper_minecart", new ItemStack(Items.HOPPER_MINECART), "hIw", " M ", " d ", 'I', new ItemStack(Blocks.HOPPER), 'M', new ItemStack(Items.MINECART));
    }

    /**
     * - Removes Concrete, Stained Clay, and Stained glass crafting recipes
     */
    private static void dyeRecipes() {
        for (MarkerMaterial colorMaterial : MarkerMaterials.Color.VALUES) {
            ModHandler.removeRecipeByName(new ResourceLocation(String.format("minecraft:%s_concrete_powder", colorMaterial)));
            ModHandler.removeRecipeByName(new ResourceLocation(String.format("minecraft:%s_stained_hardened_clay", colorMaterial)));
            ModHandler.removeRecipeByName(new ResourceLocation(String.format("minecraft:%s_stained_glass", colorMaterial)));
            ModHandler.removeRecipeByName(new ResourceLocation(String.format("minecraft:%s_wool", colorMaterial)));
        }
        ModHandler.removeRecipeByName("minecraft:dark_prismarine");
    }

    /**
     * - Removes block compression/decompression recipes if configured
     */
    private static void removeCompressionRecipes() {
        if (ConfigHolder.recipes.disableManualCompression) {
            ModHandler.removeRecipeByName("minecraft:gold_block");
            ModHandler.removeRecipeByName("minecraft:gold_nugget");
            ModHandler.removeRecipeByName("minecraft:gold_ingot_from_block");
            ModHandler.removeRecipeByName("minecraft:gold_ingot_from_nuggets");
            ModHandler.removeRecipeByName("minecraft:coal_block");
            ModHandler.removeRecipeByName("minecraft:coal");
            ModHandler.removeRecipeByName("minecraft:redstone_block");
            ModHandler.removeRecipeByName("minecraft:redstone");
            ModHandler.removeRecipeByName("minecraft:emerald_block");
            ModHandler.removeRecipeByName("minecraft:emerald");
            ModHandler.removeRecipeByName("minecraft:diamond_block");
            ModHandler.removeRecipeByName("minecraft:diamond");
            ModHandler.removeRecipeByName("minecraft:iron_block");
            ModHandler.removeRecipeByName("minecraft:iron_nugget");
            ModHandler.removeRecipeByName("minecraft:iron_ingot_from_block");
            ModHandler.removeRecipeByName("minecraft:iron_ingot_from_nuggets");
            ModHandler.removeRecipeByName("minecraft:lapis_block");
            ModHandler.removeRecipeByName("minecraft:lapis_lazuli");
            ModHandler.removeRecipeByName("minecraft:quartz_block");
            ModHandler.removeRecipeByName("minecraft:clay");
            ModHandler.removeRecipeByName("minecraft:nether_brick");
            ModHandler.removeRecipeByName("minecraft:glowstone");
        }

        if (ConfigHolder.recipes.removeVanillaBlockRecipes) {
            ModHandler.removeRecipeByName("minecraft:slime");
            ModHandler.removeRecipeByName("minecraft:slime_ball");
            ModHandler.removeRecipeByName("minecraft:melon_block");
            ModHandler.removeRecipeByName("minecraft:hay_block");
            ModHandler.removeRecipeByName("minecraft:wheat");
            ModHandler.removeRecipeByName("minecraft:magma");
            ModHandler.removeRecipeByName("minecraft:nether_wart_block");
            ModHandler.removeRecipeByName("minecraft:bone_block");
            ModHandler.removeRecipeByName("minecraft:bone_meal_from_block");
            ModHandler.removeRecipeByName("minecraft:purpur_block");
            ModHandler.removeRecipeByName("minecraft:prismarine_bricks");
            ModHandler.removeRecipeByName("minecraft:prismarine");
            ModHandler.removeRecipeByName("minecraft:snow");
            ModHandler.removeRecipeByName("minecraft:sandstone");
            ModHandler.removeRecipeByName("minecraft:polished_andesite");
            ModHandler.removeRecipeByName("minecraft:polished_diorite");
            ModHandler.removeRecipeByName("minecraft:polished_granite");
            ModHandler.removeRecipeByName("minecraft:coarse_dirt");
            ModHandler.removeRecipeByName("minecraft:smooth_sandstone");
            ModHandler.removeRecipeByName("minecraft_chiseled_sandstone");
            ModHandler.removeRecipeByName("minecraft:chiseled_quartz_block");
            ModHandler.removeRecipeByName("minecraft:stonebrick");
            ModHandler.removeRecipeByName("minecraft:chiseled_stonebrick");
            ModHandler.removeRecipeByName("minecraft:purpur_pillar");
            ModHandler.removeRecipeByName("minecraft:end_bricks");
            ModHandler.removeRecipeByName("minecraft:red_nether_brick");
            ModHandler.removeRecipeByName("minecraft:red_sandstone");
            ModHandler.removeRecipeByName("minecraft:chiseled_red_sandstone");
            ModHandler.removeRecipeByName("minecraft:smooth_red_sandstone");
            ModHandler.removeRecipeByName("minecraft:bookshelf");
            ModHandler.removeRecipeByName("minecraft:pillar_quartz_block");
        }
    }

    /**
     * + Replaces Vanilla Armor and Tool recipes
     */
    private static void toolArmorRecipes() {
        if (ConfigHolder.recipes.flintAndSteelRequireSteel) {
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:flint_and_steel"));
            ModHandler.addShapedRecipe("flint_and_steel", new ItemStack(Items.FLINT_AND_STEEL), "G", "F", "S",
                    'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Steel),
                    'F', new ItemStack(Items.FLINT, 1),
                    'S', new UnificationEntry(OrePrefix.springSmall, Materials.Steel)
            );
        }

        if (!ConfigHolder.recipes.hardToolArmorRecipes)
            return;

        createShovelRecipe("iron_shovel", new ItemStack(Items.IRON_SHOVEL), Materials.Iron);
        createPickaxeRecipe("iron_pickaxe", new ItemStack(Items.IRON_PICKAXE), Materials.Iron);
        createAxeRecipe("iron_axe", new ItemStack(Items.IRON_AXE), Materials.Iron);
        createSwordRecipe("iron_sword", new ItemStack(Items.IRON_SWORD), Materials.Iron);
        createHoerecipe("iron_hoe", new ItemStack(Items.IRON_HOE), Materials.Iron);
        createHelmetRecipe("iron_helmet", new ItemStack(Items.IRON_HELMET), Materials.Iron);
        createChestplateRecipe("iron_chestplate", new ItemStack(Items.IRON_CHESTPLATE), Materials.Iron);
        createLeggingsRecipe("iron_leggings", new ItemStack(Items.IRON_LEGGINGS), Materials.Iron);
        createBootsRecipe("iron_boots", new ItemStack(Items.IRON_BOOTS), Materials.Iron);

        createShovelRecipe("golden_shovel", new ItemStack(Items.GOLDEN_SHOVEL), Materials.Gold);
        createPickaxeRecipe("golden_pickaxe", new ItemStack(Items.GOLDEN_PICKAXE), Materials.Gold);
        createAxeRecipe("golden_axe", new ItemStack(Items.GOLDEN_AXE), Materials.Gold);
        createSwordRecipe("golden_sword", new ItemStack(Items.GOLDEN_SWORD), Materials.Gold);
        createHoerecipe("golden_hoe", new ItemStack(Items.GOLDEN_HOE), Materials.Gold);
        createHelmetRecipe("golden_helmet", new ItemStack(Items.GOLDEN_HELMET), Materials.Gold);
        createChestplateRecipe("golden_chestplate", new ItemStack(Items.GOLDEN_CHESTPLATE), Materials.Gold);
        createLeggingsRecipe("golden_leggings", new ItemStack(Items.GOLDEN_LEGGINGS), Materials.Gold);
        createBootsRecipe("golden_boots", new ItemStack(Items.GOLDEN_BOOTS), Materials.Gold);

        createShovelRecipe("diamond_shovel", new ItemStack(Items.DIAMOND_SHOVEL), Materials.Diamond);
        createPickaxeRecipe("diamond_pickaxe", new ItemStack(Items.DIAMOND_PICKAXE), Materials.Diamond);
        createAxeRecipe("diamond_axe", new ItemStack(Items.DIAMOND_AXE), Materials.Diamond);
        createSwordRecipe("diamond_sword", new ItemStack(Items.DIAMOND_SWORD), Materials.Diamond);
        createHoerecipe("diamond_hoe", new ItemStack(Items.DIAMOND_HOE), Materials.Diamond);
        createHelmetRecipe("diamond_helmet", new ItemStack(Items.DIAMOND_HELMET), Materials.Diamond);
        createChestplateRecipe("diamond_chestplate", new ItemStack(Items.DIAMOND_CHESTPLATE), Materials.Diamond);
        createLeggingsRecipe("diamond_leggings", new ItemStack(Items.DIAMOND_LEGGINGS), Materials.Diamond);
        createBootsRecipe("diamond_boots", new ItemStack(Items.DIAMOND_BOOTS), Materials.Diamond);

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:compass"));
        ModHandler.addShapedRecipe("compass", new ItemStack(Items.COMPASS), "SGB", "RPR", "AdS",
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'G', new ItemStack(Blocks.GLASS_PANE, 1, GTValues.W),
                'B', new UnificationEntry(OrePrefix.bolt, Materials.IronMagnetic),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Zinc),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'A', new UnificationEntry(OrePrefix.bolt, Materials.RedAlloy)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Iron)
                .input(OrePrefix.ring, Materials.Zinc)
                .input(OrePrefix.bolt, Materials.RedAlloy)
                .input(OrePrefix.bolt, Materials.IronMagnetic)
                .input(OrePrefix.screw, Materials.Iron, 2)
                .outputs(new ItemStack(Items.COMPASS))
                .duration(100).EUt(16).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:fishing_rod"));
        ModHandler.addShapedRecipe("fishing_rod", new ItemStack(Items.FISHING_ROD), "  S", " SL", "SxR",
                'S', new UnificationEntry(OrePrefix.stickLong, Materials.Wood),
                'L', new ItemStack(Items.STRING),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:clock"));
        ModHandler.addShapedRecipe("clock", new ItemStack(Items.CLOCK), "RPR", "BCB", "dSw",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Gold),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Gold),
                'B', new UnificationEntry(OrePrefix.bolt, Materials.Gold),
                'C', new ItemStack(Items.COMPARATOR),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Gold)
        );

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Gold)
                .input(OrePrefix.ring, Materials.Gold)
                .input(OrePrefix.bolt, Materials.Gold, 2)
                .input(OrePrefix.screw, Materials.Gold)
                .inputs(new ItemStack(Items.COMPARATOR))
                .outputs(new ItemStack(Items.CLOCK))
                .duration(100).EUt(16).buildAndRegister();

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:shears"));
        ModHandler.addShapedRecipe("shears", new ItemStack(Items.SHEARS), "PSP", "hRf", "TdT",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Iron),
                'S', new UnificationEntry(OrePrefix.screw, Materials.Iron),
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron),
                'T', new ItemStack(Items.STICK)
        );

        ModHandler.removeRecipeByName(new ResourceLocation("minecraft:shield"));
        ModHandler.addShapedRecipe("shield", new ItemStack(Items.SHIELD), "BRB", "LPL", "BRB",
                'B', new UnificationEntry(OrePrefix.bolt, Materials.Iron),
                'R', new UnificationEntry(OrePrefix.stick, Materials.Iron),
                'L', new UnificationEntry(OrePrefix.stickLong, Materials.Iron),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Wood)
        );

        // Remove a bunch of processing recipes for tools and armor, since we have significantly better options
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HELMET, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_CHESTPLATE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_LEGGINGS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_BOOTS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HORSE_ARMOR, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_PICKAXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_SHOVEL, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_AXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_SWORD, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HOE, 1, W));

        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HELMET, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_CHESTPLATE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_LEGGINGS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_BOOTS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HORSE_ARMOR, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_PICKAXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_SHOVEL, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_AXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_SWORD, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HOE, 1, W));
    }

    private static void createShovelRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "fPh", " S ", " S ",
                'P', new UnificationEntry(OrePrefix.plate, material),
                'S', new ItemStack(Items.STICK)
        );
    }

    private static void createPickaxeRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "PII", "fSh", " S ",
                'P', new UnificationEntry(OrePrefix.plate, material),
                'I', new UnificationEntry(material.equals(Materials.Diamond) ? OrePrefix.gem : OrePrefix.ingot, material),
                'S', new ItemStack(Items.STICK)
        );
    }

    private static void createAxeRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "PIh", "PS ", "fS ",
                'P', new UnificationEntry(OrePrefix.plate, material),
                'I', new UnificationEntry(material.equals(Materials.Diamond) ? OrePrefix.gem : OrePrefix.ingot, material),
                'S', new ItemStack(Items.STICK)
        );
    }

    private static void createSwordRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, " P ", "fPh", " S ",
                'P', new UnificationEntry(OrePrefix.plate, material),
                'S', new ItemStack(Items.STICK)
        );
    }

    private static void createHoerecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "PIh", "fS ", " S ",
                'P', new UnificationEntry(OrePrefix.plate, material),
                'I', new UnificationEntry(material.equals(Materials.Diamond) ? OrePrefix.gem : OrePrefix.ingot, material),
                'S', new ItemStack(Items.STICK)
        );
    }

    private static void createHelmetRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "PPP", "PhP",
                'P', new UnificationEntry(OrePrefix.plate, material)
        );
    }

    private static void createChestplateRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "PhP", "PPP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, material)
        );
    }

    private static void createLeggingsRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "PPP", "PhP", "P P",
                'P', new UnificationEntry(OrePrefix.plate, material)
        );
    }

    private static void createBootsRecipe(String regName, ItemStack output, Material material) {
        ModHandler.removeRecipeByName(new ResourceLocation(regName));
        ModHandler.addShapedRecipe(regName, output, "P P", "PhP",
                'P', new UnificationEntry(OrePrefix.plate, material)
        );
    }
}
