package gregtech.worldgen;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static gregtech.worldgen.WorldgenModule.DEBUG;

public final class GTWorldGenerator implements IWorldGenerator {

    public static final GTWorldGenerator INSTANCE = new GTWorldGenerator();

    private GTWorldGenerator() {}

    @Override
    public void generate(@NotNull Random random, int chunkX, int chunkZ, @NotNull World world,
                         @NotNull IChunkGenerator chunkGenerator, @NotNull IChunkProvider chunkProvider) {
        Runnable container = new WorldgenContainer(chunkX, chunkZ, world);
        if (DEBUG) {
            WorldgenModule.logger.info("Running {}", container);
        }
        container.run();
    }
}
