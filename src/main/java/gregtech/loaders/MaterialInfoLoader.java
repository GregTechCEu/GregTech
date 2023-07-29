package gregtech.loaders;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.*;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.loaders.recipe.WoodRecipeLoader;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.common.metatileentities.MetaTileEntities.LONG_DIST_FLUID_ENDPOINT;
import static gregtech.common.metatileentities.MetaTileEntities.LONG_DIST_ITEM_ENDPOINT;

// TODO, material amount for planks ore prefix so we can use wood planks instead of using ingots?
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

        // Hull Composition is the Assembler recipe, minus the fluid input
        // Use Silicone Rubber for cable coverings when both Silicone Rubber and SBR can cover cables
        OreDictUnifier.registerOre(MetaTileEntities.HULL[ULV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(WroughtIron, OrePrefix.plate.getMaterialAmount(WroughtIron) * 8), // plate
                new MaterialStack(RedAlloy, OrePrefix.cableGtSingle.getMaterialAmount(RedAlloy) * 2), // cables
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2))); // plate (for the cable)

        OreDictUnifier.registerOre(MetaTileEntities.HULL[LV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Steel, OrePrefix.plate.getMaterialAmount(Steel) * 8), // plate
                new MaterialStack(Tin, OrePrefix.cableGtSingle.getMaterialAmount(Tin) * 2), // cables
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2))); // plate (for the cable)

        OreDictUnifier.registerOre(MetaTileEntities.HULL[MV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Aluminium, OrePrefix.plate.getMaterialAmount(Aluminium) * 8), // plate
                new MaterialStack(Copper, OrePrefix.cableGtSingle.getMaterialAmount(Copper) * 2), // cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2))); // plate (for the cable)

        OreDictUnifier.registerOre(MetaTileEntities.HULL[HV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(StainlessSteel, OrePrefix.plate.getMaterialAmount(StainlessSteel) * 8), // plate
                new MaterialStack(Gold, OrePrefix.cableGtSingle.getMaterialAmount(Gold) * 2), // cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2))); // plate (for the cable)

        OreDictUnifier.registerOre(MetaTileEntities.HULL[EV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Titanium, OrePrefix.plate.getMaterialAmount(Titanium) * 8), // plate
                new MaterialStack(Aluminium, OrePrefix.cableGtSingle.getMaterialAmount(Aluminium) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Aluminium coils require this foil for covering
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2))); // plate (for the cable)

        OreDictUnifier.registerOre(MetaTileEntities.HULL[IV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(TungstenSteel, OrePrefix.plate.getMaterialAmount(TungstenSteel) * 8), // plate
                new MaterialStack(Platinum, OrePrefix.cableGtSingle.getMaterialAmount(Platinum) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Required for cable covering
                new MaterialStack(SiliconeRubber, (OrePrefix.plate.getMaterialAmount(SiliconeRubber) / 2) * 2))); // Plate for covering the cable. Requires 72mb silicon rubber

        OreDictUnifier.registerOre(MetaTileEntities.HULL[LuV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(RhodiumPlatedPalladium, OrePrefix.plate.getMaterialAmount(RhodiumPlatedPalladium) * 8), // plate
                new MaterialStack(NiobiumTitanium, OrePrefix.cableGtSingle.getMaterialAmount(NiobiumTitanium) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Required for cable covering
                new MaterialStack(PolyphenyleneSulfide, OrePrefix.foil.getMaterialAmount(PolyphenyleneSulfide) * 2), // Required for cable covering
                new MaterialStack(SiliconeRubber, (OrePrefix.plate.getMaterialAmount(SiliconeRubber) / 2) * 2))); // Plate for covering the cable. Requires 72mb silicon rubber

        OreDictUnifier.registerOre(MetaTileEntities.HULL[ZPM].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(NaquadahAlloy, OrePrefix.plate.getMaterialAmount(NaquadahAlloy) * 8), // plate
                new MaterialStack(VanadiumGallium, OrePrefix.cableGtSingle.getMaterialAmount(VanadiumGallium) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Required for cable covering
                new MaterialStack(PolyphenyleneSulfide, OrePrefix.foil.getMaterialAmount(PolyphenyleneSulfide) * 2), // Required for cable covering
                new MaterialStack(SiliconeRubber, (OrePrefix.plate.getMaterialAmount(SiliconeRubber) / 2) * 2))); // Plate for covering the cable. Requires 72mb silicon rubber

        OreDictUnifier.registerOre(MetaTileEntities.HULL[UV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Darmstadtium, OrePrefix.plate.getMaterialAmount(Darmstadtium) * 8), // plate
                new MaterialStack(YttriumBariumCuprate, OrePrefix.cableGtSingle.getMaterialAmount(YttriumBariumCuprate) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Required for cable covering
                new MaterialStack(PolyphenyleneSulfide, OrePrefix.foil.getMaterialAmount(PolyphenyleneSulfide) * 2), // Required for cable covering
                new MaterialStack(SiliconeRubber, (OrePrefix.plate.getMaterialAmount(SiliconeRubber) / 2) * 2))); // Plate for covering the cable. Requires 72mb silicon rubber

        OreDictUnifier.registerOre(MetaTileEntities.HULL[UHV].getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Neutronium, OrePrefix.plate.getMaterialAmount(Neutronium) * 8), // plate
                new MaterialStack(Europium, OrePrefix.cableGtSingle.getMaterialAmount(Europium) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Required for cable covering
                new MaterialStack(PolyphenyleneSulfide, OrePrefix.foil.getMaterialAmount(PolyphenyleneSulfide) * 2), // Required for cable covering
                new MaterialStack(SiliconeRubber, (OrePrefix.plate.getMaterialAmount(SiliconeRubber) / 2) * 2))); // Plate for covering the cable. Requires 72mb silicon rubber


        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[HV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[HV].getStackForm())),
                new MaterialStack(Gold, OrePrefix.wireGtSingle.getMaterialAmount(Gold) * 2), // cable
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate for covering cable
                new MaterialStack(BlackSteel, OrePrefix.wireFine.getMaterialAmount(BlackSteel) * 16), // fine wire
                new MaterialStack(SteelMagnetic, OrePrefix.stick.getMaterialAmount(SteelMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[EV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[EV].getStackForm())),
                new MaterialStack(Aluminium, OrePrefix.wireGtSingle.getMaterialAmount(Aluminium) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Aluminium coils require this foil for covering
                new MaterialStack(Rubber, OrePrefix.plate.getMaterialAmount(Rubber) * 2), // plate
                new MaterialStack(TungstenSteel, OrePrefix.wireFine.getMaterialAmount(TungstenSteel) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_INPUT_HATCH[IV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[IV].getStackForm())),
                new MaterialStack(Tungsten, OrePrefix.wireGtSingle.getMaterialAmount(Tungsten) * 2), // cable
                new MaterialStack(PolyvinylChloride, OrePrefix.foil.getMaterialAmount(PolyvinylChloride) * 2), // Required for cable covering
                new MaterialStack(SiliconeRubber, (OrePrefix.plate.getMaterialAmount(SiliconeRubber) / 2) * 2), // Plate for covering the cable. Requires 72mb silicon rubber
                new MaterialStack(Iridium, OrePrefix.wireFine.getMaterialAmount(Iridium) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[HV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[HV].getStackForm())),
                new MaterialStack(Gold, OrePrefix.spring.getMaterialAmount(Gold) * 2), // spring
                new MaterialStack(BlackSteel, OrePrefix.wireFine.getMaterialAmount(BlackSteel) * 16), // fine wire
                new MaterialStack(SteelMagnetic, OrePrefix.stick.getMaterialAmount(SteelMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[EV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[EV].getStackForm())),
                new MaterialStack(Aluminium, OrePrefix.spring.getMaterialAmount(Aluminium) * 2), // spring
                new MaterialStack(TungstenSteel, OrePrefix.wireFine.getMaterialAmount(TungstenSteel) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        OreDictUnifier.registerOre(MetaTileEntities.ENERGY_OUTPUT_HATCH[IV].getStackForm(), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(MetaTileEntities.HULL[IV].getStackForm())),
                new MaterialStack(Tungsten, OrePrefix.spring.getMaterialAmount(Tungsten) * 2), // spring
                new MaterialStack(Iridium, OrePrefix.wireFine.getMaterialAmount(Iridium) * 16), // fine wire
                new MaterialStack(NeodymiumMagnetic, OrePrefix.stick.getMaterialAmount(NeodymiumMagnetic)) // rod
        ));

        // Divide by 2 since the recipe outputs 2 blocks
        OreDictUnifier.registerOre(MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.PLASCRETE), new ItemMaterialInfo(
                new MaterialStack(Steel, OrePrefix.frameGt.getMaterialAmount(Steel) / 2), // frame / 2
                new MaterialStack(Polyethylene, (OrePrefix.plate.getMaterialAmount(Polyethylene) * 6) / 2), // 6 sheets / 2
                new MaterialStack(Concrete, OrePrefix.block.getMaterialAmount(Concrete) / 2) // 1 block / 2
        ));

        // Divide by 2 since the recipe outputs 2 blocks
        OreDictUnifier.registerOre(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.CLEANROOM_GLASS), new ItemMaterialInfo(
                new MaterialStack(Steel, OrePrefix.frameGt.getMaterialAmount(Steel) / 2), // frame / 2
                new MaterialStack(Polyethylene, (OrePrefix.plate.getMaterialAmount(Polyethylene) * 6) / 2), // 6 sheets / 2
                new MaterialStack(Glass, OrePrefix.block.getMaterialAmount(Glass) / 2) // 1 block / 2
        ));

        OreDictUnifier.registerOre(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS), new ItemMaterialInfo(new MaterialStack(Materials.Fireclay, OrePrefix.ingot.getMaterialAmount(Fireclay) * 4)));

        // Empty PSS Capacitors
        OreDictUnifier.registerOre(MetaBlocks.BATTERY_BLOCK.getItemVariant(BlockBatteryPart.BatteryPartType.EMPTY_TIER_I), new ItemMaterialInfo(
                new MaterialStack(Materials.Ultimet, OrePrefix.frameGt.getMaterialAmount(Ultimet)),
                new MaterialStack(Materials.Ultimet, OrePrefix.plate.getMaterialAmount(Ultimet) * 6),
                new MaterialStack(Materials.Ultimet, OrePrefix.screw.getMaterialAmount(Ultimet) * 24)));

        OreDictUnifier.registerOre(MetaBlocks.BATTERY_BLOCK.getItemVariant(BlockBatteryPart.BatteryPartType.EMPTY_TIER_II), new ItemMaterialInfo(
                new MaterialStack(Materials.Ruridit, OrePrefix.frameGt.getMaterialAmount(Ruridit)),
                new MaterialStack(Materials.Ruridit, OrePrefix.plate.getMaterialAmount(Ruridit) * 6),
                new MaterialStack(Materials.Ruridit, OrePrefix.screw.getMaterialAmount(Ruridit) * 24)));

        OreDictUnifier.registerOre(MetaBlocks.BATTERY_BLOCK.getItemVariant(BlockBatteryPart.BatteryPartType.EMPTY_TIER_III), new ItemMaterialInfo(
                new MaterialStack(Materials.Neutronium, OrePrefix.frameGt.getMaterialAmount(Neutronium)),
                new MaterialStack(Materials.Neutronium, OrePrefix.plate.getMaterialAmount(Neutronium) * 6),
                new MaterialStack(Materials.Neutronium, OrePrefix.screw.getMaterialAmount(Neutronium) * 24)));


        // Long Distance Pipe Components

        // Divide by 2 for endpoints, since recipe outputs 2
        OreDictUnifier.registerOre(LONG_DIST_ITEM_ENDPOINT.getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Tin, (OrePrefix.pipeLargeItem.getMaterialAmount(Tin) * 2) / 2),
                new MaterialStack(Tin, (OrePrefix.ingot.getMaterialAmount(Tin)) / 2), // fluid input
                new MaterialStack(Steel, (OrePrefix.plate.getMaterialAmount(Steel) * 8) / 2),
                new MaterialStack(Steel, (OrePrefix.gear.getMaterialAmount(Steel) * 2) / 2)));

        OreDictUnifier.registerOre(LONG_DIST_FLUID_ENDPOINT.getStackForm(), new ItemMaterialInfo(
                new MaterialStack(Bronze, (OrePrefix.pipeLargeFluid.getMaterialAmount(Bronze) * 2) / 2),
                new MaterialStack(Tin, (OrePrefix.ingot.getMaterialAmount(Tin)) / 2), // fluid input
                new MaterialStack(Steel, (OrePrefix.plate.getMaterialAmount(Steel) * 8) / 2),
                new MaterialStack(Steel, (OrePrefix.gear.getMaterialAmount(Steel) * 2) / 2)));

        // Divide by 64 for pipes, since recipe outputs 64
        OreDictUnifier.registerOre(new ItemStack(MetaBlocks.LD_ITEM_PIPE), new ItemMaterialInfo(
                new MaterialStack(Tin, (OrePrefix.pipeLargeItem.getMaterialAmount(Tin) * 2) / 64),
                new MaterialStack(Tin, OrePrefix.ingot.getMaterialAmount(Tin) / 64), // fluid input
                new MaterialStack(Steel, (OrePrefix.plate.getMaterialAmount(Steel) * 8) / 64)));

        OreDictUnifier.registerOre(new ItemStack(MetaBlocks.LD_FLUID_PIPE), new ItemMaterialInfo(
                new MaterialStack(Bronze, (OrePrefix.pipeLargeFluid.getMaterialAmount(Bronze) * 2) / 64),
                new MaterialStack(Tin, OrePrefix.ingot.getMaterialAmount(Tin) / 64), // fluid input
                new MaterialStack(Steel, (OrePrefix.plate.getMaterialAmount(Steel) * 8) / 64)));


        // Vanilla Items


        if (ConfigHolder.recipes.hardAdvancedIronRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(
                    new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 4),
                    new MaterialStack(Materials.Iron, ((OrePrefix.stick.getMaterialAmount(Iron) * 3) / 4) / 4), // Iron Bars are 3 rods in, 4 bars out, and then we only use 1 bar
                    new MaterialStack(Materials.Steel, OrePrefix.screw.getMaterialAmount(Steel)),
                    new MaterialStack(Materials.Steel, OrePrefix.ring.getMaterialAmount(Steel))));
        } else {
            // Outputs 3 doors
            OreDictUnifier.registerOre(new ItemStack(Items.IRON_DOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, (OrePrefix.ingot.getMaterialAmount(Iron) * 6) / 3)));
        }

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (OrePrefix.block.getMaterialAmount(Stone) * 6) / 4)));

        // TODO, do sandstone recycling separately
        //OreDictUnifier.registerOre(new ItemStack(Blocks.SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (3 * M) / 2))); // dust small
        //OreDictUnifier.registerOre(new ItemStack(Blocks.RED_SANDSTONE_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (3 * M) / 2))); // dust small


        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, (OrePrefix.block.getMaterialAmount(Stone) * 6) / 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.QUARTZ_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.NetherQuartz, (OrePrefix.block.getMaterialAmount(NetherQuartz) * 6) / 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Brick, (OrePrefix.block.getMaterialAmount(Brick) * 6) / 4)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.NETHER_BRICK_STAIRS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Netherrack, (OrePrefix.block.getMaterialAmount(Netherrack) * 6) / 4)));

        // Meta 2 for stone slab just doesn't exist for some reason?
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) / 2)));
        // TODO, Sandstone recycling
        //OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 3), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 4), new ItemMaterialInfo(new MaterialStack(Materials.Brick, OrePrefix.block.getMaterialAmount(Brick) / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 5), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 6), new ItemMaterialInfo(new MaterialStack(Materials.Netherrack, OrePrefix.block.getMaterialAmount(Netherrack) / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_SLAB, 1, 7), new ItemMaterialInfo(new MaterialStack(Materials.NetherQuartz, OrePrefix.block.getMaterialAmount(NetherQuartz) / 2)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.LEVER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone)), new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_BUTTON, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_TORCH, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood)), new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));

        // TODO, rails are a bit weird. Assembler doubles the output and has some different inputs
        OreDictUnifier.registerOre(new ItemStack(Blocks.RAIL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, (OrePrefix.stick.getMaterialAmount(Iron) * 12) / 32),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) / 32)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.GOLDEN_RAIL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Gold, (OrePrefix.stick.getMaterialAmount(Gold) * 12) / 12),
                new MaterialStack(Redstone, OrePrefix.dust.getMaterialAmount(Redstone) / 12),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) / 12)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.DETECTOR_RAIL, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M / 2)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ACTIVATOR_RAIL, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, M / 2)));

        if (ConfigHolder.recipes.hardRedstoneRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 2),
                    new MaterialStack(Materials.Iron, OrePrefix.spring.getMaterialAmount(Iron))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone)),
                    new MaterialStack(Materials.Iron, OrePrefix.spring.getMaterialAmount(Iron))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Gold, OrePrefix.plate.getMaterialAmount(Gold)),
                    new MaterialStack(Materials.Steel, OrePrefix.spring.getMaterialAmount(Steel))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W),
                    new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron)),
                            new MaterialStack(Materials.Steel, OrePrefix.spring.getMaterialAmount(Steel))));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.plate.getMaterialAmount(Gold) * 2)));
            OreDictUnifier.registerOre(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 2)));
        }

        OreDictUnifier.registerOre(new ItemStack(Items.WHEAT, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wheat, OrePrefix.dust.getMaterialAmount(Wheat))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HAY_BLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wheat, OrePrefix.block.getMaterialAmount(Wheat))));

        OreDictUnifier.registerOre(new ItemStack(Items.SNOWBALL, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Water, OrePrefix.dustSmall.getMaterialAmount(Water))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.SNOW, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Water, OrePrefix.block.getMaterialAmount(Water))));

        OreDictUnifier.registerOre(new ItemStack(Blocks.PACKED_ICE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Ice, OrePrefix.dust.getMaterialAmount(Ice))));

        OreDictUnifier.registerOre(new ItemStack(Items.BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, OrePrefix.dust.getMaterialAmount(Paper) * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.WRITABLE_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, OrePrefix.dust.getMaterialAmount(Paper) * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.ENCHANTED_BOOK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Paper, OrePrefix.dust.getMaterialAmount(Paper) * 3)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.BOOKSHELF, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Paper, (OrePrefix.dust.getMaterialAmount(Paper) * 3) * 3), // Three Books
                new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 6)));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.block.getMaterialAmount(Gold) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_APPLE, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 8)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.CHEST, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.HOPPER, 1, W), new ItemMaterialInfo(
                Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.CHEST, 1))),
                new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 5)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.FURNACE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 8)));


        OreDictUnifier.registerOre(new ItemStack(Items.MINECART, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 3),
                new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron) * 4)));

        OreDictUnifier.registerOre(new ItemStack(Items.CHEST_MINECART, 1), new ItemMaterialInfo(
                Arrays.asList(OreDictUnifier.getMaterialInfo(new ItemStack(Items.MINECART)), OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.CHEST)))));

        OreDictUnifier.registerOre(new ItemStack(Items.FURNACE_MINECART, 1), new ItemMaterialInfo(
                Arrays.asList(OreDictUnifier.getMaterialInfo(new ItemStack(Items.MINECART)), OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.FURNACE)))));
        OreDictUnifier.registerOre(new ItemStack(Items.TNT_MINECART, 1), new ItemMaterialInfo(Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Items.MINECART)))));
        OreDictUnifier.registerOre(new ItemStack(Items.HOPPER_MINECART, 1), new ItemMaterialInfo(
                Arrays.asList(OreDictUnifier.getMaterialInfo(new ItemStack(Items.MINECART)), OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.HOPPER)))));

        OreDictUnifier.registerOre(new ItemStack(Items.CAULDRON, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.IRON_BARS, 8, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, (OrePrefix.stick.getMaterialAmount(Iron) * 3) / 16)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.IRON_TRAPDOOR, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 4)));
        OreDictUnifier.registerOre(new ItemStack(Items.BUCKET, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 3)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 0), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 31)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 22)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.ANVIL, 1, 2), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 13)));

        OreDictUnifier.registerOre(new ItemStack(Items.GLASS_BOTTLE), new ItemMaterialInfo(new MaterialStack(Materials.Glass, OrePrefix.dust.getMaterialAmount(Glass))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, OrePrefix.block.getMaterialAmount(Glass))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, OrePrefix.block.getMaterialAmount(Glass))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, (OrePrefix.block.getMaterialAmount(Glass) * 3) / 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.GLASS_PANE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Glass, (OrePrefix.block.getMaterialAmount(Glass) * 3) / 8))); // dust tiny

        OreDictUnifier.registerOre(new ItemStack(Items.FLOWER_POT, 1), new ItemMaterialInfo(new MaterialStack(Materials.Brick, OrePrefix.ingot.getMaterialAmount(Brick) * 3)));
        OreDictUnifier.registerOre(new ItemStack(Items.PAINTING, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.ITEM_FRAME, 1), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Blocks.COBBLESTONE_WALL, 1), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone))));
        OreDictUnifier.registerOre(new ItemStack(Items.END_CRYSTAL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Glass, OrePrefix.block.getMaterialAmount(Glass) * 7),
                new MaterialStack(Materials.EnderEye, OrePrefix.gem.getMaterialAmount(EnderEye))));

        if (ConfigHolder.recipes.hardToolArmorRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo
                    (new MaterialStack(Materials.Gold, (13 * M) / 8), // M + ring + 3 * bolt
                            new MaterialStack(Materials.Redstone, M)));

            OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Iron, (4 * M) / 3), // M + 3*screw
                    new MaterialStack(Materials.RedAlloy, M / 8), // bolt
                    new MaterialStack(Materials.Zinc, M / 4))); // ring
        } else {
            OreDictUnifier.registerOre(new ItemStack(Items.CLOCK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.plate.getMaterialAmount(Gold) * 4), new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));
            OreDictUnifier.registerOre(new ItemStack(Items.COMPASS, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.plate.getMaterialAmount(Iron) * 4), new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));
        }

        OreDictUnifier.registerOre(new ItemStack(Items.BOW, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 3)));

        if (ConfigHolder.recipes.hardMiscRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.NetherStar, OrePrefix.gem.getMaterialAmount(NetherStar)),
                    new MaterialStack(Materials.NetherStar, OrePrefix.lens.getMaterialAmount(NetherStar)),
                    new MaterialStack(Materials.Obsidian, OrePrefix.plate.getMaterialAmount(Obsidian) * 3),
                    new MaterialStack(Materials.Glass, OrePrefix.block.getMaterialAmount(Glass) * 4)));

            OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(
                    Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.BOOKSHELF))),
                    new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 4),
                    new MaterialStack(Materials.Obsidian, OrePrefix.plate.getMaterialAmount(Obsidian) * 3)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.BEACON, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.NetherStar, OrePrefix.gem.getMaterialAmount(NetherStar)),
                    new MaterialStack(Materials.Obsidian, OrePrefix.block.getMaterialAmount(Obsidian) * 3),
                    new MaterialStack(Materials.Glass, OrePrefix.block.getMaterialAmount(Glass) * 5)));

            OreDictUnifier.registerOre(new ItemStack(Blocks.ENCHANTING_TABLE, 1, W), new ItemMaterialInfo(
                    Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Items.BOOK))),
                    new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 2),
                    new MaterialStack(Materials.Obsidian, OrePrefix.block.getMaterialAmount(Obsidian) * 4)));
        }

        OreDictUnifier.registerOre(new ItemStack(Blocks.ENDER_CHEST, 1, W), new ItemMaterialInfo(
                new MaterialStack(Materials.EnderEye, OrePrefix.gem.getMaterialAmount(EnderEye)),
                new MaterialStack(Materials.Obsidian, OrePrefix.block.getMaterialAmount(Obsidian) * 8)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.STONEBRICK, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone))));
        OreDictUnifier.registerOre(new ItemStack(Blocks.LADDER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, (OrePrefix.ingot.getMaterialAmount(Wood) * 7) / 3)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.TRIPWIRE_HOOK), new ItemMaterialInfo(
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2),
                new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.BOWL, 1, W), new ItemMaterialInfo(new MaterialStack(Wood, OrePrefix.ingot.getMaterialAmount(Wood) / 2)));
        OreDictUnifier.registerOre(new ItemStack(Items.SIGN, 1, W), new ItemMaterialInfo(
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) / 3),
                new MaterialStack(Wood, (OrePrefix.plate.getMaterialAmount(Wood) * 6) / 3)));

        ItemMaterialInfo[] items = new ItemMaterialInfo[]{OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.CHEST, 1, W)), OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.TRIPWIRE_HOOK))};
        OreDictUnifier.registerOre(new ItemStack(Blocks.TRAPPED_CHEST, 1, W), new ItemMaterialInfo(Arrays.asList(items)));

        if (ConfigHolder.recipes.hardMiscRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Iron, ((OrePrefix.stick.getMaterialAmount(Iron) * 3) / 16) * 2), // 2 iron bars
                    new MaterialStack(Materials.Wood, OrePrefix.plate.getMaterialAmount(Wood) * 4),
                    new MaterialStack(Wood, OrePrefix.gear.getMaterialAmount(Wood)),
                    new MaterialStack(Materials.RedAlloy, OrePrefix.stick.getMaterialAmount(RedAlloy))));

            // TODO, 2 noteblocks
            OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Diamond, OrePrefix.bolt.getMaterialAmount(Diamond)),
                    new MaterialStack(Materials.Iron, OrePrefix.gear.getMaterialAmount(Iron)),
                    new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron)),
                    new MaterialStack(Wood, OrePrefix.plate.getMaterialAmount(Wood) * 4)));
        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.NOTEBLOCK, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 8),
                    new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.JUKEBOX, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 8),
                    new MaterialStack(Materials.Diamond, OrePrefix.dust.getMaterialAmount(Diamond))));
        }
        OreDictUnifier.registerOre(new ItemStack(Blocks.REDSTONE_LAMP, 1, W), new ItemMaterialInfo(
                new MaterialStack(Materials.Glowstone, OrePrefix.dust.getMaterialAmount(Glowstone) * 4),
                new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone) * 4)));

        OreDictUnifier.registerOre(new ItemStack(Blocks.CRAFTING_TABLE, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 4)));

        if (ConfigHolder.recipes.hardRedstoneRecipes) {
            OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 2),
                    new MaterialStack(Materials.RedAlloy, OrePrefix.stick.getMaterialAmount(RedAlloy)),
                    new MaterialStack(Materials.Iron, OrePrefix.spring.getMaterialAmount(Iron) * 2),
                    new MaterialStack(Materials.Iron, OrePrefix.gearSmall.getMaterialAmount(Iron) * 2),
                    new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 2),
                    new MaterialStack(Materials.RedAlloy, OrePrefix.stick.getMaterialAmount(RedAlloy)),
                    new MaterialStack(Materials.Iron, OrePrefix.springSmall.getMaterialAmount(Iron) * 2),
                    new MaterialStack(Materials.Iron, OrePrefix.gearSmall.getMaterialAmount(Iron) * 2),
                    new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.PISTON, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Wood, OrePrefix.ingot.getMaterialAmount(Wood) / 2),
                    new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone)),
                    new MaterialStack(Materials.Iron, OrePrefix.stick.getMaterialAmount(Iron)),
                    new MaterialStack(Materials.Iron, OrePrefix.gearSmall.getMaterialAmount(Iron))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.STICKY_PISTON, 1, W), new ItemMaterialInfo(
                    Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.PISTON)))));


        } else {
            OreDictUnifier.registerOre(new ItemStack(Blocks.DISPENSER, 1, W), new ItemMaterialInfo(
                    Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Items.BOW))),
                    new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 7),
                    new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));

            OreDictUnifier.registerOre(new ItemStack(Blocks.PISTON, 1, W), new ItemMaterialInfo(
                    new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 4),
                    new MaterialStack(Iron, OrePrefix.plate.getMaterialAmount(Iron)),
                    new MaterialStack(Redstone, OrePrefix.dust.getMaterialAmount(Redstone)),
                    new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 3)));

            OreDictUnifier.registerOre(new ItemStack(Blocks.STICKY_PISTON, 1, W), new ItemMaterialInfo(Collections.singletonList(OreDictUnifier.getMaterialInfo(new ItemStack(Blocks.PISTON)))));


            OreDictUnifier.registerOre(new ItemStack(Blocks.DROPPER, 1, W), new ItemMaterialInfo(new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 7), new MaterialStack(Materials.Redstone, OrePrefix.dust.getMaterialAmount(Redstone))));
        }

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.IRON_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 4)));

        // TODO, crashes because of the duplicate leggings.
        List<ItemMaterialInfo> ironArmorList = new ArrayList<>();
        //ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(new ItemStack(Items.IRON_HELMET));
        ironArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.IRON_HELMET)));
        ironArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.IRON_CHESTPLATE)));
        ironArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.IRON_LEGGINGS)));
        ironArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.IRON_LEGGINGS)));

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_HORSE_ARMOR, 1), new ItemMaterialInfo(
                //ironArmorList,
                new MaterialStack(Iron, OrePrefix.plate.getMaterialAmount(Iron) * 2),
                new MaterialStack(Iron, OrePrefix.screw.getMaterialAmount(Iron))));

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_SHOVEL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron)),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_PICKAXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_AXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_SWORD, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        OreDictUnifier.registerOre(new ItemStack(Items.IRON_HOE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Iron, OrePrefix.ingot.getMaterialAmount(Iron) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 4)));

        List<ItemMaterialInfo> goldArmorList = new ArrayList<>();
        goldArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.GOLDEN_HELMET)));
        goldArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.GOLDEN_CHESTPLATE)));
        goldArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.GOLDEN_LEGGINGS)));
        goldArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.GOLDEN_LEGGINGS)));


        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_HORSE_ARMOR, 1), new ItemMaterialInfo(
                //goldArmorList,
                new MaterialStack(Gold, OrePrefix.plate.getMaterialAmount(Gold) * 2),
                new MaterialStack(Gold, OrePrefix.screw.getMaterialAmount(Gold))));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_SHOVEL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold)),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_PICKAXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_AXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_SWORD, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        OreDictUnifier.registerOre(new ItemStack(Items.GOLDEN_HOE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Gold, OrePrefix.ingot.getMaterialAmount(Gold) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 5)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 8)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 7)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 4)));

        List<ItemMaterialInfo> diamondArmorList = new ArrayList<>();
        diamondArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.DIAMOND_HELMET)));
        diamondArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        diamondArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.DIAMOND_LEGGINGS)));
        diamondArmorList.add(OreDictUnifier.getMaterialInfo(new ItemStack(Items.DIAMOND_LEGGINGS)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_HORSE_ARMOR, 1), new ItemMaterialInfo(
                //diamondArmorList,
                new MaterialStack(Materials.Diamond, OrePrefix.plate.getMaterialAmount(Diamond) * 2),
                new MaterialStack(Diamond, OrePrefix.screw.getMaterialAmount(Diamond))));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_SHOVEL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond)),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_PICKAXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_AXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_SWORD, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        OreDictUnifier.registerOre(new ItemStack(Items.DIAMOND_HOE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Diamond, OrePrefix.gem.getMaterialAmount(Diamond) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_HELMET, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron) * 5)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_CHESTPLATE, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron) * 8)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_LEGGINGS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron) * 7)));
        OreDictUnifier.registerOre(new ItemStack(Items.CHAINMAIL_BOOTS, 1), new ItemMaterialInfo(new MaterialStack(Materials.Iron, OrePrefix.ring.getMaterialAmount(Iron) * 4)));

        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_SHOVEL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood)),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_PICKAXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 3),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_AXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 3),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_HOE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 2),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.WOODEN_SWORD, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Wood, OrePrefix.ingot.getMaterialAmount(Wood) * 2),
                new MaterialStack(Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        OreDictUnifier.registerOre(new ItemStack(Items.STONE_SHOVEL, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone)),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.STONE_PICKAXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.STONE_AXE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 3),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.STONE_HOE, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood) * 2)));

        OreDictUnifier.registerOre(new ItemStack(Items.STONE_SWORD, 1), new ItemMaterialInfo(
                new MaterialStack(Materials.Stone, OrePrefix.block.getMaterialAmount(Stone) * 2),
                new MaterialStack(Materials.Wood, OrePrefix.stick.getMaterialAmount(Wood))));

        // TODO, Wood
        WoodRecipeLoader.registerUnificationInfo();
    }
}
