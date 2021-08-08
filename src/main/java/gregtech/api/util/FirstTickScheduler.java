package gregtech.api.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import gregtech.api.GTValues;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.Queue;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class FirstTickScheduler {

    private static final Map<World, Queue<IFirstTickTask>> tasksByWorld = Maps.newConcurrentMap();

    public static void addTask(final IFirstTickTask task) {
        final World world = task.getWorld();
        if (!world.isRemote) {
            final Queue<IFirstTickTask> tasks = tasksByWorld.computeIfAbsent(world,
                    k -> Queues.newConcurrentLinkedQueue());
            tasks.add(task);
        } else task.handleFirstTick();
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        tasksByWorld.remove(event.getWorld());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            final Queue<IFirstTickTask> tasks = tasksByWorld.get(event.world);
            if (tasks != null) {
                IFirstTickTask task = tasks.poll();
                while (task != null) {
                    task.handleFirstTick();
                    task = tasks.poll();
                }
            }
        }
    }
}
