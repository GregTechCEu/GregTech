package gregtech.worldgen;

import gregtech.api.util.GTLog;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import static gregtech.worldgen.WorldgenModule.DEBUG;

public final class GTWorldGenerator implements IWorldGenerator {

    public static final GTWorldGenerator INSTANCE = new GTWorldGenerator();

    private static final int MAX_CONTAINERS = 5;

    private final Queue<WorldgenContainer> containers = new ArrayDeque<>();

    private GTWorldGenerator() {}

    @Override
    public void generate(@NotNull Random random, int chunkX, int chunkZ, @NotNull World world,
                         @NotNull IChunkGenerator chunkGenerator, @NotNull IChunkProvider chunkProvider) {
        WorldgenContainer container = new WorldgenContainer(chunkX, chunkZ, world);
        containers.add(container);
        if (DEBUG) {
            WorldgenModule.logger.info("Added {}", container);
        }
        runContainers();
    }

    private void runContainers() {
        GTLog.logger.fatal("Container size {}", containers.size());
        int amount = Math.min(containers.size(), MAX_CONTAINERS);
        for (int i = 0; i < amount; i++) {
            WorldgenContainer container = containers.remove();
            if (DEBUG) {
                WorldgenModule.logger.info("Running {}", container);
            }
            container.run();
        }
    }
}
