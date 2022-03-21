package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.util.function.Task;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

@EventBusSubscriber(modid = GTValues.MODID)
public class TaskScheduler {

    private static final Map<World, List<Task>> tasksPerWorld = new HashMap<>();

    public static void scheduleTask(World world, Task task) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Attempt to schedule task on client world!");
        }
        tasksPerWorld.computeIfAbsent(world, k -> new ArrayList<>()).add(task);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            tasksPerWorld.remove(event.getWorld());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            List<Task> taskList = tasksPerWorld.getOrDefault(event.world, Collections.emptyList());
            taskList.removeIf(task -> !task.run());
        }
    }
}
