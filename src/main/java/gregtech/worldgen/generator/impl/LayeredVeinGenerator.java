package gregtech.worldgen.generator.impl;

import gregtech.worldgen.PlacementResult;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class LayeredVeinGenerator extends CuboidVeinGenerator<LayeredVeinSettings, LayeredVeinGenerator> {

    public LayeredVeinGenerator(@NotNull LayeredVeinSettings settings) {
        super(settings);
    }

    /**
     * Places the ores in the vein
     *
     * @return {@link PlacementResult#CANNOT_GEN_IN_BOTTOM} if nothing was placed in the bottom, otherwise {@code null}.
     */
    @Override
    protected @Nullable PlacementResult placeOres(@NotNull World world, @NotNull Random random,
                                                @NotNull BlockPos.MutableBlockPos pos, double volume,
                                                int startY, int xLeft, int xRight, int zLeft, int zRight,
                                                int westBound, int eastBound, int southBound, int northBound) {
        int localDensity = (int) Math.max(1, settings.density() / volume);

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
                    westBound, eastBound, northBound, southBound, localDensity, settings.density(),
                    topAmount, middleAmount, bottomAmount, spreadAmount);
        }

        return null;
    }

    protected void placeSmallOres(@NotNull World world, @NotNull Random random, @NotNull BlockPos.MutableBlockPos pos, int xLeft,
                                int xRight, int zLeft, int zRight, int chunkX, int chunkZ) {
        int amount = (xRight - xLeft) * (zRight - zLeft) * settings.density() / 10 * WorldgenModule.smallOresMultiplier();

        WorldgenPlaceable top = settings.top();
        WorldgenPlaceable middle = settings.middle();
        WorldgenPlaceable bottom = settings.bottom();
        WorldgenPlaceable spread = settings.spread();

        IBlockState state;

        for (int i = 0; i < amount; i++) {
            if (top.hasSmall()) {
                state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 160);
                placeSmallOre(world, pos, top, state);
            }

            if (middle.hasSmall()) {
                state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 160);
                placeSmallOre(world, pos, middle, state);
            }

            if (bottom.hasSmall()) {
                state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 160);
                placeSmallOre(world, pos, bottom, state);
            }

            if (spread.hasSmall()) {
                state = rollSmallOrePos(world, random, pos, chunkX, chunkZ, 190);
                placeSmallOre(world, pos, spread, state);
            }
        }
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
        WorldgenPlaceable top = settings.top();
        if (top.hasRegular() && shouldPlaceOre(random, weightX, weightZ)) {
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
        WorldgenPlaceable middle = settings.middle();
        if (middle.hasRegular() && random.nextInt(2) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
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
        WorldgenPlaceable bottom = settings.bottom();
        if (bottom.hasRegular() && shouldPlaceOre(random, weightX, weightZ)) {
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
        WorldgenPlaceable spread = settings.spread();
        if (spread.hasRegular() && random.nextInt(7) == 0 && shouldPlaceOre(random, weightX, weightZ)) {
            placeOre(world, pos, spread, existing);
            return true;
        }
        return false;
    }
}
