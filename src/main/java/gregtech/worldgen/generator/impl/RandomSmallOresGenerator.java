package gregtech.worldgen.generator.impl;

import gregtech.worldgen.generator.SporadicWorldGenerator;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RandomSmallOresGenerator extends GeneratorBase<RandomSmallOresSettings> implements SporadicWorldGenerator {

    public RandomSmallOresGenerator(@NotNull RandomSmallOresSettings settings) {
        super(settings);
    }

    @Override
    public void generate(@NotNull World world, @NotNull Random random, @NotNull String biome, int dimension, int chunkX, int chunkZ) {
        if (!canGenerateInDimension(dimension)) return;
        if (!canGenerateInBiome(biome)) return;

        WorldgenPlaceable placeable = settings.placeable();
        int amount = (settings.size() / 2) + (random.nextInt(settings.size()) / 2);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < amount; i++) {
            int x = chunkX + 8 + random.nextInt(16);
            int y = settings.minY() + random.nextInt(settings.maxY() - settings.minY());
            int z = chunkZ + 8 + random.nextInt(16);
            pos.setPos(x, y, z);

            placeable.placeSmall(world, pos, world.getBlockState(pos));
        }
    }
}
