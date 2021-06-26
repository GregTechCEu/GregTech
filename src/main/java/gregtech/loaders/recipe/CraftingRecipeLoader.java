package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregLog.LogVariant;
import gregtech.common.crafting.FacadeRecipe;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.loaders.oreprocessing.ToolRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;

import static gregtech.api.util.DyeUtil.*;

public class CraftingRecipeLoader {

    public static void init() {
        loadCraftingRecipes();
    }

    private static void loadCraftingRecipes() {
        registerFacadeRecipe(Materials.Aluminium, 5);
        registerFacadeRecipe(Materials.WroughtIron, 3);
        registerFacadeRecipe(Materials.Iron, 2);

        ToolRecipeHandler.registerPowerUnitRecipes();
        ModHandler.addShapedRecipe("small_wooden_pipe", OreDictUnifier.get(OrePrefix.pipeSmall, Materials.Wood, 4), "WWW", "h f", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("medium_wooden_pipe", OreDictUnifier.get(OrePrefix.pipeMedium, Materials.Wood, 2), "WWW", "f h", "WWW", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood));

        ModHandler.addShapelessRecipe("nether_quartz_block_to_nether_quartz", new ItemStack(Items.QUARTZ, 4), Blocks.QUARTZ_BLOCK);
        ModHandler.addShapelessRecipe("clay_block_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Clay, 4), 'm', Blocks.CLAY);
        ModHandler.addShapelessRecipe("clay_ball_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Clay), 'm', Items.CLAY_BALL);
        ModHandler.addShapelessRecipe("brick_block_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Brick, 4), 'm', Blocks.BRICK_BLOCK);
        ModHandler.addShapelessRecipe("brick_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Brick), 'm', Items.BRICK);
        ModHandler.addShapelessRecipe("wheat_to_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Wheat, 1), 'm', Items.WHEAT);
        ModHandler.addShapelessRecipe("gravel_to_flint", new ItemStack(Items.FLINT, 1), 'm', Blocks.GRAVEL);
        ModHandler.addShapelessRecipe("bone_to_bone_meal", new ItemStack(Items.DYE, 4, 15), 'm', Items.BONE);
        ModHandler.addShapelessRecipe("blaze_rod_to_powder", new ItemStack(Items.BLAZE_POWDER, 3), 'm', Items.BLAZE_ROD);
        ModHandler.addShapelessRecipe("integrated_circuit", IntCircuitIngredient.getIntegratedCircuit(0), new UnificationEntry(OrePrefix.circuit, Tier.Basic));

        ModHandler.addShapedRecipe("item_filter", MetaItems.ITEM_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Steel));
        ModHandler.addShapedRecipe("fluid_filter", MetaItems.FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Lapis));

        ModHandler.addShapedRecipe("ore_dictionary_filter_olivine", MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Olivine));
        ModHandler.addShapedRecipe("ore_dictionary_filter_emerald", MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Emerald));

        ModHandler.addShapedRecipe("smart_item_filter_olivine", MetaItems.SMART_FILTER.getStackForm(), "XEX", "XCX", "XEX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'C', new UnificationEntry(OrePrefix.circuit, Tier.Basic), 'E', new UnificationEntry(OrePrefix.plate, Materials.Olivine));
        ModHandler.addShapedRecipe("smart_item_filter_emerald", MetaItems.SMART_FILTER.getStackForm(), "XEX", "XCX", "XEX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'C', new UnificationEntry(OrePrefix.circuit, Tier.Basic), 'E', new UnificationEntry(OrePrefix.plate, Materials.Emerald));

        ModHandler.addShapedRecipe("plank_to_wooden_shape", MetaItems.WOODEN_FORM_EMPTY.getStackForm(), "   ", " X ", "s  ", 'X', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("wooden_shape_brick", MetaItems.WOODEN_FORM_BRICK.getStackForm(), "k ", " X", 'X', MetaItems.WOODEN_FORM_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("compressed_clay", MetaItems.COMPRESSED_CLAY.getStackForm(8), "XXX", "XYX", "XXX", 'Y', MetaItems.WOODEN_FORM_BRICK.getStackForm(), 'X', Items.CLAY_BALL);
        ModHandler.addShapelessRecipe("fireclay_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Fireclay, 2), new UnificationEntry(OrePrefix.dust, Materials.Brick), new UnificationEntry(OrePrefix.dust, Materials.Clay));
        ModHandler.addSmeltingRecipe(MetaItems.COMPRESSED_CLAY.getStackForm(), MetaItems.COKE_OVEN_BRICK.getStackForm());
        ModHandler.addSmeltingRecipe(MetaItems.COMPRESSED_FIRECLAY.getStackForm(), MetaItems.FIRECLAY_BRICK.getStackForm());

        ModHandler.addSmeltingRecipe(new UnificationEntry(OrePrefix.nugget, Materials.Iron), OreDictUnifier.get(OrePrefix.nugget, Materials.WroughtIron));

        for (MetaValueItem batteryItem : ToolRecipeHandler.batteryItems[0]) {
            ModHandler.addShapedEnergyTransferRecipe("scanner_" + batteryItem.unlocalizedName, MetaItems.SCANNER.getStackForm(),
                batteryItem::isItemEqual, true,
                "DGD", "CGC", "SBS",
                'D', new UnificationEntry(OrePrefix.plate, Materials.Diamond),
                'G', new UnificationEntry(OrePrefix.paneGlass),
                'C', new UnificationEntry(OrePrefix.circuit, Tier.Basic),
                'S', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                'B', batteryItem.getStackForm());
        }

        for(Material material : new Material[] {Materials.Lapis, Materials.Lazurite, Materials.Sodalite}) {
            String recipeName = "lapotron_crystal_" + material.toString();
            ModHandler.addShapedEnergyTransferRecipeWithOverride(recipeName, MetaItems.LAPOTRON_CRYSTAL.getStackForm(),
                Ingredient.fromStacks(MetaItems.ENERGY_CRYSTAL.getStackForm()), false, false,
                "XCX", "XEX", "XCX",
                'X', new UnificationEntry(OrePrefix.plate, material),
                'C', new UnificationEntry(OrePrefix.circuit, Tier.Advanced),
                'E', MetaItems.ENERGY_CRYSTAL.getStackForm());
        }

        ModHandler.addShapelessRecipe("rubber_wood_planks", new ItemStack(Blocks.PLANKS, 4, EnumType.JUNGLE.getMetadata()), new ItemStack(MetaBlocks.LOG, 1, LogVariant.RUBBER_WOOD.ordinal()));

        ModHandler.addShapedRecipe("paper_ring", OreDictUnifier.get(OrePrefix.ring, Materials.Paper), "k", "X", 'X', new UnificationEntry(OrePrefix.plate, Materials.Paper));
        ModHandler.addShapedRecipe("rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.Rubber), "k", "X", 'X', new UnificationEntry(OrePrefix.plate, Materials.Rubber));
        ModHandler.addShapedRecipe("silicone_rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.SiliconeRubber), "k", "P", 'P', OreDictUnifier.get(OrePrefix.plate, Materials.SiliconeRubber));
        ModHandler.addShapedRecipe("styrene_rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.StyreneButadieneRubber), "k", "P", 'P', OreDictUnifier.get(OrePrefix.plate, Materials.StyreneButadieneRubber));

        ModHandler.addShapedRecipe("rubber_drop_torch", new ItemStack(Blocks.TORCH, 3), "X", "Y", 'X', MetaItems.RUBBER_DROP, 'Y', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("lignite_coal_torch", new ItemStack(Blocks.TORCH, 4), "X", "Y", 'X', new UnificationEntry(OrePrefix.gem, Materials.Lignite), 'Y', new UnificationEntry(OrePrefix.stick, Materials.Wood));

        ModHandler.addShapelessRecipe("iron_magnetic_stick", OreDictUnifier.get(OrePrefix.stick, Materials.IronMagnetic), new UnificationEntry(OrePrefix.stick, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Redstone), new UnificationEntry(OrePrefix.dust, Materials.Redstone), new UnificationEntry(OrePrefix.dust, Materials.Redstone), new UnificationEntry(OrePrefix.dust, Materials.Redstone));

        ModHandler.addShapedRecipe("torch_sulfur", new ItemStack(Blocks.TORCH, 2), "C", "S", 'C', new UnificationEntry(OrePrefix.dust, Materials.Sulfur), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));
        ModHandler.addShapedRecipe("torch_phosphor", new ItemStack(Blocks.TORCH, 6), "C", "S", 'C', new UnificationEntry(OrePrefix.dust, Materials.Phosphorus), 'S', new UnificationEntry(OrePrefix.stick, Materials.Wood));

        ModHandler.addShapedRecipe("piston_bronze", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood), 'C', OrePrefix.stoneCobble, 'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone), 'B', new UnificationEntry(OrePrefix.ingot, Materials.Bronze));
        ModHandler.addShapedRecipe("piston_aluminium", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood), 'C', OrePrefix.stoneCobble, 'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone), 'B', new UnificationEntry(OrePrefix.ingot, Materials.Aluminium));
        ModHandler.addShapedRecipe("piston_steel", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood), 'C', OrePrefix.stoneCobble, 'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone), 'B', new UnificationEntry(OrePrefix.ingot, Materials.Steel));
        ModHandler.addShapedRecipe("piston_titanium", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood), 'C', OrePrefix.stoneCobble, 'R', new UnificationEntry(OrePrefix.dust, Materials.Redstone), 'B', new UnificationEntry(OrePrefix.ingot, Materials.Titanium));

        ModHandler.addShapelessRecipe("dynamite", MetaItems.DYNAMITE.getStackForm(), Items.STRING, Items.PAPER, Items.GUNPOWDER);
        GTLog.logger.info("Modifying vanilla recipes according to config. DON'T BE SCARED OF FML's WARNING ABOUT DANGEROUS ALTERNATIVE PREFIX.");

        if (ConfigHolder.vanillaRecipes.bucketRequirePlatesAndHammer) {
            ModHandler.addShapedRecipe("iron_bucket", new ItemStack(Items.BUCKET), "XhX", " X ", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:bucket"));
        }
        if (ConfigHolder.vanillaRecipes.ironConsumingCraftingRecipesRequirePlates) {
            ModHandler.addShapedRecipe("iron_pressure_plate", new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), "XXh", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:heavy_weighted_pressure_plate"));

            ModHandler.addShapedRecipe("gold_pressure_plate", new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), "XXh", 'X', new UnificationEntry(OrePrefix.plate, Materials.Gold));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:light_weighted_pressure_plate"));

            ModHandler.addShapedRecipe("iron_door", new ItemStack(Items.IRON_DOOR, 3), "XX ", "XXh", "XX ", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_door"));

            ModHandler.addShapedRecipe("iron_trapdoor", new ItemStack(Blocks.IRON_TRAPDOOR), "XX ", "XXh", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_trapdoor"));

            ModHandler.addShapedRecipe("cauldron", new ItemStack(Items.CAULDRON), "X X", "XhX", "XXX", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:cauldron"));

            ModHandler.addShapedRecipe("hopper", new ItemStack(Blocks.HOPPER), "XwX", "XCX", " X ", 'X', new UnificationEntry(OrePrefix.plate, Materials.Iron), 'C', "chestWood");
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:hopper"));

            ModHandler.addShapedRecipe("iron_bars", new ItemStack(Blocks.IRON_BARS, 8), " w ", "XXX", "XXX", 'X', new UnificationEntry(OrePrefix.stick, Materials.Iron));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:iron_bars"));
        }

        if (ConfigHolder.vanillaRecipes.bowlRequireKnife) {
            ModHandler.addShapedRecipe("bowl", new ItemStack(Items.BOWL), "k", "X", 'X', new UnificationEntry(OrePrefix.plank, Materials.Wood));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:bowl"));
        }

        if (ConfigHolder.vanillaRecipes.nerfStickCrafting) {
            ModHandler.addShapedRecipe("stick_saw", new ItemStack(Items.STICK, 4), "s", "P", "P", 'P', new UnificationEntry(OrePrefix.plank, Materials.Wood));
            ModHandler.addShapedRecipe("stick_normal", new ItemStack(Items.STICK, 2), "P", "P", 'P', new UnificationEntry(OrePrefix.plank, Materials.Wood));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:stick"));
        }

        ModHandler.addShapelessRecipe("borosilicate_glass", OreDictUnifier.get(OrePrefix.dust, Materials.BorosilicateGlass, 8), new UnificationEntry(OrePrefix.dust, Materials.Boron), new UnificationEntry(OrePrefix.dust, Materials.Glass), new UnificationEntry(OrePrefix.dust, Materials.Glass), new UnificationEntry(OrePrefix.dust, Materials.Glass), new UnificationEntry(OrePrefix.dust, Materials.Glass), new UnificationEntry(OrePrefix.dust, Materials.Glass), new UnificationEntry(OrePrefix.dust, Materials.Glass), new UnificationEntry(OrePrefix.dust, Materials.Glass));
        ModHandler.addShapelessRecipe("dust_ferrite_mixture", OreDictUnifier.get(OrePrefix.dust, Materials.FerriteMixture, 6), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.Zinc), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron));
        ModHandler.addShapelessRecipe("dust_indium_gallium_phosphide", OreDictUnifier.get(OrePrefix.dust, Materials.IndiumGalliumPhosphide, 3), new UnificationEntry(OrePrefix.dust, Materials.Indium), new UnificationEntry(OrePrefix.dust, Materials.Gallium), new UnificationEntry(OrePrefix.dust, Materials.Phosphorus));
        ModHandler.addShapelessRecipe("dust_electrum", OreDictUnifier.get(OrePrefix.dust, Materials.Electrum, 2), new UnificationEntry(OrePrefix.dust, Materials.Silver), new UnificationEntry(OrePrefix.dust, Materials.Gold));
        ModHandler.addShapelessRecipe("dust_brass", OreDictUnifier.get(OrePrefix.dust, Materials.Brass, 4), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Zinc));
        ModHandler.addShapelessRecipe("dust_bronze", OreDictUnifier.get(OrePrefix.dust, Materials.Bronze, 4), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Tin));
        ModHandler.addShapelessRecipe("dust_invar", OreDictUnifier.get(OrePrefix.dust, Materials.Invar, 3), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Nickel));
        ModHandler.addShapelessRecipe("dust_cupronickel", OreDictUnifier.get(OrePrefix.dust, Materials.Cupronickel, 2), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.Copper));

        ModHandler.addShapelessRecipe("dust_rose_gold", OreDictUnifier.get(OrePrefix.dust, Materials.RoseGold, 5), new UnificationEntry(OrePrefix.dust, Materials.Gold), new UnificationEntry(OrePrefix.dust, Materials.Gold), new UnificationEntry(OrePrefix.dust, Materials.Gold), new UnificationEntry(OrePrefix.dust, Materials.Gold), new UnificationEntry(OrePrefix.dust, Materials.Copper));
        ModHandler.addShapelessRecipe("dust_sterling_silver", OreDictUnifier.get(OrePrefix.dust, Materials.SterlingSilver, 5), new UnificationEntry(OrePrefix.dust, Materials.Silver), new UnificationEntry(OrePrefix.dust, Materials.Silver), new UnificationEntry(OrePrefix.dust, Materials.Silver), new UnificationEntry(OrePrefix.dust, Materials.Silver), new UnificationEntry(OrePrefix.dust, Materials.Copper));
        ModHandler.addShapelessRecipe("dust_black_bronze", OreDictUnifier.get(OrePrefix.dust, Materials.BlackBronze, 5), new UnificationEntry(OrePrefix.dust, Materials.Gold), new UnificationEntry(OrePrefix.dust, Materials.Silver), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper));
        ModHandler.addShapelessRecipe("dust_bismuth_bronze", OreDictUnifier.get(OrePrefix.dust, Materials.BismuthBronze, 5), new UnificationEntry(OrePrefix.dust, Materials.Bismuth), new UnificationEntry(OrePrefix.dust, Materials.Zinc), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper));
        ModHandler.addShapelessRecipe("dust_black_steel", OreDictUnifier.get(OrePrefix.dust, Materials.BlackSteel, 5), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.BlackBronze), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel));
        ModHandler.addShapelessRecipe("dust_red_steel", OreDictUnifier.get(OrePrefix.dust, Materials.RedSteel, 8), new UnificationEntry(OrePrefix.dust, Materials.SterlingSilver), new UnificationEntry(OrePrefix.dust, Materials.BismuthBronze), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel));
        ModHandler.addShapelessRecipe("dust_blue_steel", OreDictUnifier.get(OrePrefix.dust, Materials.BlueSteel, 8), new UnificationEntry(OrePrefix.dust, Materials.RoseGold), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel), new UnificationEntry(OrePrefix.dust, Materials.BlackSteel));

        ModHandler.addShapelessRecipe("dust_ultimet", OreDictUnifier.get(OrePrefix.dust, Materials.Ultimet, 9), new UnificationEntry(OrePrefix.dust, Materials.Cobalt), new UnificationEntry(OrePrefix.dust, Materials.Cobalt), new UnificationEntry(OrePrefix.dust, Materials.Cobalt), new UnificationEntry(OrePrefix.dust, Materials.Cobalt), new UnificationEntry(OrePrefix.dust, Materials.Cobalt), new UnificationEntry(OrePrefix.dust, Materials.Chrome), new UnificationEntry(OrePrefix.dust, Materials.Chrome), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.Molybdenum));
        ModHandler.addShapelessRecipe("dust_cobalt_brass", OreDictUnifier.get(OrePrefix.dust, Materials.CobaltBrass, 9), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Aluminium), new UnificationEntry(OrePrefix.dust, Materials.Cobalt));
        ModHandler.addShapelessRecipe("dust_stainless_steel", OreDictUnifier.get(OrePrefix.dust, Materials.StainlessSteel, 9), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.Manganese), new UnificationEntry(OrePrefix.dust, Materials.Chrome));
        ModHandler.addShapelessRecipe("dust_kanthal", OreDictUnifier.get(OrePrefix.dust, Materials.Kanthal, 3), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Aluminium), new UnificationEntry(OrePrefix.dust, Materials.Chrome));

        ModHandler.addShapelessRecipe("dust_tiny_ultimet", OreDictUnifier.get(OrePrefix.dust, Materials.Ultimet, 1), new UnificationEntry(OrePrefix.dustTiny, Materials.Cobalt), new UnificationEntry(OrePrefix.dustTiny, Materials.Cobalt), new UnificationEntry(OrePrefix.dustTiny, Materials.Cobalt), new UnificationEntry(OrePrefix.dustTiny, Materials.Cobalt), new UnificationEntry(OrePrefix.dustTiny, Materials.Cobalt), new UnificationEntry(OrePrefix.dustTiny, Materials.Chrome), new UnificationEntry(OrePrefix.dustTiny, Materials.Chrome), new UnificationEntry(OrePrefix.dustTiny, Materials.Nickel), new UnificationEntry(OrePrefix.dustTiny, Materials.Molybdenum));
        ModHandler.addShapelessRecipe("dust_tiny_cobalt_brass", OreDictUnifier.get(OrePrefix.dust, Materials.CobaltBrass, 1), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Brass), new UnificationEntry(OrePrefix.dustTiny, Materials.Aluminium), new UnificationEntry(OrePrefix.dustTiny, Materials.Cobalt));
        ModHandler.addShapelessRecipe("dust_tiny_stainless_steel", OreDictUnifier.get(OrePrefix.dust, Materials.StainlessSteel, 1), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Nickel), new UnificationEntry(OrePrefix.dustTiny, Materials.Manganese), new UnificationEntry(OrePrefix.dustTiny, Materials.Chrome));
        ModHandler.addShapelessRecipe("dust_tiny_kanthal", OreDictUnifier.get(OrePrefix.dustTiny, Materials.Kanthal, 3), new UnificationEntry(OrePrefix.dustTiny, Materials.Iron), new UnificationEntry(OrePrefix.dustTiny, Materials.Aluminium), new UnificationEntry(OrePrefix.dustTiny, Materials.Chrome));

        ModHandler.addShapelessRecipe("dust_vanadium_steel", OreDictUnifier.get(OrePrefix.dust, Materials.VanadiumSteel, 9), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Steel), new UnificationEntry(OrePrefix.dust, Materials.Vanadium), new UnificationEntry(OrePrefix.dust, Materials.Chrome));
        ModHandler.addShapelessRecipe("dust_hssg", OreDictUnifier.get(OrePrefix.dust, Materials.HSSG, 9), new UnificationEntry(OrePrefix.dust, Materials.TungstenSteel), new UnificationEntry(OrePrefix.dust, Materials.TungstenSteel), new UnificationEntry(OrePrefix.dust, Materials.TungstenSteel), new UnificationEntry(OrePrefix.dust, Materials.TungstenSteel), new UnificationEntry(OrePrefix.dust, Materials.TungstenSteel), new UnificationEntry(OrePrefix.dust, Materials.Chrome), new UnificationEntry(OrePrefix.dust, Materials.Molybdenum), new UnificationEntry(OrePrefix.dust, Materials.Molybdenum), new UnificationEntry(OrePrefix.dust, Materials.Vanadium));
        ModHandler.addShapelessRecipe("dust_hsse", OreDictUnifier.get(OrePrefix.dust, Materials.HSSE, 9), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.Cobalt), new UnificationEntry(OrePrefix.dust, Materials.Manganese), new UnificationEntry(OrePrefix.dust, Materials.Silicon));
        ModHandler.addShapelessRecipe("dust_hsss", OreDictUnifier.get(OrePrefix.dust, Materials.HSSS, 9), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.HSSG), new UnificationEntry(OrePrefix.dust, Materials.Iridium), new UnificationEntry(OrePrefix.dust, Materials.Iridium), new UnificationEntry(OrePrefix.dust, Materials.Osmium));

        ModHandler.addShapelessRecipe("powder_coal", new ItemStack(Items.GUNPOWDER, 6), new UnificationEntry(OrePrefix.dust, Materials.Coal), new UnificationEntry(OrePrefix.dust, Materials.Coal), new UnificationEntry(OrePrefix.dust, Materials.Coal), new UnificationEntry(OrePrefix.dust, Materials.Sulfur), new UnificationEntry(OrePrefix.dust, Materials.Saltpeter), new UnificationEntry(OrePrefix.dust, Materials.Saltpeter));
        ModHandler.addShapelessRecipe("powder_charcoal", new ItemStack(Items.GUNPOWDER, 6), new UnificationEntry(OrePrefix.dust, Materials.Charcoal), new UnificationEntry(OrePrefix.dust, Materials.Charcoal), new UnificationEntry(OrePrefix.dust, Materials.Charcoal), new UnificationEntry(OrePrefix.dust, Materials.Sulfur), new UnificationEntry(OrePrefix.dust, Materials.Saltpeter), new UnificationEntry(OrePrefix.dust, Materials.Saltpeter));
        ModHandler.addShapelessRecipe("powder_carbon", new ItemStack(Items.GUNPOWDER, 6), new UnificationEntry(OrePrefix.dust, Materials.Carbon), new UnificationEntry(OrePrefix.dust, Materials.Carbon), new UnificationEntry(OrePrefix.dust, Materials.Carbon), new UnificationEntry(OrePrefix.dust, Materials.Sulfur), new UnificationEntry(OrePrefix.dust, Materials.Saltpeter), new UnificationEntry(OrePrefix.dust, Materials.Saltpeter));

        MetaBlocks.FRAMES.values().forEach(CraftingRecipeLoader::registerColoringRecipes);

        if (ConfigHolder.vanillaRecipes.nerfPaperCrafting) {
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

        if (ConfigHolder.vanillaRecipes.flintAndSteelRequireSteel) {
            ModHandler.addShapedRecipe("flint_and_steel", new ItemStack(Items.FLINT_AND_STEEL), "S ", " F", 'F', new ItemStack(Items.FLINT, 1), 'S', new UnificationEntry(OrePrefix.nugget, Materials.Steel));
            ModHandler.removeRecipeByName(new ResourceLocation("minecraft:flint_and_steel"));
        }

    }

    private static void registerFacadeRecipe(Material material, int facadeAmount) {
        OreIngredient ingredient = new OreIngredient(new UnificationEntry(OrePrefix.plate, material).toString());
        ForgeRegistries.RECIPES.register(new FacadeRecipe(null, ingredient, facadeAmount).setRegistryName("facade_" + material));
    }

    private static void registerColoringRecipes(BlockColored block) {
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            String recipeName = String.format("%s_color_%s", block.getRegistryName().getPath(), getColorName(dyeColor));
            ModHandler.addShapedRecipe(recipeName, new ItemStack(block, 8, dyeColor.getMetadata()), "XXX", "XDX", "XXX",
                'X', new ItemStack(block, 1, GTValues.W), 'D', getOrdictColorName(dyeColor));
        }
    }
}
