package gregtech.worldgen.terrain;

import gregtech.api.util.WorldgenUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A terrain generation worker that applies terrain gen using a given IStoneTypeMapper
 */
public final class TerrainGenWorker {

    private final IBlockMapper mapper;

    public TerrainGenWorker(@Nonnull IBlockMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Generate terrain for a chunk.
     *
     * @param world the world to generate in
     * @param chunk the chunk to populate
     * @param startX the starting x block coordinate of the world
     * @param startZ the starting z block coordinate of the world
     */
    public void generate(@Nonnull World world, @Nonnull Chunk chunk, int startX, int startZ) {
        final ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
        final int maxY = world.getActualHeight();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < maxY; y++) {
            final ExtendedBlockStorage storage = storages[y >> 4];
            // out of storages, so immediately exit
            if (storage == null || storage.isEmpty()) break;

            final int storageY = y & 0xF;
            for (int xOffset = 0; xOffset < 16; xOffset++) {
                final int x = startX + xOffset;
                for (int zOffset = 0; zOffset < 16; zOffset++) {
                    final int z = startZ + zOffset;
                    pos.setPos(x, y, z);

                    final IBlockState state = storage.get(xOffset, storageY, zOffset);
                    if (state.getBlock().isAir(state, world, pos)) continue;
                    final List<IBlockState> candidates = mapper.getCandidates(state);
                    if (candidates == null || candidates.isEmpty()) continue;

                    final IBlockState toSet;
                    if (candidates.size() == 1) {
                        toSet = candidates.get(0);
                    } else {
                        final int surfaceY = WorldgenUtil.getWorldSurfaceFast(world, chunk, xOffset, zOffset);
                        toSet = getStateForPos(candidates, x, y, z, surfaceY);
                    }

                    if (toSet == state || toSet.getBlock().isAir(toSet, world, pos)) continue;

                    storage.set(xOffset, storageY, zOffset, toSet);
                }
            }
        }
    }

    /**
     *
     * @param candidates the candidates to select from
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @param surfaceY the y value of the world surface
     * @return the selected BlockState to place
     */
    @Nonnull
    private static IBlockState getStateForPos(@Nonnull List<IBlockState> candidates, int x, int y, int z, int surfaceY) {
        // need abs() for x and y, when generating blobs between - and + coords
        float noiseValue = GTTerrainGenManager.noise.noise(Math.abs(x * 0.01F), y * 1.0F / surfaceY, Math.abs(z * 0.01F), 4, 0.1F);
        return candidates.get((int) (candidates.size() * noiseValue));
    }
}
