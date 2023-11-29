package gregtech.worldgen.generator.impl;

import gregtech.worldgen.PlacementResult;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.WorldgenUtil;
import gregtech.worldgen.generator.ChunkAlignedWorldGenerator;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class CuboidVeinGenerator<T extends CuboidVeinSettings<S>, S extends ChunkAlignedWorldGenerator>
        extends GeneratorBase<T> implements ChunkAlignedWorldGenerator {

    protected CuboidVeinGenerator(@NotNull T settings) {
        super(settings);
    }

    /**
     * Check if an ore can be placed at the position
     *
     * @param state the blockstate at the position
     * @param world the world containing the position
     * @param pos   the position to check
     * @param left  the left placement bound
     * @param right the right placement bound
     * @return {@link PlacementResult#NON_OVERLAPPING} if the position cannot be reached but could be placed,
     * {@link PlacementResult#NON_OVERLAPPING_AIR_BLOCK} if the position cannot be reached, but cannot be placed,
     * {@code null} otherwise
     */
    private static @Nullable PlacementResult checkCanPlace(@NotNull IBlockState state, @NotNull World world,
                                                           @NotNull BlockPos pos, int left, int right) {
        if (left >= right) {
            if (state.getBlock().isReplaceableOreGen(state, world, pos, WorldgenModule::isOregenReplaceable)) {
                // Didn't reach, but could place, so save for future use
                return PlacementResult.NON_OVERLAPPING;
            } else {
                // Didn't reach, cannot place in the test spot, so try for another vein
                return PlacementResult.NON_OVERLAPPING_AIR_BLOCK;
            }
        }
        return null;
    }

    /**
     * Check if an ore should be placed
     *
     * @param random  the random to use
     * @param weightX the x weight
     * @param weightZ the z weight
     * @return if the ore should be placed
     */
    protected static boolean shouldPlaceOre(@NotNull Random random, int weightX, int weightZ) {
        return random.nextInt(weightX) == 0 || random.nextInt(weightZ) == 0;
    }

    /**
     * Rolls the position for small ores
     *
     * @param random the random to use
     * @param pos    the position to set
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @param yBound the upper bound for Y
     */
    protected static @NotNull IBlockState rollSmallOrePos(@NotNull World world, @NotNull Random random,
                                                        @NotNull BlockPos.MutableBlockPos pos,
                                                        int chunkX, int chunkZ, int yBound) {
        int x = random.nextInt(16) + chunkX + 2;
        int z = random.nextInt(16) + chunkZ + 2;
        int y = random.nextInt(yBound) + 10;
        pos.setPos(x, y, z);
        return world.getBlockState(pos);
    }

    /**
     * Place an ore block at the position
     *
     * @param world     the world containing the position
     * @param pos       the position to place at
     * @param placeable the placeable to place
     * @param existing  the block currently in the world at the position
     */
    protected static void placeOre(@NotNull World world, @NotNull BlockPos pos, @NotNull WorldgenPlaceable placeable, @NotNull IBlockState existing) {
        placeable.place(world, pos, existing);
    }

    /**
     * Place a small ore block at the position
     *
     * @param world     the world containing the position
     * @param pos       the position to place at
     * @param placeable the placeable to place
     * @param existing  the block currently in the world at the position
     */
    protected static void placeSmallOre(@NotNull World world, @NotNull BlockPos pos, @NotNull WorldgenPlaceable placeable, @NotNull IBlockState existing) {
        placeable.placeSmall(world, pos, existing);
    }

    @Override
    public @NotNull PlacementResult generate(@NotNull World world, @NotNull Random random,
                                             @NotNull String biome, int dimension, int originX, int originZ,
                                             int chunkX, int chunkZ) {
        if (!canGenerateInDimension(dimension)) return PlacementResult.INCOMPATIBLE_DIMENSION;
        if (!canGenerateInBiome(biome)) return PlacementResult.INCOMPATIBLE_BIOME;

        int minY = settings.minY();
        int startY = minY + random.nextInt(settings.maxY() - minY - 5);

        // check the X axis first

        int size = settings.size();
        int westBound = originX - random.nextInt(size);
        int eastBound = originX + 16 + random.nextInt(size);

        // limit to positions only in the cascade-adjusted chunk
        int xLeft = Math.max(westBound, chunkX + 8);
        int xRight = Math.min(eastBound, chunkX + 8 + 16);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        pos.setPos(chunkX + 7, startY, chunkZ + 9);
        IBlockState state = world.getBlockState(pos);

        PlacementResult result = checkCanPlace(state, world, pos, xLeft, xRight);
        if (result != null) {
            return result;
        }

        // if the X axis passes, check the Z axis

        int northBound = originZ - random.nextInt(size);
        int southBound = originZ + 16 + random.nextInt(size);

        // limit to positions only in the cascade-adjusted chunk
        int zLeft = Math.max(northBound, chunkZ + 8);
        int zRight = Math.min(southBound, chunkZ + 8 + 16);

        result = checkCanPlace(state, world, pos, zLeft, zRight);
        if (result != null) {
            return result;
        }

        if (WorldgenModule.DEBUG) {
            WorldgenModule.logger.info("Attempting vein \"{}\", chunkX={}, chunkZ={}, originX={}, originZ={}, minY={}",
                    settings.name(), chunkX / 16, chunkZ / 16, originX / 16, originZ / 16, minY);
        }

        // determine density based on distance from the origin chunk
        // this makes the vein more concentrated towards the center
        double xLength = chunkX / 16f - originX / 16f;
        double zLength = chunkZ / 16f - originZ / 16f;
        double volume = Math.sqrt(2 + (xLength * xLength) + (zLength * zLength));

        // place the regular ores in the vein
        result = placeOres(world, random, pos, volume, startY, xLeft, xRight, zLeft, zRight, westBound, eastBound, southBound, northBound);
        if (result != null) {
            return result;
        }

        // place the small ores
        if (WorldgenModule.isSmallOresEnabled()) {
            placeSmallOres(world, random, pos, xLeft, xRight, zLeft, zRight, chunkX, chunkZ);
        }

        placeIndicators(world, random, pos, xLeft, xRight, zLeft, zRight, chunkX, chunkZ);

        return PlacementResult.PLACED;
    }

    /**
     * Places the ores in the vein
     *
     * @return {@link PlacementResult#CANNOT_GEN_IN_BOTTOM} if nothing was placed in the bottom, otherwise {@code null}.
     */
    protected abstract @Nullable PlacementResult placeOres(@NotNull World world, @NotNull Random random,
                                                           @NotNull BlockPos.MutableBlockPos pos, double volume,
                                                           int startY, int xLeft, int xRight, int zLeft, int zRight,
                                                           int westBound, int eastBound, int southBound, int northBound);

    /**
     * Places the small ores around the vein
     *
     * @param world  the world containing the pos
     * @param random the random to use
     * @param pos    the pos to use
     * @param xLeft  the x left bound of the vein
     * @param xRight the x right bound of the vein
     * @param zLeft  the z left bound of the vein
     * @param zRight the z right bound of the vein
     * @param chunkX the chunk x coordinate
     * @param chunkZ the chunk z coordinate
     */
    protected abstract void placeSmallOres(@NotNull World world, @NotNull Random random,
                                           @NotNull BlockPos.MutableBlockPos pos, int xLeft, int xRight, int zLeft,
                                           int zRight, int chunkX, int chunkZ);
    /**
     * Places the indicators around the vein
     *
     * @param world  the world containing the pos
     * @param random the random to use
     * @param pos    the pos to use
     * @param xLeft  the x left bound of the vein
     * @param xRight the x right bound of the vein
     * @param zLeft  the z left bound of the vein
     * @param zRight the z right bound of the vein
     * @param chunkX the chunk x coordinate
     * @param chunkZ the chunk z coordinate
     */
    protected void placeIndicators(@NotNull World world, @NotNull Random random, @NotNull BlockPos.MutableBlockPos pos,
                                   int xLeft, int xRight, int zLeft, int zRight, int chunkX, int chunkZ) {
        WorldgenPlaceable indicator = settings.indicator();
        if (!indicator.hasIndicator()) return;

        int worldHeight = world.getHeight(chunkX, chunkZ);
        int minY = Math.min(worldHeight - 2, world.getSeaLevel());
        int maxY = Math.min(worldHeight - 1, world.provider.hasSkyLight() ? minY * 2 + 16 : 80);

        int xLim = xRight - xLeft;
        int zLim = zRight - zLeft;

        int amount = calculateIndicatorAmount(random);
        int attempts = 0;
        while (amount > 0 && attempts < 10) {
            int x = xLeft + random.nextInt(xLim);
            int z = zLeft + random.nextInt(zLim);
            for (int y = maxY; y >= minY; y--) {
                pos.setPos(x, y, z);

                // check the block to replace with the rock
                WorldgenUtil.RockPlacementResult result = WorldgenUtil.canSurfaceRockReplace(world, pos, world.getBlockState(pos));
                if (result == WorldgenUtil.RockPlacementResult.SKIP_COLUMN) {
                    break;
                } else if (result == WorldgenUtil.RockPlacementResult.SUCCESS) {
                    pos.move(EnumFacing.DOWN);
                    result = WorldgenUtil.canSurfaceRockStay(world, pos, world.getBlockState(pos));
                    if (result == WorldgenUtil.RockPlacementResult.SKIP_COLUMN) {
                        break;
                    } else if (result == WorldgenUtil.RockPlacementResult.SUCCESS) {
                        pos.move(EnumFacing.UP);
                        indicator.placeIndicator(world, pos);
                        amount--;
                        break;
                    }
                }
            }
            attempts++;
        }
    }

    /**
     * Calculate how many indicators to place
     *
     * @param random the random to use
     * @return the amount to place
     */
    protected abstract int calculateIndicatorAmount(@NotNull Random random);
}
