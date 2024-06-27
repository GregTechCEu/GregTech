package gregtech.api.util.virtualregistry;

import gregtech.api.util.virtualregistry.entries.VirtualTank;

import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class VirtualTankRegistry extends VirtualRegistryBase {

    private static final int DEFAULT_CAPACITY = 64000; // 64B

    public VirtualTankRegistry(String name) {
        super(name);
    }

    /**
     * Retrieves a tank from the registry
     * 
     * @param key  The name of the tank
     * @param uuid The uuid of the player the tank is private to, or null if the tank is public
     * @return The tank object
     */
    public static VirtualTank getTank(String key, UUID uuid) {
        return getEntry(uuid, EntryTypes.ENDER_FLUID, key);
    }

    /**
     * Deprecated, use {@link VirtualRegistryBase#getEntryNames(UUID, EntryTypes)} to get a list of names to display.
     * This now returns an empty map.
     *
     * @return the internal Map of tanks.
     *         Do not use to modify the map!
     */
    @Deprecated
    public static Map<UUID, Map<String, IFluidTank>> getTankMap() {
        return new HashMap<>();
    }

    /**
     * Retrieves a tank from the registry, creating it if it does not exist
     * 
     * @param key      The name of the tank
     * @param uuid     The uuid of the player the tank is private to, or null if the tank is public
     * @param capacity The initial capacity of the tank
     * @return The tank object
     */
    public static VirtualTank getTankCreate(String key, UUID uuid, int capacity) {
        if (!hasTank(key, uuid))
            addTank(key, uuid, capacity);

        return getTank(key, uuid);
    }

    /**
     * Checks if the tank exists in the registry
     *
     * @param key  The name of the tank
     * @param uuid The uuid of the player the tank is private to, or null if the tank is public
     * @return true if the tank exists, otherwise false
     */
    public static boolean hasTank(String key, UUID uuid) {
        return hasEntry(uuid, EntryTypes.ENDER_FLUID, key);
    }

    /**
     * Retrieves a tank from the registry, creating it with {@link #DEFAULT_CAPACITY the default capacity} if it does
     * not exist
     * 
     * @param key  The name of the tank
     * @param uuid The uuid of the player the tank is private to, or null if the tank is public
     * @return The tank object
     */
    public static VirtualTank getTankCreate(String key, UUID uuid) {
        return getTankCreate(key, uuid, DEFAULT_CAPACITY);
    }

    /**
     * Adds a tank to the registry
     * 
     * @param key      The name of the tank
     * @param uuid     The uuid of the player the tank is private to, or null if the tank is public
     * @param capacity The initial capacity of the tank
     */
    public static void addTank(String key, UUID uuid, int capacity) {
        addEntry(uuid, key, new VirtualTank(capacity));
    }

    /**
     * Adds a tank to the registry with {@link #DEFAULT_CAPACITY the default capacity}
     * 
     * @param key  The name of the tank
     * @param uuid The uuid of the player the tank is private to, or null if the tank is public
     */
    public static void addTank(String key, UUID uuid) {
        addTank(key, uuid, DEFAULT_CAPACITY);
    }

    /**
     * Removes a tank from the registry. Use with caution!
     * 
     * @param key         The name of the tank
     * @param uuid        The uuid of the player the tank is private to, or null if the tank is public
     * @param removeFluid Whether to remove the tank if it has fluid in it
     */
    public static void delTank(String key, UUID uuid, boolean removeFluid) {
        var tank = getTank(key, uuid);
        if (removeFluid && tank.getFluidAmount() >= 0)
            deleteEntry(uuid, EntryTypes.ENDER_FLUID, key);
        else if (tank.getFluidAmount() == 0)
            deleteEntry(uuid, EntryTypes.ENDER_FLUID, key);
    }
}
