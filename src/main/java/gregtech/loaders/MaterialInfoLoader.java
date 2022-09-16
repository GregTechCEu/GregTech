package gregtech.loaders;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static gregtech.api.GTValues.M;
import static gregtech.api.GTValues.W;
import static gregtech.api.unification.material.Materials.*;

public class MaterialInfoLoader {

    public static void init() {
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.CUPRONICKEL),
                new ItemMaterialInfo(new MaterialStack(Cupronickel, OrePrefix.wireGtDouble.getMaterialAmount(Cupronickel) * 8), // double wire
                        new MaterialStack(Bronze, OrePrefix.foil.getMaterialAmount(Bronze) * 8), // foil
                        new MaterialStack(TinAlloy, OrePrefix.ingot.getMaterialAmount(TinAlloy))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.KANTHAL),
                new ItemMaterialInfo(new MaterialStack(Kanthal, OrePrefix.wireGtDouble.getMaterialAmount(Kanthal) * 8), // double wire
                        new MaterialStack(Aluminium, OrePrefix.foil.getMaterialAmount(Aluminium) * 8), // foil
                        new MaterialStack(Copper, OrePrefix.ingot.getMaterialAmount(Copper))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NICHROME),
                new ItemMaterialInfo(new MaterialStack(Nichrome, OrePrefix.wireGtDouble.getMaterialAmount(Nichrome) * 8), // double wire
                        new MaterialStack(StainlessSteel, OrePrefix.foil.getMaterialAmount(StainlessSteel) * 8), // foil
                        new MaterialStack(Aluminium, OrePrefix.ingot.getMaterialAmount(Aluminium))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TUNGSTENSTEEL),
                new ItemMaterialInfo(new MaterialStack(TungstenSteel, OrePrefix.wireGtDouble.getMaterialAmount(TungstenSteel) * 8), // double wire
                        new MaterialStack(VanadiumSteel, OrePrefix.foil.getMaterialAmount(VanadiumSteel) * 8), // foil
                        new MaterialStack(Nichrome, OrePrefix.ingot.getMaterialAmount(Nichrome))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.HSS_G),
                new ItemMaterialInfo(new MaterialStack(HSSG, OrePrefix.wireGtDouble.getMaterialAmount(HSSG) * 8), // double wire
                        new MaterialStack(TungstenCarbide, OrePrefix.foil.getMaterialAmount(TungstenCarbide) * 8), // foil
                        new MaterialStack(Tungsten, OrePrefix.ingot.getMaterialAmount(Tungsten))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NAQUADAH),
                new ItemMaterialInfo(new MaterialStack(Naquadah, OrePrefix.wireGtDouble.getMaterialAmount(Naquadah) * 8), // double wire
                        new MaterialStack(Osmium, OrePrefix.foil.getMaterialAmount(Osmium) * 8), // foil
                        new MaterialStack(TungstenSteel, OrePrefix.ingot.getMaterialAmount(TungstenSteel))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRINIUM),
                new ItemMaterialInfo(new MaterialStack(Trinium, OrePrefix.wireGtDouble.getMaterialAmount(Trinium) * 8), // double wire
                        new MaterialStack(NaquadahEnriched, OrePrefix.foil.getMaterialAmount(NaquadahEnriched) * 8), // foil
                        new MaterialStack(Naquadah, OrePrefix.ingot.getMaterialAmount(Naquadah))) // ingot
        );
        OreDictUnifier.registerOre(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRITANIUM),
                new ItemMaterialInfo(new MaterialStack(Tritanium, OrePrefix.wireGtDouble.getMaterialAmount(Tritanium) * 8), // double wire
                        new MaterialStack(Naquadria, OrePrefix.foil.getMaterialAmount(Naquadria) * 8), // foil
                        new MaterialStack(Trinium, OrePrefix.ingot.getMaterialAmount(Trinium))) // ingot
        );

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.ULV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(WroughtIron, OrePrefix.plate.getMaterialAmount(WroughtIron) * 8), // plate
                new MaterialStack(RedAlloy, OrePrefix.cableGtSingle.getMaterialAmount(RedAlloy) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Wood, OrePrefix.plate.getMaterialAmount(Wood) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.LV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Steel, OrePrefix.plate.getMaterialAmount(Steel) * 8), // plate
                new MaterialStack(Tin, OrePrefix.cableGtSingle.getMaterialAmount(Tin) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(WroughtIron, OrePrefix.plate.getMaterialAmount(WroughtIron) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.MV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Aluminium, OrePrefix.plate.getMaterialAmount(Aluminium) * 8), // plate
                new MaterialStack(Copper, OrePrefix.cableGtSingle.getMaterialAmount(Copper) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(WroughtIron, OrePrefix.plate.getMaterialAmount(WroughtIron) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.HV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(StainlessSteel, OrePrefix.plate.getMaterialAmount(StainlessSteel) * 8), // plate
                new MaterialStack(Gold, OrePrefix.cableGtSingle.getMaterialAmount(Gold) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polyethylene, OrePrefix.plate.getMaterialAmount(Polyethylene) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.EV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Titanium, OrePrefix.plate.getMaterialAmount(Titanium) * 8), // plate
                new MaterialStack(Aluminium, OrePrefix.cableGtSingle.getMaterialAmount(Aluminium) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polyethylene, OrePrefix.plate.getMaterialAmount(Polyethylene) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.IV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(TungstenSteel, OrePrefix.plate.getMaterialAmount(TungstenSteel) * 8), // plate
                new MaterialStack(Platinum, OrePrefix.cableGtSingle.getMaterialAmount(Platinum) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polytetrafluoroethylene, OrePrefix.plate.getMaterialAmount(Polytetrafluoroethylene) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.LuV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(RhodiumPlatedPalladium, OrePrefix.plate.getMaterialAmount(RhodiumPlatedPalladium) * 8), // plate
                new MaterialStack(NiobiumTitanium, OrePrefix.cableGtSingle.getMaterialAmount(NiobiumTitanium) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polytetrafluoroethylene, OrePrefix.plate.getMaterialAmount(Polytetrafluoroethylene) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.ZPM].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(NaquadahAlloy, OrePrefix.plate.getMaterialAmount(NaquadahAlloy) * 8), // plate
                new MaterialStack(VanadiumGallium, OrePrefix.cableGtSingle.getMaterialAmount(VanadiumGallium) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polybenzimidazole, OrePrefix.plate.getMaterialAmount(Polybenzimidazole) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.UV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Darmstadtium, OrePrefix.plate.getMaterialAmount(Darmstadtium) * 8), // plate
                new MaterialStack(YttriumBariumCuprate, OrePrefix.cableGtSingle.getMaterialAmount(YttriumBariumCuprate) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polybenzimidazole, OrePrefix.plate.getMaterialAmount(Polybenzimidazole) * 2))); // plate

        OreDictUnifier.registerOre(MetaTileEntities.HULL[GTValues.UHV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Neutronium, OrePrefix.plate.getMaterialAmount(Neutronium) * 8), // plate
                new MaterialStack(Europium, OrePrefix.cableGtSingle.getMaterialAmount(Europium) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate (for the cable)
                new MaterialStack(Polybenzimidazole, OrePrefix.plate.getMaterialAmount(Polybenzimidazole) * 2))); // plate


        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[GTValues.HV].getStackForm())),
                new MaterialStack(Gold, OrePrefix.wireGtSingle.getMaterialAmount(Gold) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate
                new MaterialStack(BlackSteel, OrePrefix.wireFine.getMaterialAmount(BlackSteel) * 16), // fine wire
                new MaterialStack(SteelMagnetic, OrePrefix.stick.getMaterialAmount(SteelMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[GTValues.EV].getStackForm())),
                new MaterialStack(Aluminium, OrePrefix.wireGtSingle.getMaterialAmount(Aluminium) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate
                new MaterialStack(TungstenSteel, OrePrefix.wireFine.getMaterialAmount(TungstenSteel) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.IV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[GTValues.IV].getStackForm())),
                new MaterialStack(Tungsten, OrePrefix.wireGtSingle.getMaterialAmount(Tungsten) * 2), // single cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate
                new MaterialStack(Iridium, OrePrefix.wireFine.getMaterialAmount(Iridium) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.HV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[GTValues.HV].getStackForm())),
                new MaterialStack(Gold, OrePrefix.spring.getMaterialAmount(Gold) * 2), // spring
                new MaterialStack(BlackSteel, OrePrefix.wireFine.getMaterialAmount(BlackSteel) * 16), // fine wire
                new MaterialStack(SteelMagnetic, OrePrefix.stick.getMaterialAmount(SteelMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.EV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[GTValues.EV].getStackForm())),
                new MaterialStack(Aluminium, OrePrefix.spring.getMaterialAmount(Aluminium) * 2), // spring
                new MaterialStack(TungstenSteel, OrePrefix.wireFine.getMaterialAmount(TungstenSteel) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.IV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[GTValues.IV].getStackForm())),
                new MaterialStack(Tungsten, OrePrefix.spring.getMaterialAmount(Tungsten) * 2), // spring
                new MaterialStack(Iridium, OrePrefix.wireFine.getMaterialAmount(Iridium) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        // Divide by 2 as the recipe outputs two blocks
        OreDictUnifier.registerOre(MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.PLASCRETE), new ItemMaterialInfo(
                new MaterialStack(Steel, OrePrefix.frameGt.getMaterialAmount(Steel) / 2), // frame / 2
                new MaterialStack(Polyethylene, (OrePrefix.plate.getMaterialAmount(Polyethylene) * 3) / 2), // 6 sheets / 2
                new MaterialStack(Concrete, OrePrefix.block.getMaterialAmount(Concrete) / 2) // 1 block / 2
        ));

        // Divide by 2 as the recipe outputs two blocks
        OreDictUnifier.registerOre(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.CLEANROOM_GLASS), new ItemMaterialInfo(
                new MaterialStack(Steel, OrePrefix.frameGt.getMaterialAmount(Steel) / 2), // frame / 2
                new MaterialStack(Polyethylene, (OrePrefix.plate.getMaterialAmount(Polyethylene) * 3) / 2), // 6 sheets / 2
                new MaterialStack(Glass, OrePrefix.block.getMaterialAmount(Glass) / 2) // 1 block / 2
        ));

        if (ConfigHolder.recipes.hardWoodRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Items.ACACIA_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3), new MaterialStack(Iron, OrePrefix.screw.getMaterialAmount(Iron)))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.BIRCH_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2), new MaterialStack(Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.JUNGLE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2), new MaterialStack(Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2), new MaterialStack(Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.SPRUCE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2), new MaterialStack(Iron, M / 9))); // screw
            OreDictUnifier.registerOre(new ItemStack(Items.DARK_OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2), new MaterialStack(Iron, M / 9))); // screw
        } else {
            OreDictUnifier.registerOre(new ItemStack(Items.ACACIA_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3)));
            OreDictUnifier.registerOre(new ItemStack(Items.BIRCH_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3)));
            OreDictUnifier.registerOre(new ItemStack(Items.JUNGLE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3)));
            OreDictUnifier.registerOre(new ItemStack(Items.OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3)));
            OreDictUnifier.registerOre(new ItemStack(Items.SPRUCE_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3)));
            OreDictUnifier.registerOre(new ItemStack(Items.DARK_OAK_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Wood, (OrePrefix.plank.getMaterialAmount(Wood) * 6) / 3)));
        }

        OreDictUnifier.registerOre(new ItemStack(Blocks.PLANKS, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PLANKS, 1, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PLANKS, 1, 2), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PLANKS, 1, 3), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PLANKS, 1, 4), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PLANKS, 1, 5), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood))));


        if (ConfigHolder.recipes.hardIronRecipes)
            OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(
                    new MaterialStack(Iron, (37 * M) / 9), // dust tiny
                    new MaterialStack(Steel, M / 9))); // dust tiny
        else
            OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_FENCE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M))); // dust

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 3))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 3))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 3))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 3))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 3))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_FENCE_GATE, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 3))); // dust

        OreDictUnifier.registerOre(new ItemStack(Blocks.OAK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.BIRCH_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.SPRUCE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.JUNGLE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.DARK_OAK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Wood, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACACIA_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Wood, (3 * M) / 2))); // dust small

        OreDictUnifier.registerOre(new ItemStack(Items.BOAT, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.BIRCH_BOAT, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.SPRUCE_BOAT, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.JUNGLE_BOAT, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.DARK_OAK_BOAT, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.ACACIA_BOAT, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 5)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.RED_SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Stone, (3 * M) / 2))); // dust small
        OreDictUnifier.registerOre(new ItemStack(Blocks.QUARTZ_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(NetherQuartz, M * 6))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Brick, M * 6))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.NETHER_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Netherrack, M * 6))); // dust

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 0), new ItemMaterialInfo(new MaterialStack(Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 1), new ItemMaterialInfo(new MaterialStack(Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 2), new ItemMaterialInfo(new MaterialStack(Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 3), new ItemMaterialInfo(new MaterialStack(Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 4), new ItemMaterialInfo(new MaterialStack(Brick, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 5), new ItemMaterialInfo(new MaterialStack(Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 6), new ItemMaterialInfo(new MaterialStack(Netherrack, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 7), new ItemMaterialInfo(new MaterialStack(NetherQuartz, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_SLAB, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M / 2)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.LEVER, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M / 9), new MaterialStack(Wood, 1814400L)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M / 9)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M / 9)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_TORCH, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M / 2), new MaterialStack(Redstone, M)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.RAIL, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 3 / 16)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GOLDEN_RAIL, 1), new ItemMaterialInfo(new MaterialStack(Gold, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DETECTOR_RAIL, 1), new ItemMaterialInfo(new MaterialStack(Iron, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACTIVATOR_RAIL, 1), new ItemMaterialInfo(new MaterialStack(Iron, M / 2)));

        if (ConfigHolder.recipes.hardRedstoneRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M), new MaterialStack(Iron, M / 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M), new MaterialStack(Iron, M * 6 / 8)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Gold, M), new MaterialStack(Steel, M)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Iron, M), new MaterialStack(Steel, M)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Gold, M * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Iron, M * 2)));
        }

        OreDictUnifier.registerOre(new ItemStack(Items.WHEAT, 1, W), new ItemMaterialInfo(new MaterialStack(Wheat, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HAY_BLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Wheat, M * 9)));

        OreDictUnifier.registerOre(new ItemStack(Items.SNOWBALL, 1, W), new ItemMaterialInfo(new MaterialStack(Water, M / 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SNOW, 1, W), new ItemMaterialInfo(new MaterialStack(Water, M)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.PACKED_ICE, 1, W), new ItemMaterialInfo(new MaterialStack(Ice, M * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.WRITABLE_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.ENCHANTED_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Paper, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BOOKSHELF, 1), new ItemMaterialInfo(new MaterialStack(Paper, M * 9), new MaterialStack(Wood, M * 6)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemMaterialInfo(new MaterialStack(Gold, M * 72))); // block
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 0), new ItemMaterialInfo(new MaterialStack(Gold, M * 8))); // ingot

        OreDictUnifier.registerOre(new ItemStack(Items.MINECART, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHEST_MINECART, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 4), new MaterialStack(Wood, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.FURNACE_MINECART, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 4), new MaterialStack(Stone, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.TNT_MINECART, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.HOPPER_MINECART, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 9), new MaterialStack(Wood, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Items.CAULDRON, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 7)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.IRON_BARS, 8, W), new ItemMaterialInfo(new MaterialStack(Iron, M * 3 / 16)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.IRON_TRAPDOOR, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.BUCKET, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 3)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 0), new ItemMaterialInfo(new MaterialStack(Iron, M * 31)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 1), new ItemMaterialInfo(new MaterialStack(Iron, M * 22)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 2), new ItemMaterialInfo(new MaterialStack(Iron, M * 13)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HOPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Iron, M * 5), new MaterialStack(Wood, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Items.GLASS_BOTTLE), new ItemMaterialInfo(new MaterialStack(Glass, OrePrefix.block.getMaterialAmount(Glass))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Glass, OrePrefix.block.getMaterialAmount(Glass))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Glass, OrePrefix.block.getMaterialAmount(Glass))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Glass, OrePrefix.dustTiny.getMaterialAmount(Glass) * 3))); // dust tiny
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Glass, OrePrefix.dustTiny.getMaterialAmount(Glass) * 3))); // dust tiny

        OreDictUnifier.registerOre(new ItemStack(Items.FLOWER_POT, 1), new ItemMaterialInfo(new MaterialStack(Brick, OrePrefix.ingot.getMaterialAmount(Brick) * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.PAINTING, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.ITEM_FRAME, 1), new ItemMaterialInfo(new MaterialStack(Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.COBBLESTONE_WALL, 1), new ItemMaterialInfo(new MaterialStack(Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Items.END_CRYSTAL, 1), new ItemMaterialInfo(new MaterialStack(Glass, M * 7), new MaterialStack(EnderEye, M)));

        if (ConfigHolder.recipes.hardToolArmorRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo
                    (new MaterialStack(Gold, (13 * M) / 8), // M + ring + 3 * bolt
                            new MaterialStack(Redstone, M)));

            OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Iron, (4 * M) / 3), // M + 3*screw
                    new MaterialStack(RedAlloy, M / 8), // bolt
                    new MaterialStack(Zinc, M / 4))); // ring
        } else {
            OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Gold, M * 4), new MaterialStack(Redstone, M)));
            OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(new MaterialStack(Iron, M * 4), new MaterialStack(Redstone, M)));
        }

        if (ConfigHolder.recipes.hardMiscRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(
                    new MaterialStack(NetherStar, (7 * M) / 4), // M + lens
                    new MaterialStack(Obsidian, M * 3),
                    new MaterialStack(Glass, M * 4)));

            OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Diamond, M * 4), new MaterialStack(Obsidian, M * 3), new MaterialStack(Paper, M * 9)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(new MaterialStack(NetherStar, M), new MaterialStack(Obsidian, M * 3), new MaterialStack(Glass, M * 5)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Diamond, M * 2), new MaterialStack(Obsidian, M * 4), new MaterialStack(Paper, M * 3)));
        }

        OreDictUnifier.registerOre(new ItemStack(Blocks.ENDER_CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(EnderEye, M), new MaterialStack(Obsidian, M * 8)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.FURNACE, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONEBRICK, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.LADDER, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOWL, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M / 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.SIGN, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.TRAPPED_CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 8), new MaterialStack(Iron, M / 2))); // ring

        if (ConfigHolder.recipes.hardMiscRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 8), new MaterialStack(RedAlloy, M / 2))); // rod
            OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Diamond, M / 8), // bolt
                    new MaterialStack(Iron, (17 * M) / 4), // gear + ring
                    new MaterialStack(RedAlloy, M)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 8), new MaterialStack(Redstone, M)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 8), new MaterialStack(Diamond, M)));
        }
        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_LAMP, 1, W), new ItemMaterialInfo(new MaterialStack(Glowstone, M * 4), new MaterialStack(Redstone, M * 4))); // dust
        OreDictUnifier.registerOre(new ItemStack(Blocks.CRAFTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, M * 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.PISTON, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M * 4), new MaterialStack(Wood, M * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STICKY_PISTON, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M * 4), new MaterialStack(Wood, M * 3)));
        if (ConfigHolder.recipes.hardRedstoneRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M * 2), new MaterialStack(RedAlloy, M / 2), new MaterialStack(Iron, M * 4 + M / 4)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, M * 2), new MaterialStack(RedAlloy, M / 2), new MaterialStack(Iron, M * 2 + M * 3 / 4)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone) * 7), new MaterialStack(Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));
            OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone) * 7), new MaterialStack(Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));
        }

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_HORSE_ARMOR, 1), new ItemMaterialInfo(
                new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 5), // helmet
                new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 8), // armor
                new MaterialStack(Iron, (OrePrefix.ingot.getMaterialAmount(Iron) * 7) * 2), // leggings
                new MaterialStack(Iron, OrePrefix.plate.getMaterialAmount(Iron) * 2), // plate
                new MaterialStack(Iron, OrePrefix.screw.getMaterialAmount(Iron)))); // screw
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_SHOVEL, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron)), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_PICKAXE, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_AXE, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_SWORD, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_HOE, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_HORSE_ARMOR, 1), new ItemMaterialInfo(
                new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 5), // helmet
                new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 8), // armor
                new MaterialStack(Gold, (OrePrefix.ingot.getMaterialAmount(Gold) * 7) * 2), // leggings
                new MaterialStack(Gold, OrePrefix.plate.getMaterialAmount(Gold) * 2), // plate
                new MaterialStack(Gold, OrePrefix.screw.getMaterialAmount(Gold)))); // screw
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_SHOVEL, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold)), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_PICKAXE, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_AXE, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_SWORD, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_HOE, 1), new ItemMaterialInfo(new MaterialStack(Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_HORSE_ARMOR, 1), new ItemMaterialInfo(
                new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 5), // helmet
                new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 8), // armor
                new MaterialStack(Diamond, (OrePrefix.gem.getMaterialAmount(Diamond) * 7) * 2), // leggings
                new MaterialStack(Diamond, OrePrefix.plate.getMaterialAmount(Diamond) * 2), // plate
                new MaterialStack(Diamond, OrePrefix.screw.getMaterialAmount(Diamond)))); // screw
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_SHOVEL, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond)), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_PICKAXE, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_AXE, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_SWORD, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_HOE, 1), new ItemMaterialInfo(new MaterialStack(Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ring.getMaterialAmount(Iron) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ring.getMaterialAmount(Iron) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ring.getMaterialAmount(Iron) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Iron, OrePrefix.ring.getMaterialAmount(Iron) * 4)));

        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_SHOVEL, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood)), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_PICKAXE, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_AXE, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_HOE, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_SWORD, 1), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.plank.getMaterialAmount(Wood) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        OreDictUnifier.registerOre(new ItemStack(Items.STONE_SHOVEL, 1), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone)), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.STONE_PICKAXE, 1), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.STONE_AXE, 1), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone) * 3), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.STONE_HOE, 1), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.STONE_SWORD, 1), new ItemMaterialInfo(new MaterialStack(Stone, OrePrefix.ingot.getMaterialAmount(Stone) * 2), new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood))));
    }
}
