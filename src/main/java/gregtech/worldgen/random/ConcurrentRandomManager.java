package gregtech.worldgen.random;

import gregtech.api.util.XSTR;
import gregtech.worldgen.WorldgenUtil;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApiStatus.Experimental
public class ConcurrentRandomManager implements RandomManager {

    private final ConcurrentMap<Integer, Random> persistantRandoms = new ConcurrentHashMap<>();
    private final ThreadLocal<Random> seededRandom = ThreadLocal.withInitial(XSTR::new);

    @Override
    public @NotNull Random persistent(@NotNull World world) {
        return persistantRandoms.computeIfAbsent(world.provider.getDimension(),
                k -> new XSTR(WorldgenUtil.getRandomSeed(world)));
    }

    @Override
    public @NotNull Random seeded(long seed) {
        Random random = seededRandom.get();
        random.setSeed(seed);
        return random;
    }

    @Override
    public void onDimensionUnload(@NotNull World world) {
        persistantRandoms.remove(world.provider.getDimension());
        // call seededRandom#remove on the appropriate thread, retrieve with the concurrent mod's api
    }
}
