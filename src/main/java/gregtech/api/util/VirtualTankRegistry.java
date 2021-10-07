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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VirtualTankRegistry extends WorldSavedData {

    private static final int DEFAULT_CAPACITY = Integer.MAX_VALUE;
    private static final String DATA_ID = GTValues.MODID + ".vtank_data";

    protected static Map<String, IFluidTank> tankMap = new HashMap<>();
    protected static Map<String, Integer> refmap = new HashMap<>();

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
     * @param key The name of the tank
     * @return The tank object
     */
    public static IFluidTank getTank(String key) {
        return tankMap.get(key);
    }

    /**
     * @return the internal Map of tanks.
     * Do not use to modify the map!
     */
    public static Map<String, IFluidTank> getTankMap() {
        return tankMap;
    }

    /**
     * Retrieves a tank from the registry, creating it if it does not exist
     * @param key The name of the tank
     * @param capacity The initial capacity of the tank
     * @return The tank object
     */
    public static IFluidTank getTankCreate(String key, int capacity) {
        if (!tankMap.containsKey(key)) {
            addTank(key, capacity);
        }
        return getTank(key);
    }

    /**
     * Retrieves a tank from the registry, creating it with {@link #DEFAULT_CAPACITY the default capacity} if it does not exist
     * @param key The name of the tank
     * @return The tank object
     */
    public static IFluidTank getTankCreate(String key) {
        return getTankCreate(key, DEFAULT_CAPACITY);
    }

    /**
     * Adds a tank to the registry
     * @param key The name of the tank
     * @param capacity The initial capacity of the tank
     */
    public static void addTank(String key, int capacity) {
        if(tankMap.containsKey(key)) {
            GTLog.logger.warn("Overwriting virtual tank " + key + ", this might cause fluid loss!");
        }
        tankMap.put(key, new VirtualTank(capacity));
    }

    /**
     * Adds a tank to the registry with {@link #DEFAULT_CAPACITY the default capacity}
     * @param key The name of the tank
     */
    public static void addTank(String key) {
        addTank(key, DEFAULT_CAPACITY);
    }

    /**
     * Adds a reference to the specified tank in the reference counter.
     * @param key The name of the tank
     */
    public static void addRef(String key) {
        if (tankMap.containsKey(key)) {
            if (refmap.containsKey(key)) {
                refmap.put(key, refmap.get(key) + 1);
            } else {
                refmap.put(key, 1);
            }
        } else {
            GTLog.logger.warn("Attempted to add reference to virtual tank " + key + ", which does not exist in the tank map!");
        }
    }

    /**
     * Removes a reference to the specified tank in the reference counter
     * @param key The name of the tank
     * @param doCull Whether to remove the tank if it has no references and contains no fluid
     */
    public static void delRef(String key, boolean doCull) {
        if (tankMap.containsKey(key)) {
            if (refmap.containsKey(key)) {
                refmap.put(key, refmap.get(key) - 1);
                if (doCull && refmap.get(key) <= 0 && tankMap.get(key).getFluidAmount() <= 0) {
                    tankMap.remove(key);
                    refmap.remove(key);
                }
            } else {
                GTLog.logger.warn("Attempted to delete reference to virtual tank " + key + ", which does not exist in the reference map!");
            }
        } else {
            GTLog.logger.warn("Attempted to delete reference to virtual tank " + key + ", which does not exist in the tank map!");
        }
    }

    /**
     * Equivalent to {@link #delRef(String, boolean) delRef(key, true)}
     */
    public static void delRef(String key) {
        delRef(key, true);
    }

    /**
     * @param key The name of the tank
     * @return The number of counted references to the specified tank or -1 if it does not exist or is not tracked
     */
    public static int getRefs(String key) {
        if (tankMap.containsKey(key) && refmap.containsKey(key)) {
            return refmap.get(key);
        }
        return -1;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        for (String key : nbt.getKeySet()) {
            NBTTagCompound tankCompound = nbt.getCompoundTag(key);
            tankMap.put(key, new VirtualTank(tankCompound.getInteger("Capacity")));
            if (!tankMap.containsKey("Empty")){
                tankMap.get(key).fill(FluidStack.loadFluidStackFromNBT(tankCompound), true);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        tankMap.forEach( (key, tank) -> {
            NBTTagCompound tankCompound = new NBTTagCompound();
            tankCompound.setInteger("Capacity", tank.getCapacity());
            if (tank.getFluid() != null) {
                tank.getFluid().writeToNBT(tankCompound);
            } else {
                tankCompound.setString("Empty", "");
            }
            compound.setTag(key, tankCompound);
        });
        return compound;
    }

    @Override
    public boolean isDirty() {
        // can't think of a good way to mark dirty other than always
        return true;
    }

    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();
        assert storage != null;
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
                this.tankProperties = new IFluidTankProperties[]{ new VirtualTankProperties(this) };
            }
            return this.tankProperties;
        }

        @Override
        public int fill(FluidStack fluidStack, boolean doFill) {
            if (fluidStack == null || fluidStack.amount <= 0 || (this.fluid != null && !fluidStack.isFluidEqual(this.fluid)))
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

        private class VirtualTankProperties implements IFluidTankProperties {

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
