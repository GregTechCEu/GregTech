package gregtech.api.util;

import gregtech.api.GTValues;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualTankRegistry extends WorldSavedData {

    private static final int DEFAULT_CAPACITY = 64000; // 64B
    private static final String DATA_ID = GTValues.MODID + ".vtank_data";

    protected static Map<UUID, Map<String, IFluidTank>> tankMap = new HashMap<>();

    public VirtualTankRegistry() {
        super(DATA_ID);
    }

    // for some reason, MapStorage throws an error if this constructor is not present
    @SuppressWarnings("unused")
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
        return tankMap.get(uuid).get(key);
    }

    /**
     * @return the internal Map of tanks.
     *         Do not use to modify the map!
     */
    public static Map<UUID, Map<String, IFluidTank>> getTankMap() {
        return tankMap;
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
        if (!tankMap.containsKey(uuid) || !tankMap.get(uuid).containsKey(key)) {
            addTank(key, uuid, capacity);
        }
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
        if (tankMap.containsKey(uuid) && tankMap.get(uuid).containsKey(key)) {
            GTLog.logger.warn("Overwriting virtual tank " + key + "/" + (uuid == null ? "null" : uuid.toString()) +
                    ", this might cause fluid loss!");
        } else if (!tankMap.containsKey(uuid)) {
            tankMap.put(uuid, new HashMap<>());
        }
        tankMap.get(uuid).put(key, new VirtualTank(capacity));
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
        if (tankMap.containsKey(uuid) && tankMap.get(uuid).containsKey(key)) {
            if (removeFluid || tankMap.get(uuid).get(key).getFluidAmount() <= 0) {
                tankMap.get(uuid).remove(key);
                if (tankMap.get(uuid).size() == 0) {
                    tankMap.remove(uuid);
                }
            }
        } else {
            GTLog.logger.warn("Attempted to delete tank " + key + "/" + (uuid == null ? "null" : uuid.toString()) +
                    ", which does not exist!");
        }
    }

    /**
     * To be called on server stopped event
     */
    public static void clearMaps() {
        tankMap.clear();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Public")) {
            NBTTagCompound publicTanks = nbt.getCompoundTag("Public");
            for (String key : publicTanks.getKeySet()) {
                NBTTagCompound tankCompound = publicTanks.getCompoundTag(key);
                VirtualTankRegistry.addTank(key, null, tankCompound.getInteger("Capacity"));
                if (!tankCompound.hasKey("Empty")) {
                    VirtualTankRegistry.getTank(key, null).fill(FluidStack.loadFluidStackFromNBT(tankCompound), true);
                }
            }
        }
        if (nbt.hasKey("Private")) {
            NBTTagCompound privateTankUUIDs = nbt.getCompoundTag("Private");
            for (String uuidStr : privateTankUUIDs.getKeySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                NBTTagCompound privateTanks = privateTankUUIDs.getCompoundTag(uuidStr);
                for (String key : privateTanks.getKeySet()) {
                    NBTTagCompound tankCompound = privateTanks.getCompoundTag(key);
                    VirtualTankRegistry.addTank(key, uuid, tankCompound.getInteger("Capacity"));
                    if (!tankCompound.hasKey("Empty")) {
                        VirtualTankRegistry.getTank(key, uuid).fill(FluidStack.loadFluidStackFromNBT(tankCompound),
                                true);
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Private", new NBTTagCompound());
        tankMap.forEach((uuid, map) -> {
            NBTTagCompound mapCompound = new NBTTagCompound();
            map.forEach((key, tank) -> {
                if (tank.getFluid() != null || tank.getCapacity() != DEFAULT_CAPACITY) {
                    NBTTagCompound tankCompound = new NBTTagCompound();
                    tankCompound.setInteger("Capacity", tank.getCapacity());
                    if (tank.getFluid() != null) {
                        tank.getFluid().writeToNBT(tankCompound);
                    } else {
                        tankCompound.setString("Empty", "");
                    }
                    mapCompound.setTag(key, tankCompound);
                }
            });
            if (mapCompound.getSize() > 0) {
                if (uuid == null) {
                    compound.setTag("Public", mapCompound);
                } else {
                    compound.getCompoundTag("Private").setTag(uuid.toString(), mapCompound);
                }
            }
        });
        return compound;
    }

    @Override
    public boolean isDirty() {
        // can't think of a good way to mark dirty other than always
        return true;
    }

    /**
     * To be called on world load event
     */
    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();
        VirtualTankRegistry instance = (VirtualTankRegistry) storage.getOrLoadData(VirtualTankRegistry.class, DATA_ID);

        if (instance == null) {
            instance = new VirtualTankRegistry();
            storage.setData(DATA_ID, instance);
        }
    }

    private static class VirtualTank implements IFluidTank, IFluidHandler {

        @Nullable
        protected FluidStack fluid;
        protected int capacity;
        protected IFluidTankProperties[] tankProperties;

        public VirtualTank(int capacity) {
            this.capacity = capacity;
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return this.fluid;
        }

        @Override
        public int getFluidAmount() {
            return this.fluid == null ? 0 : this.fluid.amount;
        }

        @Override
        public int getCapacity() {
            return this.capacity;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            if (this.tankProperties == null) {
                this.tankProperties = new IFluidTankProperties[] { new VirtualTankProperties(this) };
            }
            return this.tankProperties;
        }

        @Override
        public int fill(FluidStack fluidStack, boolean doFill) {
            if (fluidStack == null || fluidStack.amount <= 0 ||
                    (this.fluid != null && !fluidStack.isFluidEqual(this.fluid)))
                return 0;

            int fillAmt = Math.min(fluidStack.amount, this.capacity - this.getFluidAmount());
            if (doFill) {
                if (this.fluid == null) {
                    this.fluid = new FluidStack(fluidStack, fillAmt);
                } else {
                    this.fluid.amount += fillAmt;
                }
            }
            return fillAmt;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return resource == null || !resource.isFluidEqual(this.fluid) ? null : drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int amount, boolean doDrain) {
            if (this.fluid == null || amount <= 0)
                return null;

            int drainAmt = Math.min(this.getFluidAmount(), amount);
            FluidStack drainedFluid = new FluidStack(fluid, drainAmt);
            if (doDrain) {
                this.fluid.amount -= drainAmt;
                if (this.fluid.amount <= 0) {
                    this.fluid = null;
                }
            }
            return drainedFluid;
        }

        private static class VirtualTankProperties implements IFluidTankProperties {

            protected final VirtualTank tank;

            private VirtualTankProperties(VirtualTank tank) {
                this.tank = tank;
            }

            @Nullable
            @Override
            public FluidStack getContents() {
                FluidStack contents = tank.getFluid();
                return contents == null ? null : contents.copy();
            }

            @Override
            public int getCapacity() {
                return tank.getCapacity();
            }

            @Override
            public boolean canFill() {
                return true;
            }

            @Override
            public boolean canDrain() {
                return true;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return true;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return true;
            }
        }
    }
}
