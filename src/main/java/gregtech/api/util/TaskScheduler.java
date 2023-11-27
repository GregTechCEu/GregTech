package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.util.function.Task;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = GTValues.MODID)
public class TaskScheduler {

    @Nullable
    public static TaskScheduler get(World world) {
        return tasksPerWorld.get(world);
    }

    private static final Map<World, TaskScheduler> tasksPerWorld = new HashMap<>();

    private final List<Task> tasks = new ArrayList<>();
    private final List<Task> scheduledTasks = new ArrayList<>();
    private boolean running = false;

    public static void scheduleTask(World world, Task task) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Attempt to schedule task on client world!");
        }
        tasksPerWorld.computeIfAbsent(world, k -> new TaskScheduler()).scheduleTask(task);
    }

    public void scheduleTask(Task task) {
        if (running) {
            scheduledTasks.add(task);
        } else {
            tasks.add(task);
        }
    }

    public void unload() {
        tasks.clear();
        scheduledTasks.clear();
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            tasksPerWorld.remove(event.getWorld());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote && event.phase == TickEvent.Phase.START) {
            TaskScheduler scheduler = get(event.world);
            if (scheduler != null) {
                if (!scheduler.scheduledTasks.isEmpty()) {
                    scheduler.tasks.addAll(scheduler.scheduledTasks);
                    scheduler.scheduledTasks.clear();
                }
                scheduler.running = true;
                scheduler.tasks.removeIf(task -> !task.run());
                scheduler.running = false;
            }
        }
    }
}
