package gregtech.api.block.coil;

import gregtech.api.GTValues;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;

public class CoilManager {

    private static CoilManager instance;
    private static int networkId;
    private static BuilderFactory internal;

    private final Object2ObjectMap<String, CoilRegistry> registryMap = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<CoilRegistry> networkMap = new Int2ObjectOpenHashMap<>();

    public static CoilManager getInstance() {
        if (instance == null) {
            instance = new CoilManager();
            internal = instance.createRegistry(GTValues.MODID);
        }
        return instance;
    }

    private CoilManager() {}

    public BuilderFactory getRegistry(String modid) {
        CoilRegistry coilRegistry = registryMap.get(modid);
        if (coilRegistry == null) {
            throw new IllegalArgumentException("No MTE registry exists for modid \"" + modid + "\"");
        }
        return coilRegistry;
    }

    public BuilderFactory createRegistry(String modid) {
        if (registryMap.containsKey(modid)) {
            throw new IllegalArgumentException("MTE Registry for modid \"" + modid + "\" is already registered");
        }
        CoilRegistry registry = new CoilRegistry(modid, ++networkId);
        registryMap.put(modid, registry);
        networkMap.put(networkId, registry);
        return registry;
    }

    public BuilderFactory getRegistry(int networkId) {
        CoilRegistry coilRegistry = networkMap.get(networkId);
        return coilRegistry == null ? internal : coilRegistry;
    }

    public Collection<CoilRegistry> getRegistries() {
        return registryMap.values();
    }

    // event class
    public static class CoilRegistryEvent extends Event {}
}
