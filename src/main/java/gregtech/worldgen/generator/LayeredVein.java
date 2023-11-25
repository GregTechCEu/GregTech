package gregtech.worldgen.generator;

import gregtech.worldgen.PlacementResult;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class LayeredVein extends WorldGeneratorBase implements ChunkAlignedWorldGenerator {

    private final int density;

    private final WorldgenPlaceable top;
    private final WorldgenPlaceable middle;
    private final WorldgenPlaceable bottom;
    private final WorldgenPlaceable spread;

    public LayeredVein(@NotNull String name, int minY, int maxY, int weight, int density, int size,
                       int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes, @NotNull WorldgenPlaceable top, @NotNull WorldgenPlaceable middle,
                       @NotNull WorldgenPlaceable bottom, @NotNull WorldgenPlaceable spread) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
        this.density = density;
        this.top = top;
        this.middle = middle;
        this.bottom = bottom;
        this.spread = spread;
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
    private static boolean shouldPlaceOre(@NotNull Random random, int weightX, int weightZ) {
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
    private static @NotNull IBlockState rollSmallOrePos(@NotNull World world, @NotNull Random random,
                                                        @NotNull BlockPos.MutableBlockPos pos,
                                                        int chunkX, int chunkZ, int yBound) {
        int x = random.nextInt(16) + chunkX + 2;
        int z = random.nextInt(16) + chunkZ + 2;
        int y = random.nextInt(yBound) + 10;
        pos.setPos(x, y, z);
        return world.getBlockState(pos);
    }

    @Override
    public @NotNull PlacementResult generate(@NotNull World world, @NotNull Random random,
                                             @NotNull String biome, int dimension, int originX, int originZ,
                                             int chunkX, int chunkZ) {
        if (!canGenerateInDimension(dimension)) return PlacementResult.INCOMPATIBLE_DIMENSION;
        if (!canGenerateInBiome(biome)) return PlacementResult.INCOMPATIBLE_BIOME;

        int startY = this.minY + random.nextInt(maxY - minY - 5);

        // check the X axis first

        int westBound = originX - random.nextInt(size);
        int eastBound = originX + 16 + random.nextInt(size);

        // limit to positions only in the cascade-adjusted chunk
        int xLeft = Math.max(westBound, chunkX + 8);
        int xRight = Math.min(eastBound, chunkX + 8 + 16);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        pos.setPos(chunkX + 7, startY, chunkZ + 9);
        IBlockState state = world.getBlockState(pos);

        PlacementResult result = checkCanPlace(state, world, pos, xLeft, xRight);
        if (result != null) return result;

        // if the X axis passes, check the Z axis

        int northBound = originZ - random.nextInt(size);
        int southBound = originZ + 16 + random.nextInt(size);

        // limit to positions only in the cascade-adjusted chunk
        int zLeft = Math.max(northBound, chunkZ + 8);
        int zRight = Math.min(southBound, chunkZ + 8 + 16);

        result = checkCanPlace(state, world, pos, zLeft, zRight);
        if (result != null) return result;

        if (WorldgenModule.DEBUG) {
            WorldgenModule.logger.info("Attempting vein \"{}\", chunkX={}, chunkZ={}, originX={}, originZ={}, minY={}",
                    this.name, chunkX / 16, chunkZ / 16, originX / 16, originZ / 16, minY);
        }

        // determine density based on distance from the origin chunk
        // this makes the vein more concentrated towards the center
        double xLength = chunkX / 16f - originX / 16f;
        double zLength = chunkZ / 16f - originZ / 16f;
        double volume = Math.sqrt(2 + (xLength * xLength) + (zLength * zLength));
        int localDensity = (int) Math.max(1, this.density / volume);

        // place the regular ores in the vein
        result = placeOres(world, random, pos, localDensity, startY, xLeft, xRight, zLeft, zRight, westBound, eastBound, southBound, northBound);
        if (result != null) return result;

        // place the small ores
        if (WorldgenModule.isSmallOresEnabled()) {
            placeSmallOres(world, random, pos, xLeft, xRight, zLeft, zRight, chunkX, chunkZ);
        }

        return PlacementResult.PLACED;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Places the ores in the vein
     *
     * @return {@link PlacementResult#CANNOT_GEN_IN_BOTTOM} if nothing was placed in the bottom, otherwise {@code null}.
     */
    private @Nullable PlacementResult placeOres(@NotNull World world, @NotNull Random random,
                                                @NotNull BlockPos.MutableBlockPos pos, int localDensity,
                                                int startY, int xLeft, int xRight, int zLeft, int zRight,
                                                int westBound, int eastBound, int southBound, int northBound) {
        int topAmount = 0;
        int middleAmount = 0;
        int bottomAmount = 0;
        int spreadAmount = 0;

        for (int layerOffset = -1; layerOffset <= 7; layerOffset++) {
            int layer = startY + layerOffset;
            for (int x = xLeft; x < xRight; x++) {
                int weightX = Math.max(1, Math.max(MathHelper.abs(westBound - x), MathHelper.abs(eastBound - x)) / localDensity);
                for (int z = zLeft; z < zRight; z++) {
                    int weightZ = Math.max(1, Math.max(MathHelper.abs(southBound - z), MathHelper.abs(northBound - z)) / localDensity);

                    pos.setPos(x, layer, z);
                    IBlockState existing = world.getBlockState(pos);

                    if (layerOffset <= 1) {
                        // layers -1, 0, and 1 are bottom and spread
                        if (placeBottom(world, random, pos, existing, weightX, weightZ)) {
                            bottomAmount++;
                        } else if (placeSpread(world, random, pos, existing, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else if (layerOffset == 2) {
                        // layer 2 is bottom, middle, and spread
                        if (placeMiddle(world, random, pos, existing, weightX, weightZ)) {
                            middleAmount++;
                        } else if (placeBottom(world, random, pos, existing, weightX, weightZ)) {
                            bottomAmount++;
                        } else if (placeSpread(world, random, pos, existing, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else if (layerOffset == 3) {
                        // layer 3 is middle, and spread
                        if (placeMiddle(world, random, pos, existing, weightX, weightZ)) {
                            middleAmount++;
                        } else if (placeSpread(world, random, pos, existing, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else if (layerOffset <= 5) {
                        // layers 4 and 5 is top, middle, and spread
                        if (placeMiddle(world, random, pos, existing, weightX, weightZ)) {
                            middleAmount++;
                        } else if (placeTop(world, random, pos, existing, weightX, weightZ)) {
                            topAmount++;
                        } else if (placeSpread(world, random, pos, existing, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    } else {
                        // layers 6 and 7 is top and spread
                        if (placeTop(world, random, pos, existing, weightX, weightZ)) {
                            topAmount++;
                        } else if (placeSpread(world, random, pos, existing, weightX, weightZ)) {
                            spreadAmount++;
                        }
                    }
                }
            }

            // ensure there's at least one ore in bottom layer before attempting to place the rest of the vein
            if (layerOffset == -1 && bottomAmount + spreadAmount == 0) {
                if (WorldgenModule.DEBUG) {
                    WorldgenModule.logger.info("No ore in bottom layer");
                }
                return PlacementResult.CANNOT_GEN_IN_BOTTOM;
            }
        }

        if (WorldgenModule.DEBUG) {
            WorldgenModule.logger.info("Placed Ore Vein at westBound={}, eastBound={}, northBound={}, southBound={}, localDensity={}, veinDensity={}, top={}, middle={}, bottom={}, spread={}",
                    westBound, eastBound, northBound, southBound, localDensity, density,
                    topAmount, middleAmount, bottomAmount, spreadAmount);
        }

        return null;
    }

    /**
     * Place the top ore
     *
     * @param world    the world containing the pos
     * @param random   the random to use
     * @param pos      the position to place at
     * @param existing the block currently at the pos
     * @param weightX  the x weight
     * @param weightZ  the z weight
     * @return if the ore was placed
     */
    private boolean placeTop(@NotNull World world, @NotNull Random random, @NotNull BlockPos pos,
                                @NotNull IBlockState existing, int weightX, int weightZ) {
        if (shouldPlaceOre(random, weightX, weightZ)) {
            placeOre(world, pos, top, existing);
            return true;
        }
        return false;
    }

    /**
     * Place the middle ore
     *
     * @param world    the world containing the pos
     * @param random   the random to use
     * @param pos      the position to place at
     * @param existing the block currently at the pos
     * @param weightX  the x weight
     * @param weightZ  the z weight
     * @return if the ore was placed
     */
    private boolean placeMiddle(@NotNull World world, @NotNull Random random, @NotNull BlockPos pos,
                                @NotNull IBlockState existing, int weightX, int weightZ) {
        if (random.nextInt(2) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
            placeOre(world, pos, middle, existing);
            return true;
        }
        return false;
    }

    /**
     * Place the bottom ore
     *
     * @param world    the world containing the pos
     * @param random   the random to use
     * @param pos      the position to place at
     * @param existing the block currently at the pos
     * @param weightX  the x weight
     * @param weightZ  the z weight
     * @return if the ore was placed
     */
    private boolean placeBottom(@NotNull World world, @NotNull Random random, @NotNull BlockPos pos,
                                @NotNull IBlockState existing, int weightX, int weightZ) {
        if (shouldPlaceOre(random, weightX, weightZ)) {
            placeOre(world, pos, bottom, existing);
            return true;
        }
        return false;
    }

    /**
     * Place the spread ore
     *
     * @param world    the world containing the pos
     * @param random   the random to use
     * @param pos      the position to place at
     * @param existing the block currently at the pos
     * @param weightX  the x weight
     * @param weightZ  the z weight
     * @return if the ore was placed
     */
    private boolean placeSpread(@NotNull World world, @NotNull Random random, @NotNull BlockPos pos, 
                                @NotNull IBlockState existing, int weightX, int weightZ) {
        if (random.nextInt(7) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
            placeOre(world, pos, spread, existing);
            return true;
        }
        return false;
    }

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
    private void placeSmallOres(@NotNull World world, @NotNull Random random, @NotNull BlockPos.MutableBlockPos pos, int xLeft,
                                int xRight, int zLeft, int zRight, int chunkX, int chunkZ) {
        int amount = (xRight - xLeft) * (zRight - zLeft) * this.density / 10 * WorldgenModule.smallOresMultiplier();
        IBlockState state;
        for (int i = 0; i < amount; i++) {
            state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 160);
            placeSmallOre(world, pos, top, state);

            state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 160);
            placeSmallOre(world, pos, middle, state);

            state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 160);
            placeSmallOre(world, pos, bottom, state);

            state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 190);
            placeSmallOre(world, pos, spread, state);
        }
    }

    /**
     * Place an ore block at the position
     *
     * @param world     the world containing the position
     * @param pos       the position to place at
     * @param placeable the placeable to place
     * @param existing  the block currently in the world at the position
     */
    private static void placeOre(@NotNull World world, @NotNull BlockPos pos, @NotNull WorldgenPlaceable placeable, @NotNull IBlockState existing) {
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
    private static void placeSmallOre(@NotNull World world, @NotNull BlockPos pos, @NotNull WorldgenPlaceable placeable, @NotNull IBlockState existing) {
        placeable.placeSmall(world, pos, existing);
    }
}
