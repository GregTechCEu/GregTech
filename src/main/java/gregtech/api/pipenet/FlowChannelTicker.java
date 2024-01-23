package gregtech.api.pipenet;

import gregtech.api.GTValues;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

// Ok, I admit, truly tickless fluidpipes would mean sacrificing behavior. I'd have to make them act like itempipes.
// To get true 'flow' behavior, all sources and all destinations must be defined, then a single evaluation performed.
@Mod.EventBusSubscriber(modid = GTValues.MODID)
public final class FlowChannelTicker {

    private final static Map<World, Set<WeakReference<FlowChannelManager<?, ?>>>> MANAGERS = new Object2ObjectOpenHashMap<>();
    private final static Map<World, Integer> TICK_COUNTS = new Object2ObjectOpenHashMap<>();

    private final static Set<WeakReference<FlowChannelManager<?, ?>>> EMPTY = new ObjectArraySet<>(0);

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote) return;
        TICK_COUNTS.compute(event.world, (k, v) -> {
            if (v == null) v = 0;
            return v % 10 + 1;
        });
        if (TICK_COUNTS.get(event.world) != 10) return;

        for (WeakReference<FlowChannelManager<?, ?>> ref : MANAGERS.getOrDefault(event.world, EMPTY)) {
            FlowChannelManager<?, ?> manager = ref.get();
            if (manager != null) {
                manager.tick();
            } else {
                MANAGERS.get(event.world).remove(ref);
            }
        }
    }

    public static void addManager(World world, FlowChannelManager<?, ?> manager) {
        if (!MANAGERS.containsKey(world)) {
            MANAGERS.put(world, new ObjectOpenHashSet<>());
        }
        MANAGERS.get(world).add(new WeakReference<>(manager));
    }
}
