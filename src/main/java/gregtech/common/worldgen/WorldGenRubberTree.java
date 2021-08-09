package gregtech.common.worldgen;

import gregtech.api.GTValues;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregLog.LogVariant;
import gregtech.common.blocks.wood.BlockGregSapling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenRubberTree implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.getWorldType() == WorldType.FLAT ||
                !world.provider.isSurfaceWorld()) {
            return; //do not generate in flat worlds, or in non-surface worlds
        }
        BlockPos randomPos = new BlockPos(
                chunkX * 16 + GTValues.RNG.nextInt(16),
                0,
                chunkZ * 16 + GTValues.RNG.nextInt(16)
        );
        Biome biome = world.getBiome(randomPos);

        if (BiomeDictionary.hasType(biome, Type.COLD) ||
                BiomeDictionary.hasType(biome, Type.HOT) ||
                BiomeDictionary.hasType(biome, Type.DRY) ||
                BiomeDictionary.hasType(biome, Type.DEAD) ||
                BiomeDictionary.hasType(biome, Type.SPOOKY))
            return; //do not generate in inappropriate biomes

        int rubberTreeChance = 6;
        if (BiomeDictionary.hasType(biome, Type.SWAMP) ||
                BiomeDictionary.hasType(biome, Type.WET))
            rubberTreeChance /= 2; //double chance of spawning in swamp or wet biomes

        if (random.nextInt(rubberTreeChance) == 0) {
            randomPos = world.getTopSolidOrLiquidBlock(randomPos).down();
            IBlockState solidBlockState = world.getBlockState(randomPos);
            BlockGregSapling sapling = MetaBlocks.SAPLING;
            if (solidBlockState.getBlock().canSustainPlant(solidBlockState, world, randomPos, EnumFacing.UP, sapling)) {
                BlockPos abovePos = randomPos.up();
                IBlockState aboveState = world.getBlockState(abovePos);
                if (aboveState.getBlock() instanceof BlockLiquid) {
                    return;
                }

                IBlockState saplingState = sapling.getDefaultState()
                        .withProperty(BlockGregSapling.VARIANT, LogVariant.RUBBER_WOOD);
                world.setBlockState(abovePos, saplingState);
                sapling.generateTree(world, abovePos, saplingState, random);
            }
        }
    }

}
