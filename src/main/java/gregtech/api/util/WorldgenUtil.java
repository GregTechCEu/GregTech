package gregtech.api.util;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;

public final class WorldgenUtil {

    private WorldgenUtil() {}

    /**
     * Faster way to retrieve the surface level of a world than {@link World#getHeight(int, int)}.
     * Skips chunk loading checks.
     *
     * @param world the world containing the chunk
     * @param chunk the chunk containing the coordinates
     * @param x the x block coordinate within the chunk [0, 16)
     * @param z the z block coordinate within the chunk [0, 16)
     * @return the world surface height, guaranteed > 0
     */
    public static int getWorldSurfaceFast(@Nonnull World world, @Nonnull Chunk chunk, int x, int z) {
        // code simplified from World#getHeight(int, int)
        if (x >= -30_000_000 && z >= -30_000_000 && x < 30_000_000 && z < 30_000_000) {
            return Math.max(1, chunk.getHeightValue(x, z));
        }
        return Math.max(1, world.getSeaLevel() + 1);
    }
}
