package gregtech.api.util;

import gregtech.api.GTValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemCarrotOnAStick;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualContainerRegistry extends WorldSavedData{
    private static final int DEFAULT_SIZE = 27; // 27 slots
    private static final String DATA_ID = GTValues.MODID + ".vcontainer_data";

    protected static Map<UUID, Map<String, IInventory>> containerMap = new HashMap<>();

    public VirtualContainerRegistry(){
        super(DATA_ID);
    }

    public VirtualContainerRegistry(String name){
        super(name);
    }

    /**
     * Retrieves a container from the registry
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @return The container object
     */
    public static IInventory getContainer(String key, UUID uuid) {
        return containerMap.get(uuid).get(key);
    }

    /**
     * @return the internal Map of containers.
     * Do not use to modify the map!
     */
    public static Map<UUID, Map<String, IInventory>> getContainerMap() {
        return containerMap;
    }

    /**
     * Retrieves a container from the registry, creating it if it does not exist
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @param size The initial size of the container
     * @return The container object
     */
    public static IInventory getContainerCreate(String key, UUID uuid, int size) {
        if (!containerMap.containsKey(uuid) || !containerMap.get(uuid).containsKey(key)) {
            addContainer(key, uuid, size);
        }
        return getContainer(key, uuid);
    }

    /**
     * Adds a tank to the registry
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @param size The initial size of the container
     */
    public static void addContainer(String key, UUID uuid, int size) {
        if(containerMap.containsKey(uuid) && containerMap.get(uuid).containsKey(key)) {
            GTLog.logger.warn("Overwriting virtual tank " + key + "/" + (uuid == null ? "null" :uuid.toString()) + ", this might cause fluid loss!");
        } else if (!containerMap.containsKey(uuid)) {
            containerMap.put(uuid, new HashMap<>());
        }
        containerMap.get(uuid).put(key, new VirtualContainer(size));
    }

    /**
     * Adds a container to the registry with {@link #DEFAULT_SIZE the default size}
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     */
    public static void addContainer(String key, UUID uuid) {
        addContainer(key, uuid, DEFAULT_SIZE);
    }

    /**
     * Removes a container from the registry. Use with caution!
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @param removeContainer Whether to remove the container if it has items in it
     */
    public static void delContainer(String key, UUID uuid, boolean removeContainer) {
        if (containerMap.containsKey(uuid) && containerMap.get(uuid).containsKey(key)) {
            if (removeContainer || containerMap.get(uuid).get(key).isEmpty()) {
                containerMap.get(uuid).remove(key);
                if (containerMap.get(uuid).size() <= 0) {
                    containerMap.remove(uuid);
                }
            }
        } else {
            GTLog.logger.warn("Attempted to delete container " + key + "/" + (uuid == null ? "null" :uuid.toString()) + ", which does not exist!");
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Public")) {
            NBTTagCompound publicContainers = nbt.getCompoundTag("Public");
            for (String key : publicContainers.getKeySet()) {
                NBTTagCompound containerCompound = publicContainers.getCompoundTag(key);
                VirtualContainerRegistry.addContainer(key, null, containerCompound.getInteger("Size"));
                if (!containerCompound.hasKey("Empty")) {
                    // VirtualContainerRegistry.getContainer(key, null).fill(FluidStack.loadFluidStackFromNBT(tankCompound), true);
                }
            }
        }
        if (nbt.hasKey("Private")) {
            NBTTagCompound privateContainerUUIDs = nbt.getCompoundTag("Private");
            for (String uuidStr : privateContainerUUIDs.getKeySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                NBTTagCompound privateContainers = privateContainerUUIDs.getCompoundTag(uuidStr);
                for (String key : privateContainers.getKeySet()) {
                    NBTTagCompound containerCompound = privateContainers.getCompoundTag(key);
                    VirtualContainerRegistry.addContainer(key, uuid, containerCompound.getInteger("Size"));
                    if (!containerCompound.hasKey("Empty")) {
                        // VirtualContainerRegistry.getContainer(key, uuid);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Private", new NBTTagCompound());
        containerMap.forEach( (uuid, map) -> {
            NBTTagCompound mapCompound = new NBTTagCompound();
            map.forEach( (key, container) -> {
                if (container.getSizeInventory() > 0 || container != null) {
                    NBTTagCompound containerCompound = new NBTTagCompound();
                    containerCompound.setInteger("Capacity", container.getSizeInventory());
                    if (true) {
                        // container.getFluid().writeToNBT(containerCompound);
                    } else {
                        containerCompound.setString("Empty", "");
                    }
                    mapCompound.setTag(key, containerCompound);
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
        VirtualContainerRegistry instance = (VirtualContainerRegistry) storage.getOrLoadData(VirtualContainerRegistry.class, DATA_ID);

        if (instance == null) {
            instance = new VirtualContainerRegistry();
            storage.setData(DATA_ID, instance);
        }
    }

    private static class VirtualContainer implements  IInventory, IItemHandler {
        @Nullable
        protected ItemStack[] items;
        protected int size;
        protected boolean isDirty;


        public VirtualContainer(int size){
            this.size = size;
            items = new ItemStack[this.size];
        }

        @Override
        public int getSizeInventory() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            return this.size > 0 ? true : false;
        }

        @Override
        public int getSlots() {
            return this.size;
        }

        @Override
        public ItemStack getStackInSlot(int i) {
            return items.length > 0 ? items[i] : null;
        }

        /**
         *
         * @param slot index of the array to insert the ItemStack into.
         * @param itemStack ItemStack to insert into the array
         * @param simulate Simulates item input, but does not actually insert the item
         * @return a copy of the ItemStack with the adjusted amount
         */
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack itemStack, boolean simulate) {
            int amt = items[slot].getMaxStackSize() - items[slot].getCount();
            int remainder = Math.min(amt, itemStack.getCount());;
            ItemStack itemCopy;

            if (!simulate && (items[slot].isItemEqual(itemStack) || items[slot] == null)) {
                // case 1: 64 items in slot, amt should be 0 and incoming item stack is returned unchanged
                // case 2: 32 items in slot, amt should be 32 and incoming item stack is returned with 0 or self - 32
                // case 3: no item in slot, amt should be item stack count and an empty item stack is returned

                items[slot].setCount(items[slot].getCount() + remainder);
            }
            itemCopy = itemStack;
            itemCopy.setCount(remainder);
            return itemCopy;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amt, boolean simulate) {
            return null;
        }

        @Override
        public int getSlotLimit(int slot) {
            return items[slot].getMaxStackSize();
        }

        @Override
        public ItemStack decrStackSize(int slot, int amt) {
            return null;
        }

        @Override
        public ItemStack removeStackFromSlot(int slot) {
            ItemStack itemReturnable = items[slot];
            items[slot] = null;
            return itemReturnable;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack itemStack) {
            items[slot] = itemStack;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {
            isDirty = true;
        }

        @Override
        public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
            return false;
        }

        @Override
        public void openInventory(EntityPlayer entityPlayer) {
            return;
        }

        @Override
        public void closeInventory(EntityPlayer entityPlayer) {
            return;
        }

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return false;
        }

        @Override
        public int getField(int slot) {
            return 0;
        }

        @Override
        public void setField(int slot, int amount) {

        }

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public void clear() {
            items = new ItemStack[size];
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Override
        public ITextComponent getDisplayName() {
            return null;
        }
    }
}
