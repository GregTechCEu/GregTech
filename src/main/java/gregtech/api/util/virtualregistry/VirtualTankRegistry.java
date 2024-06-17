package gregtech.api.util.virtualregistry;

import gregtech.api.GTValues;

import gregtech.api.util.GTLog;

import gregtech.api.util.virtualregistry.entries.VirtualTank;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualTankRegistry extends VirtualRegistryBase {

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
    public static IFluidTank getTank(String key, UUID uuid) {
        return (IFluidTank) getEntry(uuid, EntryType.ENDER_FLUID, key);
    }

    /**
     * @return the internal Map of tanks.
     *         Do not use to modify the map!
     */
    // todo remove
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
    public static IFluidTank getTankCreate(String key, UUID uuid, int capacity) {
        if (!hasEntry(uuid, EntryType.ENDER_FLUID, key))
            addTank(key, uuid, capacity);

        return getTank(key, uuid);
    }

    /**
     * Retrieves a tank from the registry, creating it with {@link #DEFAULT_CAPACITY the default capacity} if it does
     * not exist
     * 
     * @param key  The name of the tank
     * @param uuid The uuid of the player the tank is private to, or null if the tank is public
     * @return The tank object
     */
    public static IFluidTank getTankCreate(String key, UUID uuid) {
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
        var tank = new VirtualTank();
        tank.setName(key);
        tank.setCapacity(capacity);
        addEntry(uuid, tank);
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
            deleteEntry(uuid, EntryType.ENDER_FLUID, key);
        else if (tank.getFluidAmount() == 0)
            deleteEntry(uuid, EntryType.ENDER_FLUID, key);
    }
}
