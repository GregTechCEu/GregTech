package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregLog.LogVariant;
import gregtech.common.blocks.wood.BlockGregPlank;
import gregtech.common.crafting.FacadeRecipe;
import gregtech.common.items.MetaItems;
import gregtech.loaders.oreprocessing.ToolRecipeHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;

import static gregtech.api.unification.material.Materials.*;

public class CraftingRecipeLoader {

    public static void init() {
        loadCraftingRecipes();
        GTLog.logger.info("Modifying vanilla recipes according to config. DON'T BE SCARED OF FML's WARNING ABOUT DANGEROUS ALTERNATIVE PREFIX.");
        VanillaOverrideRecipes.init();
        VanillaStandardRecipes.init();
    }

    private static void loadCraftingRecipes() {
        registerFacadeRecipe(Materials.Iron, 4);

        ToolRecipeHandler.registerPowerUnitRecipes();
        ToolRecipeHandler.registerManualToolRecipes();

        ModHandler.addShapedRecipe("small_wooden_pipe", OreDictUnifier.get(OrePrefix.pipeSmallFluid, Materials.Wood, 6), "WsW", "W W", "WrW", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("normal_wooden_pipe", OreDictUnifier.get(OrePrefix.pipeNormalFluid, Materials.Wood, 2), "WWW", "s r", "WWW", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("large_wooden_pipe", OreDictUnifier.get(OrePrefix.pipeLargeFluid, Materials.Wood), "WrW", "W W", "WsW", 'W', new UnificationEntry(OrePrefix.plank, Materials.Wood));

        ModHandler.addShapelessRecipe("integrated_circuit", IntCircuitIngredient.getIntegratedCircuit(0), new UnificationEntry(OrePrefix.circuit, Tier.Basic));

        ModHandler.addShapedRecipe("item_filter", MetaItems.ITEM_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Steel));
        ModHandler.addShapedRecipe("fluid_filter_lapis", MetaItems.FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Lapis));
        ModHandler.addShapedRecipe("fluid_filter_lazurite", MetaItems.FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Lazurite));
        ModHandler.addShapedRecipe("fluid_filter_sodalite", MetaItems.FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Sodalite));

        ModHandler.addShapedRecipe("ore_dictionary_filter_olivine", MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Olivine));
        ModHandler.addShapedRecipe("ore_dictionary_filter_emerald", MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y', new UnificationEntry(OrePrefix.plate, Materials.Emerald));

        ModHandler.addShapedRecipe("smart_item_filter_olivine", MetaItems.SMART_FILTER.getStackForm(), "XEX", "XCX", "XEX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'C', new UnificationEntry(OrePrefix.circuit, Tier.Basic), 'E', new UnificationEntry(OrePrefix.plate, Materials.Olivine));
        ModHandler.addShapedRecipe("smart_item_filter_emerald", MetaItems.SMART_FILTER.getStackForm(), "XEX", "XCX", "XEX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'C', new UnificationEntry(OrePrefix.circuit, Tier.Basic), 'E', new UnificationEntry(OrePrefix.plate, Materials.Emerald));

        ModHandler.addShapedRecipe("plank_to_wooden_shape", MetaItems.WOODEN_FORM_EMPTY.getStackForm(), "   ", " X ", "s  ", 'X', new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("wooden_shape_brick", MetaItems.WOODEN_FORM_BRICK.getStackForm(), "k ", " X", 'X', MetaItems.WOODEN_FORM_EMPTY.getStackForm());

        if (ConfigHolder.recipes.hardMiscRecipes) {
            ModHandler.addShapelessRecipe("compressed_clay", MetaItems.COMPRESSED_CLAY.getStackForm(), MetaItems.WOODEN_FORM_BRICK.getStackForm(), new ItemStack(Items.CLAY_BALL));
            ModHandler.addSmeltingRecipe(MetaItems.COMPRESSED_CLAY.getStackForm(), new ItemStack(Items.BRICK));
        }

        ModHandler.addShapedRecipe("compressed_coke_clay", MetaItems.COMPRESSED_COKE_CLAY.getStackForm(3), "XXX", "SYS", "SSS", 'Y', MetaItems.WOODEN_FORM_BRICK.getStackForm(), 'X', new ItemStack(Items.CLAY_BALL), 'S', "sand");
        ModHandler.addShapelessRecipe("fireclay_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Fireclay, 2), new UnificationEntry(OrePrefix.dust, Materials.Brick), new UnificationEntry(OrePrefix.dust, Materials.Clay));
        ModHandler.addSmeltingRecipe(MetaItems.COMPRESSED_COKE_CLAY.getStackForm(), MetaItems.COKE_OVEN_BRICK.getStackForm());
        ModHandler.addSmeltingRecipe(MetaItems.COMPRESSED_FIRECLAY.getStackForm(), MetaItems.FIRECLAY_BRICK.getStackForm());

        ModHandler.addSmeltingRecipe(new UnificationEntry(OrePrefix.nugget, Materials.Iron), OreDictUnifier.get(OrePrefix.nugget, Materials.WroughtIron));

        ModHandler.addShapedRecipe("clipboard", MetaItems.CLIPBOARD.getStackForm(), " Sd", "BWR", "PPP", 'P', Items.PAPER, 'R', new UnificationEntry(OrePrefix.springSmall, Iron), 'B', new UnificationEntry(OrePrefix.bolt, Iron), 'S', new UnificationEntry(OrePrefix.screw, Iron), 'W', new UnificationEntry(OrePrefix.plate, Wood));

        for (Material material : new Material[]{Materials.Lapis, Materials.Lazurite, Materials.Sodalite}) {
            String recipeName = "lapotron_crystal_" + material.toString();
            ModHandler.addShapedEnergyTransferRecipe(recipeName, MetaItems.LAPOTRON_CRYSTAL.getStackForm(),
                    Ingredient.fromStacks(MetaItems.ENERGIUM_CRYSTAL.getStackForm()), false, false,
                    "XCX", "RER", "XCX",
                    'X', new UnificationEntry(OrePrefix.plate, material),
                    'R', new UnificationEntry(OrePrefix.stick, material),
                    'C', new UnificationEntry(OrePrefix.circuit, Tier.Extreme),
                    'E', MetaItems.ENERGIUM_CRYSTAL.getStackForm());

            ModHandler.addShapelessRecipe(recipeName + "_alt", MetaItems.LAPOTRON_CRYSTAL.getStackForm(),
                    new UnificationEntry(OrePrefix.gemExquisite, Materials.Sapphire),
                    new UnificationEntry(OrePrefix.stick, material),
                    MetaItems.CAPACITOR.getStackForm());
        }

        ModHandler.addShapelessRecipe("rubber_wood_planks", new ItemStack(MetaBlocks.PLANKS, 4, BlockGregPlank.PlankVariant.RUBBER_PLANK.ordinal()), new ItemStack(MetaBlocks.LOG, 1, LogVariant.RUBBER_WOOD.ordinal()));

        ModHandler.addShapedRecipe("rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.Rubber), "k", "X", 'X', new UnificationEntry(OrePrefix.plate, Materials.Rubber));
        ModHandler.addShapedRecipe("silicone_rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.SiliconeRubber), "k", "P", 'P', OreDictUnifier.get(OrePrefix.plate, Materials.SiliconeRubber));
        ModHandler.addShapedRecipe("styrene_rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.StyreneButadieneRubber), "k", "P", 'P', OreDictUnifier.get(OrePrefix.plate, Materials.StyreneButadieneRubber));

        ModHandler.addShapelessRecipe("iron_magnetic_stick", OreDictUnifier.get(OrePrefix.stick, Materials.IronMagnetic), new UnificationEntry(OrePrefix.stick, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Redstone), new UnificationEntry(OrePrefix.dust, Materials.Redstone), new UnificationEntry(OrePrefix.dust, Materials.Redstone), new UnificationEntry(OrePrefix.dust, Materials.Redstone));

        // Dusts
        ModHandler.addShapelessRecipe("dust_ferrite_mixture", OreDictUnifier.get(OrePrefix.dust, Materials.FerriteMixture, 5), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.Zinc), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron));
        ModHandler.addShapelessRecipe("dust_brass", OreDictUnifier.get(OrePrefix.dust, Materials.Brass, 3), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Zinc));
        ModHandler.addShapelessRecipe("dust_bronze", OreDictUnifier.get(OrePrefix.dust, Materials.Bronze, 3), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Copper), new UnificationEntry(OrePrefix.dust, Materials.Tin));
        ModHandler.addShapelessRecipe("dust_cobalt_brass", OreDictUnifier.get(OrePrefix.dust, Materials.CobaltBrass, 8), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Brass), new UnificationEntry(OrePrefix.dust, Materials.Aluminium), new UnificationEntry(OrePrefix.dust, Materials.Cobalt));
        ModHandler.addShapelessRecipe("dust_stainless_steel", OreDictUnifier.get(OrePrefix.dust, Materials.StainlessSteel, 8), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Iron), new UnificationEntry(OrePrefix.dust, Materials.Nickel), new UnificationEntry(OrePrefix.dust, Materials.Manganese), new UnificationEntry(OrePrefix.dust, Materials.Chrome));

        ModHandler.addShapedRecipe("component_grinder_diamond", MetaItems.COMPONENT_GRINDER_DIAMOND.getStackForm(), "XSX", "SDS", "XSX", 'X', new UnificationEntry(OrePrefix.dust, Materials.Diamond), 'S', new UnificationEntry(OrePrefix.plateDouble, Materials.Steel), 'D', new UnificationEntry(OrePrefix.gem, Materials.Diamond));
        ModHandler.addShapedRecipe("component_grinder_tungsten", MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm(), "WSW", "SDS", "WSW", 'W', new UnificationEntry(OrePrefix.plate, Materials.Tungsten), 'S', new UnificationEntry(OrePrefix.plateDouble, Materials.VanadiumSteel), 'D', new UnificationEntry(OrePrefix.gem, Materials.Diamond));

        ModHandler.addShapedRecipe("nano_saber", MetaItems.NANO_SABER.getStackForm(), "PIC", "PIC", "XEX", 'P', new UnificationEntry(OrePrefix.plate, Materials.Platinum), 'I', new UnificationEntry(OrePrefix.plate, Ruridit), 'C', MetaItems.CARBON_PLATE.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, Tier.Extreme), 'E', MetaItems.ENERGIUM_CRYSTAL.getStackForm());

        ModHandler.addShapedRecipe("solar_panel/solar_panel_basic", MetaItems.COVER_SOLAR_PANEL.getStackForm(), "WGW", "CPC", 'W', MetaItems.SILICON_WAFER.getStackForm(), 'G', "paneGlass", 'C', new UnificationEntry(OrePrefix.circuit, Tier.Basic), 'P', MetaItems.CARBON_PLATE.getStackForm());
        ModHandler.addShapedRecipe("solar_panel/solar_panel_ulv", MetaItems.COVER_SOLAR_PANEL_ULV.getStackForm(), "WGW", "CAC", "P P", 'W', MetaItems.GLOWSTONE_WAFER.getStackForm(), 'G', "paneGlass", 'C', new UnificationEntry(OrePrefix.circuit, Tier.Advanced), 'P', OreDictUnifier.get(OrePrefix.plate, GalliumArsenide), 'A', OreDictUnifier.get(OrePrefix.wireGtQuadruple, Graphene));
        ModHandler.addShapedRecipe("solar_panel/solar_panel_lv", MetaItems.COVER_SOLAR_PANEL_LV.getStackForm(), "WGW", "CAC", "P P", 'W', MetaItems.NAQUADAH_WAFER.getStackForm(), 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                BlockGlassCasing.CasingType.TEMPERED_GLASS), 'C', new UnificationEntry(OrePrefix.circuit, Tier.Master), 'P', OreDictUnifier.get(OrePrefix.plate, IndiumGalliumPhosphide), 'A', OreDictUnifier.get(OrePrefix.wireGtHex, Graphene));

        ModHandler.addShapedRecipe("universal_fluid_cell", MetaItems.FLUID_CELL_UNIVERSAL.getStackForm(), "C", 'C', MetaItems.FLUID_CELL);
        ModHandler.addShapedRecipe("universal_fluid_cell_revert", MetaItems.FLUID_CELL.getStackForm(), "C", 'C', MetaItems.FLUID_CELL_UNIVERSAL);

        ///////////////////////////////////////////////////
        //               Shapes and Molds                //
        ///////////////////////////////////////////////////
        ModHandler.addShapedRecipe("shape/shape_empty", MetaItems.SHAPE_EMPTY.getStackForm(), "hf", "PP", "PP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel));

        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_bottle", MetaItems.SHAPE_EXTRUDER_BOTTLE.getStackForm(), "  x", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_gear", MetaItems.SHAPE_EXTRUDER_GEAR.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_saw", MetaItems.SHAPE_EXTRUDER_SAW.getStackForm(), "   ", " S ", "  x", 'S', MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_file", MetaItems.SHAPE_EXTRUDER_FILE.getStackForm(), "  x", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_hammer", MetaItems.SHAPE_EXTRUDER_HAMMER.getStackForm(), "  x", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_INGOT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_hoe", MetaItems.SHAPE_EXTRUDER_HOE.getStackForm(), "   ", " Sx", "   ", 'S', MetaItems.SHAPE_EXTRUDER_INGOT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_axe", MetaItems.SHAPE_EXTRUDER_AXE.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_shovel", MetaItems.SHAPE_EXTRUDER_SHOVEL.getStackForm(), " x ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_pickaxe", MetaItems.SHAPE_EXTRUDER_PICKAXE.getStackForm(), " x ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_INGOT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_sword", MetaItems.SHAPE_EXTRUDER_SWORD.getStackForm(), "   ", " Sx", "   ", 'S', MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_block", MetaItems.SHAPE_EXTRUDER_BLOCK.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_INGOT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_pipe_huge", MetaItems.SHAPE_EXTRUDER_PIPE_HUGE.getStackForm(), "   ", " S ", "  x", 'S', MetaItems.SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_pipe_large", MetaItems.SHAPE_EXTRUDER_PIPE_LARGE.getStackForm(), "   ", " Sx", "   ", 'S', MetaItems.SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_pipe_normal", MetaItems.SHAPE_EXTRUDER_PIPE_NORMAL.getStackForm(), "  x", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_pipe_small", MetaItems.SHAPE_EXTRUDER_PIPE_SMALL.getStackForm(), " x ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_pipe_tiny", MetaItems.SHAPE_EXTRUDER_PIPE_TINY.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_wire", MetaItems.SHAPE_EXTRUDER_WIRE.getStackForm(), " x ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_ROD.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_ingot", MetaItems.SHAPE_EXTRUDER_INGOT.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_cell", MetaItems.SHAPE_EXTRUDER_CELL.getStackForm(), "   ", " Sx", "   ", 'S', MetaItems.SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_ring", MetaItems.SHAPE_EXTRUDER_RING.getStackForm(), "   ", " S ", " x ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_bolt", MetaItems.SHAPE_EXTRUDER_BOLT.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_ROD.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_rod", MetaItems.SHAPE_EXTRUDER_ROD.getStackForm(), "   ", " Sx", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_rod_long", MetaItems.SHAPE_EXTRUDER_ROD_LONG.getStackForm(), "  x", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_ROD.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_plate", MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm(), "x  ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_FOIL.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_gear_small", MetaItems.SHAPE_EXTRUDER_GEAR_SMALL.getStackForm(), " x ", " S ", "   ", 'S', MetaItems.SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_foil", MetaItems.SHAPE_EXTRUDER_FOIL.getStackForm(), "   ", " S ", "  x", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/extruder/shape_extruder_rotor", MetaItems.SHAPE_EXTRUDER_ROTOR.getStackForm(), "   ", " S ", "x  ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());

        ModHandler.addShapedRecipe("shape/mold/shape_mold_rotor", MetaItems.SHAPE_MOLD_ROTOR.getStackForm(), "  h", " S ", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_gear_small", MetaItems.SHAPE_MOLD_GEAR_SMALL.getStackForm(), "   ", "   ", "h S", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_name", MetaItems.SHAPE_MOLD_NAME.getStackForm(), "  S", "   ", "h  ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_anvil", MetaItems.SHAPE_MOLD_ANVIL.getStackForm(), "  S", "   ", " h ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_cylinder", MetaItems.SHAPE_MOLD_CYLINDER.getStackForm(), "  S", "   ", "  h", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_nugget", MetaItems.SHAPE_MOLD_NUGGET.getStackForm(), "S h", "   ", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_block", MetaItems.SHAPE_MOLD_BLOCK.getStackForm(), "   ", "hS ", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_ball", MetaItems.SHAPE_MOLD_BALL.getStackForm(), "   ", " S ", "h  ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_ingot", MetaItems.SHAPE_MOLD_INGOT.getStackForm(), "   ", " S ", " h ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_bottle", MetaItems.SHAPE_MOLD_BOTTLE.getStackForm(), "   ", " S ", "  h", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_credit", MetaItems.SHAPE_MOLD_CREDIT.getStackForm(), "h  ", " S ", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_gear", MetaItems.SHAPE_MOLD_GEAR.getStackForm(), "   ", " Sh", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape/mold/shape_mold_plate", MetaItems.SHAPE_MOLD_PLATE.getStackForm(), " h ", " S ", "   ", 'S', MetaItems.SHAPE_EMPTY.getStackForm());

        ///////////////////////////////////////////////////
        //                   Credits                     //
        ///////////////////////////////////////////////////
        ModHandler.addShapelessRecipe("coin_chocolate", MetaItems.COIN_CHOCOLATE.getStackForm(), new UnificationEntry(OrePrefix.dust, Materials.Cocoa), new UnificationEntry(OrePrefix.foil, Materials.Gold), new ItemStack(Items.MILK_BUCKET), new UnificationEntry(OrePrefix.dust, Materials.Sugar));

        ModHandler.addShapelessRecipe("credit/credit_copper", MetaItems.CREDIT_COPPER.getStackForm(8), MetaItems.CREDIT_CUPRONICKEL.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_cupronickel_alt", MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm(), MetaItems.CREDIT_COPPER.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_cupronickel", MetaItems.CREDIT_CUPRONICKEL.getStackForm(8), MetaItems.CREDIT_SILVER.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_silver_alt", MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm(), MetaItems.CREDIT_CUPRONICKEL.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_silver", MetaItems.CREDIT_SILVER.getStackForm(8), MetaItems.CREDIT_GOLD.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_gold_alt", MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm(), MetaItems.CREDIT_SILVER.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_gold", MetaItems.CREDIT_GOLD.getStackForm(8), MetaItems.CREDIT_PLATINUM.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_platinum_alt", MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm(), MetaItems.CREDIT_GOLD.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_platinum", MetaItems.CREDIT_PLATINUM.getStackForm(8), MetaItems.CREDIT_OSMIUM.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_osmium_alt", MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm(), MetaItems.CREDIT_PLATINUM.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_osmium", MetaItems.CREDIT_OSMIUM.getStackForm(8), MetaItems.CREDIT_NAQUADAH.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_naquadah_alt", MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm(), MetaItems.CREDIT_OSMIUM.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_naquadah", MetaItems.CREDIT_NAQUADAH.getStackForm(8), MetaItems.CREDIT_NEUTRONIUM.getStackForm());
        ModHandler.addShapelessRecipe("credit/credit_darmstadtium", MetaItems.CREDIT_NEUTRONIUM.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm(), MetaItems.CREDIT_NAQUADAH.getStackForm());
    }

    private static void registerFacadeRecipe(Material material, int facadeAmount) {
        OreIngredient ingredient = new OreIngredient(new UnificationEntry(OrePrefix.plate, material).toString());
        ForgeRegistries.RECIPES.register(new FacadeRecipe(null, ingredient, facadeAmount).setRegistryName("facade_" + material));
    }
}
