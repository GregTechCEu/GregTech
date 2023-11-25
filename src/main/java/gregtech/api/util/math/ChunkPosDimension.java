package gregtech.api.util.math;

import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

public final class ChunkPosDimension {

    private final int x;
    private final int z;
    private final int dimension;

    public ChunkPosDimension(int x, int z, int dimension) {
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    public static @NotNull ChunkPosDimension from(@NotNull ChunkPos chunkPos, int dimension) {
        return new ChunkPosDimension(chunkPos.x, chunkPos.z, dimension);
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    public int dimension() {
        return dimension;
    }

    public @NotNull ChunkPos toChunkPos() {
        return new ChunkPos(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChunkPosDimension that = (ChunkPosDimension) o;

        if (x != that.x) return false;
        if (z != that.z) return false;
        return dimension == that.dimension;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + z;
        result = 31 * result + dimension;
        return result;
    }

    @Override
    public String toString() {
        return "ChunkPosDimension{" +
                "x=" + x +
                ", z=" + z +
                ", dimension=" + dimension +
                '}';
    }
}
