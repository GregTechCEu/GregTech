package gregtech.loaders;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.M;
import static gregtech.api.GTValues.W;

public class MaterialInfoLoader {

    public static void init() {
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.CUPRONICKEL),
                new ItemMaterialInfo(new MaterialStack(Materials.Cupronickel, M * 8), // double wire
                        new MaterialStack(Materials.Bronze, M * 2), // foil
                        new MaterialStack(Materials.TinAlloy, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.KANTHAL),
                new ItemMaterialInfo(new MaterialStack(Materials.Kanthal, M * 8), // double wire
                        new MaterialStack(Materials.Aluminium, M * 2), // foil
                        new MaterialStack(Materials.Copper, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NICHROME),
                new ItemMaterialInfo(new MaterialStack(Materials.Nichrome, M * 8), // double wire
                        new MaterialStack(Materials.StainlessSteel, M * 2), // foil
                        new MaterialStack(Materials.Aluminium, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TUNGSTENSTEEL),
                new ItemMaterialInfo(new MaterialStack(Materials.TungstenSteel, M * 8), // double wire
                        new MaterialStack(Materials.VanadiumSteel, M * 2), // foil
                        new MaterialStack(Materials.Nichrome, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.HSS_G),
                new ItemMaterialInfo(new MaterialStack(Materials.HSSG, M * 8), // double wire
                        new MaterialStack(Materials.TungstenCarbide, M * 2), // foil
                        new MaterialStack(Materials.Tungsten, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NAQUADAH),
                new ItemMaterialInfo(new MaterialStack(Materials.Naquadah, M * 8), // double wire
                        new MaterialStack(Materials.Osmium, M * 2), // foil
                        new MaterialStack(Materials.TungstenSteel, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRINIUM),
                new ItemMaterialInfo(new MaterialStack(Materials.Trinium, M * 8), // double wire
                        new MaterialStack(Materials.NaquadahEnriched, M * 2), // foil
                        new MaterialStack(Materials.Naquadah, M)) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRITANIUM),
                new ItemMaterialInfo(new MaterialStack(Materials.Tritanium, M * 8), // double wire
                        new MaterialStack(Materials.Naquadria, M * 2), // foil
                        new MaterialStack(Materials.Trinium, M)) // ingot
        );

        OreDictUnifier.registerOre(MetaTileEntities.HULL[0].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.WroughtIron, M * 8), // plate
                new MaterialStack(Materials.Lead, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[1].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Steel, M * 8), // plate
                new MaterialStack(Materials.Tin, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[2].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Aluminium, M * 8), // plate
                new MaterialStack(Materials.Copper, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[3].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.StainlessSteel, M * 8), // plate
                new MaterialStack(Materials.Gold, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[4].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Titanium, M * 8), // plate
                new MaterialStack(Materials.Aluminium, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[5].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.TungstenSteel, M * 8), // plate
                new MaterialStack(Materials.Tungsten, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[6].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.RhodiumPlatedPalladium, M * 8), // plate
                new MaterialStack(Materials.NiobiumTitanium, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[7].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.NaquadahAlloy, M * 8), // plate
                new MaterialStack(Materials.VanadiumGallium, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[8].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Darmstadtium, M * 8), // plate
                new MaterialStack(Materials.YttriumBariumCuprate, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[9].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Neutronium, M * 8), // plate
                new MaterialStack(Materials.Europium, M), // single cable
                new MaterialStack(Materials.Rubber, M * 2))); // plate


        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[3].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.StainlessSteel, M * 8), // plate
                new MaterialStack(Materials.Gold, M * 2), // single cable
                new MaterialStack(Materials.Rubber, M * 4), // plate
                new MaterialStack(Materials.BlackSteel, M * 2), // fine wire
                new MaterialStack(Materials.SteelMagnetic, M / 2) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[4].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Titanium, M * 8), // plate
                new MaterialStack(Materials.Aluminium, M * 2), // single cable
                new MaterialStack(Materials.Rubber, M * 4), // plate
                new MaterialStack(Materials.TungstenSteel, M * 2), // fine wire
                new MaterialStack(Materials.NeodymiumMagnetic, M / 2) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[5].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.TungstenSteel, M * 8), // plate
                new MaterialStack(Materials.Tungsten, M * 2), // single cable
                new MaterialStack(Materials.Rubber, M * 4), // plate
                new MaterialStack(Materials.Iridium, M * 2), // fine wire
                new MaterialStack(Materials.NeodymiumMagnetic, M / 2) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[3].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.StainlessSteel, M * 8), // plate
                new MaterialStack(Materials.Gold, 3 * M), // single cable + spring
                new MaterialStack(Materials.Rubber, M * 2), // plate
                new MaterialStack(Materials.BlackSteel, M * 2), // fine wire
                new MaterialStack(Materials.SteelMagnetic, M / 2) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[4].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.Titanium, M * 8), // plate
                new MaterialStack(Materials.Aluminium, 3 * M), // single cable + spring
                new MaterialStack(Materials.Rubber, M * 2), // plate
                new MaterialStack(Materials.TungstenSteel, M * 2), // fine wire
                new MaterialStack(Materials.NeodymiumMagnetic, M / 2) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[5].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Materials.TungstenSteel, M * 8), // plate
                new MaterialStack(Materials.Tungsten, 3 * M), // single cable + spring
                new MaterialStack(Materials.Rubber, M * 2), // plate
                new MaterialStack(Materials.Iridium, M * 2), // fine wire
                new MaterialStack(Materials.NeodymiumMagnetic, M / 2) // rod
        ));

        if (ConfigHolder.recipes.hardWoodRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Items.ACACIA_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2), new MaterialStack(Materials.Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.BIRCH_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2), new MaterialStack(Materials.Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.JUNGLE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2), new MaterialStack(Materials.Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2), new MaterialStack(Materials.Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.SPRUCE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2), new MaterialStack(Materials.Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.DARK_OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2), new MaterialStack(Materials.Iron, M / 9))); // screw
        } else {
            OreDictUnifier.registerOre(new ItemStack(Items.ACACIA_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Items.BIRCH_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Items.JUNGLE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Items.OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Items.SPRUCE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Items.DARK_OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        }

        if (ConfigHolder.recipes.hardIronRecipes)
            OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(
                    new MaterialStack(Materials.Iron, (37 * M) / 9), // dust tiny
                    new MaterialStack(Materials.Steel, M / 9))); // dust tiny
        else
            OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (15 * M) / 9))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (15 * M) / 9))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (15 * M) / 9))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (15 * M) / 9))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (15 * M) / 9))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (15 * M) / 9))); // dust tiny

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4))); // dust

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (3 * M) / 2))); // dust small

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.RED_SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.QUARTZ_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.NetherQuartz, M * 6))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Brick, M * 6))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.NETHER_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Netherrack, M * 6))); // dust

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 2), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 3), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 4), new ItemMaterialInfo(new MaterialStack(Materials.Brick, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 5), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 6), new ItemMaterialInfo(new MaterialStack(Materials.Netherrack, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 7), new ItemMaterialInfo(new MaterialStack(Materials.NetherQuartz, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_SLAB, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M / 2)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.LEVER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M), new MaterialStack(Materials.Wood, 1814400L)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_TORCH, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M / 2), new MaterialStack(Materials.Redstone, M)));

        if (ConfigHolder.recipes.hardRedstoneRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M), new MaterialStack(Materials.Iron, M / 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M), new MaterialStack(Materials.Iron, M * 6 / 8)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M), new MaterialStack(Materials.Steel, M)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M), new MaterialStack(Materials.Steel, M)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 2)));
        }

        OreDictUnifier.registerOre(new ItemStack(Items.WHEAT, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wheat, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HAY_BLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wheat, M * 9)));

        OreDictUnifier.registerOre(new ItemStack(Items.SNOWBALL, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Water, M / 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SNOW, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Water, M)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.PACKED_ICE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Ice, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.WRITABLE_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.ENCHANTED_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M * 72))); // block
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M * 8))); // ingot
        OreDictUnifier.registerOre(new ItemStack(Items.MINECART, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 4)));

        OreDictUnifier.registerOre(new ItemStack(Items.CAULDRON, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 7)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.IRON_BARS, 8, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 3 / 16)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 31)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 22)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 2), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 13)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HOPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 5), new MaterialStack(Materials.Wood, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Items.GLASS_BOTTLE), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M / 3))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M / 3))); // dust tiny

        if (ConfigHolder.recipes.hardToolArmorRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo
                    (new MaterialStack(Materials.Gold, (13 * M) / 8), // M + ring + 3 * bolt
                            new MaterialStack(Materials.Redstone, M)));

            OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Iron, (4 * M) / 3), // M + 3*screw
                    new MaterialStack(Materials.RedAlloy, M / 8), // bolt
                    new MaterialStack(Materials.Zinc, M / 4))); // ring
        } else {
            OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M * 4), new MaterialStack(Materials.Redstone, M)));
            OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 4), new MaterialStack(Materials.Redstone, M)));
        }

        if (ConfigHolder.recipes.hardMiscRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.NetherStar, (7 * M) / 4), // M + lens
                    new MaterialStack(Materials.Obsidian, M * 3),
                    new MaterialStack(Materials.Glass, M * 4)));

            OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, M * 4), new MaterialStack(Materials.Obsidian, M * 3), new MaterialStack(Materials.Paper, M * 9)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.NetherStar, M), new MaterialStack(Materials.Obsidian, M * 3), new MaterialStack(Materials.Glass, M * 5)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, M * 2), new MaterialStack(Materials.Obsidian, M * 4), new MaterialStack(Materials.Paper, M * 3)));
        }

        OreDictUnifier.registerOre(new ItemStack(Blocks.ENDER_CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.EnderEye, M), new MaterialStack(Materials.Obsidian, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.FURNACE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONEBRICK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.LADDER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOWL, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M)));
        OreDictUnifier.registerOre(new ItemStack(Items.SIGN, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.TRAPPED_CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.Iron, M / 2))); // ring

        if (ConfigHolder.recipes.hardMiscRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.RedAlloy, M / 2))); // rod
            OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Diamond, M / 8), // bolt
                    new MaterialStack(Materials.Iron, (17 * M) / 4), // gear + ring
                    new MaterialStack(Materials.RedAlloy, M)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.Redstone, M)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.Diamond, M)));
        }
        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_LAMP, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glowstone, M * 4), new MaterialStack(Materials.Redstone, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.CRAFTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PISTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 4), new MaterialStack(Materials.Wood, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STICKY_PISTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 4), new MaterialStack(Materials.Wood, M * 3)));
        if (ConfigHolder.recipes.hardRedstoneRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2), new MaterialStack(Materials.RedAlloy, M / 2), new MaterialStack(Materials.Iron, M * 4 + M / 4)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2), new MaterialStack(Materials.RedAlloy, M / 2), new MaterialStack(Materials.Iron, M * 2 + M * 3 / 4)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2), new MaterialStack(Materials.Redstone, M)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2), new MaterialStack(Materials.Redstone, M)));
        }
    }
}
