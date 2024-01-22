package gregtech.api.pipenet;

import gregtech.api.GTValues;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.Set;

// Ok, I admit, truly tickless fluidpipes would mean sacrificing behavior. I'd have to make them act like itempipes.
// To get true 'flow' behavior, all sources and all destinations must be defined, then a single evaluation performed.
@Mod.EventBusSubscriber(modid = GTValues.MODID)
public final class FlowChannelTicker {

    private final static Set<WeakReference<FlowChannelManager<?, ?>>> MANAGERS = new ObjectOpenHashSet<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % 20 != 0) return;
        for (WeakReference<FlowChannelManager<?, ?>> ref : MANAGERS) {
            FlowChannelManager<?, ?> manager = ref.get();
            if (manager != null) {
                manager.tick();
            } else {
                MANAGERS.remove(ref);
            }
        }
    }

    public static void addManager(FlowChannelManager<?, ?> manager) {
        MANAGERS.add(new WeakReference<>(manager));
    }
}
