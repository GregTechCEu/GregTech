package gregtech.worldgen.generator;

import gregtech.api.util.GTLog;
import gregtech.worldgen.PlacementResult;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static gregtech.worldgen.PlacementResult.*;

public class StoneBlob extends WorldGeneratorBase {

    private static final double[] sizes = {1, 1, 1.333333, 1.333333, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};

    private final WorldgenPlaceable placeable;

    public StoneBlob(@NotNull String name, int minY, int maxY, int weight, int size,
                     int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                     @NotNull WorldgenPlaceable placeable) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
        this.placeable = placeable;
    }

    /**
     * Generate the stone blob.
     *
     * @param world     the world to generate in
     * @param random    the random to use
     * @param biome     the biome being generated in
     * @param dimension the dimension being generated in
     * @param chunkX    the current chunk X in block coordinates
     * @param chunkZ    the current chunk Z in block coordinates
     * @param originX   the origin chunk X in block coordinates
     * @param originZ   the origin chunk Z in block coordinates
     * @param realSize  the actual size of the blob
     */
    public @NotNull PlacementResult generate(@NotNull World world, @NotNull Random random, @NotNull String biome,
                                             int dimension, int chunkX, int chunkZ, int originX, int originZ,
                                             double realSize) {
        if (!canGenerateInDimension(dimension)) return INCOMPATIBLE_DIMENSION;
        if (!canGenerateInBiome(biome)) return INCOMPATIBLE_BIOME;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int x = originX + random.nextInt(16);
        int y = minY + random.nextInt(maxY - minY);
        int z = originZ + random.nextInt(16);

        double xSize = sizes[random.nextInt(sizes.length)];
        double ySize = sizes[random.nextInt(sizes.length) / 2]; // skew towards taller, but skinnier shapes
        double zSize = sizes[random.nextInt(sizes.length)];

        int minX = x - (int) (realSize / xSize - 1);
        int maxX = x + (int) (realSize / xSize + 2);
        int startY = y - (int) (realSize / ySize - 1);
        int endY = y + (int) (realSize / ySize + 2);
        int minZ = z - (int) (realSize / zSize - 1);
        int maxZ = z + (int) (realSize / zSize + 2);

        pos.setPos(chunkX + 8, startY, chunkZ + 8);
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isAir(state, world, pos)) {
            return NON_OVERLAPPING_AIR_BLOCK;
        }

        int westBound = Math.max(minX, chunkX + 8);
        int eastBound = Math.min(maxX, chunkX + 8 + 16);

        if (westBound >= eastBound) return NON_OVERLAPPING;

        int southBound = Math.max(minZ, chunkZ + 8);
        int northBound = Math.min(maxZ, chunkZ + 8 + 16);

        if (southBound >= northBound) return NON_OVERLAPPING;

        double rightHandSide = realSize * realSize + 1;
        for (int yCoord = startY; yCoord < endY; yCoord++) {
            double dyh = (yCoord - y) * ySize;
            dyh = dyh * dyh;
            double leftHandSize = dyh;
            if (leftHandSize > rightHandSide) continue;

            for (int xCoord = westBound; xCoord < eastBound; xCoord++) {
                double dxh = (xCoord - x) * xSize;
                dxh = dxh * dxh;
                leftHandSize = dyh + dxh;
                if (leftHandSize > rightHandSide) continue;

                for (int zCoord = southBound; zCoord < northBound; zCoord++) {
                    double dzh = (zCoord - z) * zSize;
                    dzh = dzh * dzh;
                    leftHandSize = dyh + dxh + dzh;
                    if (leftHandSize > rightHandSide) continue;

                    pos.setPos(xCoord, yCoord, zCoord);
                    state = world.getBlockState(pos);
                    if (state.getBlock().isReplaceableOreGen(state, world, pos, WorldgenModule::isOregenReplaceable)) {
                        placeable.place(world, pos, state);
                    }
                }
            }
        }
        GTLog.logger.fatal("Placed at {} {}", chunkX, chunkZ);
        return PLACED;
    }
}
