package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverseold.ITraverseData;
import gregtech.api.util.TaskScheduler;
import gregtech.api.util.function.Task;

import net.minecraft.world.World;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class NodeLossCache implements Task {

    private static final WeakHashMap<WorldPipeNet, NodeLossCache> CACHE = new WeakHashMap<>();

    public static void registerLossResult(Key key, NodeLossResult result, boolean simulating) {
        NodeLossCache existing = CACHE.get(key.node().getNet());
        if (existing == null) {
            existing = new NodeLossCache(key.node().getNet().getWorld());
            CACHE.put(key.node().getNet(), existing);
        }
        existing.registerResult(key, result, simulating);
    }

    public static @Nullable NodeLossResult getLossResult(Key key, boolean simulating) {
        NodeLossCache existing = CACHE.get(key.node().getNet());
        if (existing == null) {
            existing = new NodeLossCache(key.node().getNet().getWorld());
            CACHE.put(key.node().getNet(), existing);
        }
        return existing.getResult(key, simulating);
    }

    private final Map<Key, NodeLossResult> cache = new Object2ObjectOpenHashMap<>();

    private NodeLossCache(World world) {
        TaskScheduler.scheduleTask(world, TaskScheduler.weakTask(this));
    }

    public void registerResult(Key key, NodeLossResult result, boolean simulating) {
        result.simulated = simulating;
        cache.put(key, result);
    }

    public @Nullable NodeLossResult getResult(Key key, boolean simulating) {
        NodeLossResult result = cache.get(key);
        if (!simulating && result != null && result.simulated) result.simulated = false;
        return result;
    }

    @Override
    public boolean run() {
        if (cache.isEmpty()) return true;
        for (var result : cache.entrySet()) {
            if (result.getValue().simulated) continue;
            result.getValue().triggerPostAction(result.getKey().node());
        }
        cache.clear();
        return true;
    }

    public static Key key(WorldPipeNode node, IPredicateTestObject testObject) {
        return new Key(node, testObject);
    }

    public static Key key(WorldPipeNode node, ITraverseData<?, ?> data) {
        return new Key(node, data.getTestObject());
    }

    @Desugar
    public record Key(WorldPipeNode node, IPredicateTestObject testObject) {}
}
