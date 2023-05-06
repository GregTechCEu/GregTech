package gregtech.worldgen;

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

    private final StoneTypeMapper mapper;

    public TerrainGenWorker(@Nonnull StoneTypeMapper mapper) {
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
            if (storage == null || storage.isEmpty()) return;

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
                        toSet = GTTerrainGenManager.getStateForPos(candidates, x, y, z, surfaceY);
                    }

                    if (toSet == state || toSet.getBlock().isAir(toSet, world, pos)) continue;

                    storage.set(xOffset, storageY, zOffset, toSet);
                }
            }
        }
    }
}
