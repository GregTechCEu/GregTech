package gregtech.api.unification.ore;

import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;

import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class StoneTypes {

    // Real Types that drop custom Ores
    public static final StoneType STONE = StoneType.Builder.create(0, "stone")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.ore)
            .stoneMaterial(Materials.Stone)
            .stone(() -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.STONE))
            .predicate(state -> state.getBlock() instanceof BlockStone &&
                    state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType NETHERRACK = StoneType.Builder.create(1, "netherrack")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreNetherrack)
            .stoneMaterial(Materials.Netherrack)
            .stone(Blocks.NETHERRACK::getDefaultState)
            .predicate(state -> state.getBlock() == Blocks.NETHERRACK)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType ENDSTONE = StoneType.Builder.create(2, "endstone")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreEndstone)
            .stoneMaterial(Materials.Endstone)
            .stone(Blocks.END_STONE::getDefaultState)
            .predicate(state -> state.getBlock() == Blocks.END_STONE)
            .shouldBeDroppedAsItem(true)
            .build();

    // Dummy Types used for better world generation

    public static final StoneType SANDSTONE = StoneType.Builder.create(3, "sandstone")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreSand)
            .stoneMaterial(Materials.SiliconDioxide)
            .stone(() -> Blocks.SANDSTONE.getDefaultState()
                    .withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT))
            .predicate(state -> state.getBlock() instanceof BlockSandStone &&
                    state.getValue(BlockSandStone.TYPE) == BlockSandStone.EnumType.DEFAULT)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType RED_SANDSTONE = StoneType.Builder.create(4, "red_sandstone")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreRedSand)
            .stoneMaterial(Materials.SiliconDioxide)
            .stone(() -> Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE,
                            BlockRedSandstone.EnumType.DEFAULT))
            .predicate( state -> state.getBlock() instanceof BlockRedSandstone &&
                    state.getValue(BlockRedSandstone.TYPE) == BlockRedSandstone.EnumType.DEFAULT)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType GRANITE = StoneType.Builder.create(5, "granite")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreGranite)
            .stoneMaterial(Materials.Granite)
            .stone(() -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.GRANITE))
            .predicate(state -> state.getBlock() instanceof BlockStone &&
                    state.getValue(BlockStone.VARIANT) == EnumType.GRANITE)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType DIORITE = StoneType.Builder.create(6, "diorite")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreDiorite)
            .stoneMaterial(Materials.Diorite)
            .stone(() -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, EnumType.DIORITE))
            .predicate(state -> state.getBlock() instanceof BlockStone &&
                    state.getValue(BlockStone.VARIANT) == EnumType.DIORITE)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType ANDESITE = StoneType.Builder.create(7, "andesite")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreAndesite)
            .stoneMaterial(Materials.Andesite)
            .stone(() -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
            .predicate(state -> state.getBlock() instanceof BlockStone &&
                    state.getValue(BlockStone.VARIANT) == EnumType.ANDESITE)
            .shouldBeDroppedAsItem(true)
            .build();

    public static final StoneType BLACK_GRANITE = StoneType.Builder.create(8, "black_granite")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreBlackgranite)
            .stoneMaterial(Materials.GraniteBlack)
            .stone(() -> gtStoneState(StoneVariantBlock.StoneType.BLACK_GRANITE))
            .predicate(state -> gtStonePredicate(state, StoneVariantBlock.StoneType.BLACK_GRANITE))
            .shouldBeDroppedAsItem(false)
            .build();

    public static final StoneType RED_GRANITE = StoneType.Builder.create(9, "red_granite")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreRedgranite)
            .stoneMaterial(Materials.GraniteRed)
            .stone(() -> gtStoneState(StoneVariantBlock.StoneType.RED_GRANITE))
            .predicate(state -> gtStonePredicate(state, StoneVariantBlock.StoneType.RED_GRANITE))
            .shouldBeDroppedAsItem(false)
            .build();

    public static final StoneType MARBLE = StoneType.Builder.create(10, "marble")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreMarble)
            .stoneMaterial(Materials.Marble)
            .stone(() -> gtStoneState(StoneVariantBlock.StoneType.MARBLE))
            .predicate(state -> gtStonePredicate(state, StoneVariantBlock.StoneType.MARBLE))
            .shouldBeDroppedAsItem(false)
            .build();

    public static final StoneType BASALT = StoneType.Builder.create(11, "basalt")
            .soundType(SoundType.STONE)
            .processingPrefix(OrePrefix.oreBasalt)
            .stoneMaterial(Materials.Basalt)
            .stone(() -> gtStoneState(StoneVariantBlock.StoneType.BASALT))
            .predicate(state -> gtStonePredicate(state, StoneVariantBlock.StoneType.BASALT))
            .shouldBeDroppedAsItem(false)
            .build();

    private static IBlockState gtStoneState(StoneVariantBlock.StoneType stoneType) {
        return MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH).getState(stoneType);
    }

    private static boolean gtStonePredicate(IBlockState state, StoneVariantBlock.StoneType stoneType) {
        StoneVariantBlock block = MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH);
        return state.getBlock() == block && block.getState(state) == stoneType;
    }
}
