package gregtech.api.pipenet.tickable;

import gregtech.api.GTValues;

import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@EventBusSubscriber(modid = GTValues.MODID)
public class TickableWorldPipeNetEventHandler {

    private static final List<Function<World, TickableWorldPipeNet<?, ?>>> pipeNetAccessors = new ArrayList<>();

    public static void registerTickablePipeNet(Function<World, TickableWorldPipeNet<?, ?>> pipeNetAccessor) {
        pipeNetAccessors.add(pipeNetAccessor);
    }

    private static Stream<TickableWorldPipeNet<?, ?>> getPipeNetsForWorld(World world) {
        return pipeNetAccessors.stream().map(accessor -> accessor.apply(world));
    }

    @SubscribeEvent
    public static void onWorldTick(WorldTickEvent event) {
        World world = event.world;
        if (world.isRemote)
            return;
        getPipeNetsForWorld(world).forEach(TickableWorldPipeNet::update);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote)
            return;
        getPipeNetsForWorld(world).forEach(it -> it.onChunkLoaded(event.getChunk()));
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote)
            return;
        getPipeNetsForWorld(world).forEach(it -> it.onChunkUnloaded(event.getChunk()));
    }
}
