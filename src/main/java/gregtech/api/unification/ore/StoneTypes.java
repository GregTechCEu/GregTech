package gregtech.api.unification.ore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockStoneSmooth;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.*;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class StoneTypes {

    // Real Types that drop custom Ores

    public static final StoneType STONE = new StoneType(0, "stone", new ResourceLocation("blocks/stone"), SoundType.STONE, OrePrefix.ore, Materials.Stone, "pickaxe",
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.STONE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE, true);

    public static StoneType NETHERRACK = new StoneType(1, "netherrack", new ResourceLocation("blocks/netherrack"), SoundType.STONE, OrePrefix.oreNetherrack, Materials.Netherrack, "pickaxe",
            Blocks.NETHERRACK::getDefaultState,
            state -> state.getBlock() == Blocks.NETHERRACK, true);

    public static StoneType ENDSTONE = new StoneType(2, "endstone", new ResourceLocation("blocks/end_stone"), SoundType.STONE, OrePrefix.oreEndstone, Materials.Endstone, "pickaxe",
            Blocks.END_STONE::getDefaultState,
            state -> state.getBlock() == Blocks.END_STONE, true);


    // Dummy Types used for better world generation

    public static StoneType SANDSTONE = new StoneType(3, "sandstone", new ResourceLocation("blocks/sandstone_normal"), new ResourceLocation("blocks/sandstone_top"), SoundType.STONE, OrePrefix.oreSand, Materials.SiliconDioxide, "pickaxe",
            () -> Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT),
            state -> state.getBlock() instanceof BlockSandStone && state.getValue(BlockSandStone.TYPE) == BlockSandStone.EnumType.DEFAULT, false);

    public static StoneType RED_SANDSTONE = new StoneType(4, "red_sandstone", new ResourceLocation("blocks/red_sandstone_normal"), new ResourceLocation("blocks/red_sandstone_top"), SoundType.STONE, OrePrefix.oreRedSand, Materials.SiliconDioxide, "pickaxe",
            () -> Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.DEFAULT),
            state -> state.getBlock() instanceof BlockRedSandstone && state.getValue(BlockRedSandstone.TYPE) == BlockRedSandstone.EnumType.DEFAULT, false);

    public static StoneType GRANITE = new StoneType(5, "granite", new ResourceLocation("blocks/stone_granite"), SoundType.STONE, OrePrefix.oreGranite, Materials.Granite, "pickaxe",
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.GRANITE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.GRANITE, false);

    public static StoneType DIORITE = new StoneType(6, "diorite", new ResourceLocation("blocks/stone_diorite"), SoundType.STONE, OrePrefix.oreDiorite, Materials.Diorite, "pickaxe",
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.DIORITE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.DIORITE, false);

    public static StoneType ANDESITE = new StoneType(7, "andesite", new ResourceLocation("blocks/stone_andesite"), SoundType.STONE, OrePrefix.oreAndesite, Materials.Andesite, "pickaxe",
            () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
            state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == EnumType.ANDESITE, false);

    public static StoneType BLACK_GRANITE = new StoneType(8, "black_granite", new ResourceLocation(GTValues.MODID, "blocks/stones/granite/granite_black_smooth"), SoundType.STONE, OrePrefix.oreBlackgranite, Materials.GraniteBlack, "pickaxe",
            () -> MetaBlocks.STONE_SMOOTH.getState(BlockStoneSmooth.BlockType.BLACK_GRANITE),
            state -> state.getBlock() instanceof BlockStoneSmooth && ((BlockStoneSmooth) state.getBlock()).getVariant(state) == BlockStoneSmooth.BlockType.BLACK_GRANITE, false);

    public static StoneType RED_GRANITE = new StoneType(9, "red_granite", new ResourceLocation(GTValues.MODID, "blocks/stones/granite/granite_red_smooth"), SoundType.STONE, OrePrefix.oreRedgranite, Materials.GraniteRed, "pickaxe",
            () -> MetaBlocks.STONE_SMOOTH.getState(BlockStoneSmooth.BlockType.RED_GRANITE),
            state -> state.getBlock() instanceof BlockStoneSmooth && ((BlockStoneSmooth) state.getBlock()).getVariant(state) == BlockStoneSmooth.BlockType.RED_GRANITE, false);

    public static StoneType MARBLE = new StoneType(10, "marble", new ResourceLocation(GTValues.MODID, "blocks/stones/marble/marble_smooth"), SoundType.STONE, OrePrefix.oreMarble, Materials.Marble, "pickaxe",
            () -> MetaBlocks.STONE_SMOOTH.getState(BlockStoneSmooth.BlockType.MARBLE),
            state -> state.getBlock() instanceof BlockStoneSmooth && ((BlockStoneSmooth) state.getBlock()).getVariant(state) == BlockStoneSmooth.BlockType.MARBLE, false);

    public static StoneType BASALT = new StoneType(11, "basalt", new ResourceLocation(GTValues.MODID, "blocks/stones/basalt/basalt_smooth"), SoundType.STONE, OrePrefix.oreBasalt, Materials.Basalt, "pickaxe",
            () -> MetaBlocks.STONE_SMOOTH.getState(BlockStoneSmooth.BlockType.BASALT),
            state -> state.getBlock() instanceof BlockStoneSmooth && ((BlockStoneSmooth) state.getBlock()).getVariant(state) == BlockStoneSmooth.BlockType.BASALT, false);
}
