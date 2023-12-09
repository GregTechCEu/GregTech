package gregtech.worldgen.random;

import gregtech.api.util.XSTR;
import gregtech.worldgen.WorldgenUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class SingleThreadedRandomManager implements RandomManager {

    private final Int2ObjectMap<Random> persistantRandoms = new Int2ObjectOpenHashMap<>();
    private final Random seededRandom = new XSTR();

    @Override
    public @NotNull Random persistent(@NotNull World world) {
        Random random = persistantRandoms.get(world.provider.getDimension());
        if (random == null) {
            random = new XSTR(WorldgenUtil.getRandomSeed(world));
            persistantRandoms.put(world.provider.getDimension(), random);
        }
        return random;
    }

    @Override
    public @NotNull Random seeded(long seed) {
        seededRandom.setSeed(seed);
        return seededRandom;
    }

    @Override
    public void onDimensionUnload(@NotNull World world) {
        persistantRandoms.remove(world.provider.getDimension());
    }
}
