package gregtech.api.unification.ore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockGranite;
import gregtech.common.blocks.BlockGranite.GraniteVariant;
import gregtech.common.blocks.BlockMineral;
import gregtech.common.blocks.BlockMineral.MineralVariant;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneBlock.ChiselingVariant;
import net.minecraft.block.*;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import static gregtech.api.unification.ore.StoneType.AFFECTED_BY_GRAVITY;
import static gregtech.api.unification.ore.StoneType.UNBREAKABLE;

public class StoneTypes {

    public static StoneType STONE = new StoneType(0, "stone", new ResourceLocation("blocks/stone"), SoundType.STONE, OrePrefix.ore, Materials.Stone, "pickaxe", 0,
        () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.STONE),
        state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE);

    public static StoneType GRANITE = new StoneType(1, "granite", new ResourceLocation("blocks/stone_granite"), SoundType.STONE, OrePrefix.oreGranite, Materials.Granite, "pickaxe", 0,
        () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.GRANITE),
        state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.GRANITE);

    public static StoneType DIORITE = new StoneType(2, "diorite", new ResourceLocation("blocks/stone_diorite"), SoundType.STONE, OrePrefix.oreDiorite, Materials.Diorite, "pickaxe", 0,
        () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.DIORITE),
        state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.DIORITE);

    public static StoneType ANDESITE = new StoneType(3, "andesite", new ResourceLocation("blocks/stone_andesite"), SoundType.STONE, OrePrefix.oreAndesite, Materials.Andesite, "pickaxe", 0,
        () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
        state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.ANDESITE);

    public static StoneType GRAVEL = new StoneType(4, "gravel", new ResourceLocation("blocks/gravel"), SoundType.SAND, OrePrefix.oreGravel, Materials.Flint, "shovel", AFFECTED_BY_GRAVITY,
        Blocks.GRAVEL::getDefaultState,
        state -> state.getBlock() instanceof BlockGravel);

    public static StoneType NETHERRACK = new StoneType(5, "netherrack", new ResourceLocation("blocks/netherrack"), SoundType.STONE, OrePrefix.oreNetherrack, Materials.Netherrack, "pickaxe", 0,
        Blocks.NETHERRACK::getDefaultState,
        state -> state.getBlock() == Blocks.NETHERRACK);

    public static StoneType ENDSTONE = new StoneType(6, "endstone", new ResourceLocation("blocks/end_stone"), SoundType.STONE, OrePrefix.oreEndstone, Materials.Endstone, "pickaxe", 0,
        Blocks.END_STONE::getDefaultState,
        state -> state.getBlock() == Blocks.END_STONE);

    public static StoneType SANDSTONE = new StoneType(7, "sandstone", new ResourceLocation("blocks/sandstone_normal"), new ResourceLocation("blocks/sandstone_top"), SoundType.STONE, OrePrefix.oreSand, Materials.SiliconDioxide, "pickaxe", 0,
        () -> Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT),
        state -> state.getBlock() instanceof BlockSandStone && state.getValue(BlockSandStone.TYPE) == BlockSandStone.EnumType.DEFAULT);

    public static StoneType RED_SANDSTONE = new StoneType(8, "red_sandstone", new ResourceLocation("blocks/red_sandstone_normal"), new ResourceLocation("blocks/red_sandstone_top"), SoundType.STONE, OrePrefix.oreRedSand, Materials.SiliconDioxide, "pickaxe", 0,
        () -> Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.DEFAULT),
        state -> state.getBlock() instanceof BlockRedSandstone && state.getValue(BlockRedSandstone.TYPE) == BlockRedSandstone.EnumType.DEFAULT);

    public static StoneType BLACK_GRANITE = new StoneType(9, "black_granite", new ResourceLocation(GTValues.MODID, "blocks/stones/granite/granite_black_stone"), SoundType.STONE, OrePrefix.oreBlackgranite, Materials.GraniteBlack, "pickaxe", 0,
        () -> MetaBlocks.GRANITE.withVariant(GraniteVariant.BLACK_GRANITE, ChiselingVariant.NORMAL),
        state -> state.getBlock() instanceof BlockGranite && ((BlockGranite) state.getBlock()).getVariant(state) == GraniteVariant.BLACK_GRANITE);

    public static StoneType RED_GRANITE = new StoneType(10, "red_granite", new ResourceLocation(GTValues.MODID, "blocks/stones/granite/granite_red_stone"), SoundType.STONE, OrePrefix.oreRedgranite, Materials.GraniteRed, "pickaxe", 0,
        () -> MetaBlocks.GRANITE.withVariant(GraniteVariant.RED_GRANITE, ChiselingVariant.NORMAL),
        state -> state.getBlock() instanceof BlockGranite && ((BlockGranite) state.getBlock()).getVariant(state) == GraniteVariant.RED_GRANITE);

    public static StoneType MARBLE = new StoneType(11, "marble", new ResourceLocation(GTValues.MODID, "blocks/stones/marble/marble_stone"), SoundType.STONE, OrePrefix.oreMarble, Materials.Marble, "pickaxe", 0,
        () -> MetaBlocks.MINERAL.withVariant(MineralVariant.MARBLE, ChiselingVariant.NORMAL),
        state -> state.getBlock() instanceof BlockMineral && ((BlockMineral) state.getBlock()).getVariant(state) == MineralVariant.MARBLE);

    public static StoneType BASALT = new StoneType(12, "basalt", new ResourceLocation(GTValues.MODID, "blocks/stones/basalt/basalt_stone"), SoundType.STONE, OrePrefix.oreBasalt, Materials.Basalt, "pickaxe", 0,
        () -> MetaBlocks.MINERAL.withVariant(MineralVariant.BASALT, ChiselingVariant.NORMAL),
        state -> state.getBlock() instanceof BlockMineral && ((BlockMineral) state.getBlock()).getVariant(state) == MineralVariant.BASALT);
}
