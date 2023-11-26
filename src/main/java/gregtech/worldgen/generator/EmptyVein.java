package gregtech.worldgen.generator;

import gregtech.worldgen.PlacementResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class EmptyVein implements ChunkAlignedWorldGenerator {

    public static final EmptyVein INSTANCE = new EmptyVein();

    private EmptyVein() {}

    @Override
    public @NotNull PlacementResult generate(@NotNull World world, @NotNull Random random,
                                             @NotNull String biome, int dimension, int originX, int originZ,
                                             int chunkX, int chunkZ) {
        return PlacementResult.PLACED;
    }

}
