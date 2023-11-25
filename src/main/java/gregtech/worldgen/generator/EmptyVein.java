package gregtech.worldgen.generator;

import gregtech.worldgen.OrePlacementResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class EmptyVein implements ChunkAlignedWorldGenerator {

    public static final EmptyVein INSTANCE = new EmptyVein();

    private final int[] dimensions = new int[0];

    private EmptyVein() {}

    @Override
    public @NotNull OrePlacementResult generate(@NotNull World world, @NotNull Random random,
                                                @NotNull String biome, int dimension, int originX, int originZ,
                                                int chunkX, int chunkZ) {
        return OrePlacementResult.ORE_PLACED;
    }

    @Override
    public @NotNull String getName() {
        return "EMPTY_VEIN";
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public int @NotNull [] getDimensions() {
        return dimensions;
    }
}
