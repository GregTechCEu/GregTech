package gregtech.api.graphnet.gather;

import gregtech.api.graphnet.predicate.IEdgePredicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;
import java.util.function.Supplier;

public class GatherPredicatesEvent extends Event {

    final Map<String, Supplier<IEdgePredicate<?, ?>>> gathered = new Object2ObjectOpenHashMap<>();

    GatherPredicatesEvent() {}

    public void registerPredicate(IEdgePredicate<?, ?> predicate) {
        gathered.put(predicate.getName(), predicate::getNew);
    }
}
