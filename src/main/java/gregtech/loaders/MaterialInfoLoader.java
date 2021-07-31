package gregtech.loaders;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.M;
import static gregtech.api.GTValues.W;

public class MaterialInfoLoader {

    public static void init() {
        if (ConfigHolder.U.GT5u.harderHeatingCoils){
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.CUPRONICKEL),
                    new ItemMaterialInfo(new MaterialStack(Materials.Cupronickel, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.Bronze, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.TinAlloy, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.KANTHAL),
                    new ItemMaterialInfo(new MaterialStack(Materials.Kanthal, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.Aluminium, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.Copper, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NICHROME),
                    new ItemMaterialInfo(new MaterialStack(Materials.Nichrome, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.StainlessSteel, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.Aluminium, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TUNGSTENSTEEL),
                    new ItemMaterialInfo(new MaterialStack(Materials.TungstenSteel, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.VanadiumSteel, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.Nichrome, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.HSS_G),
                    new ItemMaterialInfo(new MaterialStack(Materials.HSSG, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.TungstenCarbide, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.Tungsten, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NAQUADAH),
                    new ItemMaterialInfo(new MaterialStack(Materials.Naquadah, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.Osmium, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.TungstenSteel, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NAQUADAH_ALLOY),
                    new ItemMaterialInfo(new MaterialStack(Materials.NaquadahAlloy, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.Osmiridium, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.Naquadah, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.FLUXED_ELECTRUM),
                    new ItemMaterialInfo(new MaterialStack(Materials.FluxedElectrum, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.Naquadria, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.NaquadahAlloy, OrePrefix.ingot.materialAmount))
            );
            OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.DIAMERICIUM_TITANIUM),
                    new ItemMaterialInfo(new MaterialStack(Materials.DiamericiumTitanium, OrePrefix.wireGtDouble.materialAmount * 8),
                            new MaterialStack(Materials.Trinium, OrePrefix.foil.materialAmount * 8),
                            new MaterialStack(Materials.Neutronium, OrePrefix.ingot.materialAmount))
            );

        } else {
            for (CoilType coilType : CoilType.values()) {
                if (coilType.getMaterial() instanceof DustMaterial) {
                    ItemStack outputStack = MetaBlocks.WIRE_COIL.getItemVariant(coilType);
                    OreDictUnifier.registerOre(outputStack, new ItemMaterialInfo(new MaterialStack(coilType.getMaterial(), OrePrefix.wireGtDouble.materialAmount * 8)));
                }
            }
        }
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.ACACIA_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.BIRCH_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.JUNGLE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.SPRUCE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.DARK_OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustTiny.materialAmount * 15)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustTiny.materialAmount * 15)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustTiny.materialAmount * 15)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustTiny.materialAmount * 15)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustTiny.materialAmount * 15)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustTiny.materialAmount * 15)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dust.materialAmount * 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dust.materialAmount * 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dust.materialAmount * 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dust.materialAmount * 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dust.materialAmount * 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dust.materialAmount * 4)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.dustSmall.materialAmount * 6)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.RED_SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.dustSmall.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.QUARTZ_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.NetherQuartz, OrePrefix.dust.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Brick, OrePrefix.dust.materialAmount * 6)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.NETHER_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Netherrack, OrePrefix.dust.materialAmount * 6)));

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

        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.WHEAT, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wheat, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HAY_BLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wheat, M * 9)));

        OreDictUnifier.registerOre(new ItemStack(Items.SNOWBALL, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Water, M / 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SNOW, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Water, M)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.ICE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Ice, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PACKED_ICE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Ice, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.WRITTEN_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.WRITABLE_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.ENCHANTED_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.block.materialAmount * 8L)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.ingot.materialAmount * 8L)));
        OreDictUnifier.registerOre(new ItemStack(Items.MINECART, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 5)));

        OreDictUnifier.registerOre(new ItemStack(Items.CAULDRON, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 7)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.IRON_BARS, 8, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 3 / 16)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 31)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 22)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 2), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 13)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HOPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 5), new MaterialStack(Materials.Wood, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Items.GLASS_BOTTLE), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, 1360800L)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, 1360800L)));

        OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, M * 4), new MaterialStack(Materials.Redstone, M)));
        OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M * 4), new MaterialStack(Materials.Redstone, M)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.NetherStar, M), new MaterialStack(Materials.Obsidian, M * 3), new MaterialStack(Materials.Glass, M * 5)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, M * 2), new MaterialStack(Materials.Obsidian, M * 4), new MaterialStack(Materials.Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ENDER_CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.EnderEye, M), new MaterialStack(Materials.Obsidian, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.FURNACE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONEBRICK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.LADDER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOWL, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M)));
        OreDictUnifier.registerOre(new ItemStack(Items.SIGN, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.TRAPPED_CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.Iron, OrePrefix.ring.materialAmount * 2L)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_LAMP, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glowstone, M * 4), new MaterialStack(Materials.Redstone, OrePrefix.dust.materialAmount * 4L)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.Redstone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 8), new MaterialStack(Materials.Diamond, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.CRAFTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, M * 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PISTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 4), new MaterialStack(Materials.Wood, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STICKY_PISTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 4), new MaterialStack(Materials.Wood, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 7), new MaterialStack(Materials.Redstone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M * 7), new MaterialStack(Materials.Redstone, M)));
    }
}
