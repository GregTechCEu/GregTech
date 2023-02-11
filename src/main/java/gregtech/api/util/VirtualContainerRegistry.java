package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverConveyor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemCarrotOnAStick;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class VirtualContainerRegistry extends WorldSavedData{
    private static final int DEFAULT_SIZE = 9; // 9 slots
    private static final String DATA_ID = GTValues.MODID + ".vcontainer_data";

    protected static Map<UUID, Map<String, IItemHandler>> containerMap = new HashMap<>();

    public VirtualContainerRegistry(){
        super(DATA_ID);
    }
    // for some reason, MapStorage throws an error if this constructor is not present
    @SuppressWarnings("unused")
    public VirtualContainerRegistry(String name){
        super(name);
    }

    /**
     * Retrieves a container from the registry
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @return The container object
     */
    public static IItemHandlerModifiable getContainer(String key, UUID uuid) {
        return (IItemHandlerModifiable) containerMap.get(uuid).get(key);
    }

    /**
     * @return the internal Map of containers.
     * Do not use to modify the map!
     */
    public static Map<UUID, Map<String, IItemHandler>> getContainerMap() {
        return containerMap;
    }

    /**
     * Retrieves a container from the registry, creating it if it does not exist
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @param size The initial size of the container
     * @return The container object
     */
    public static IItemHandlerModifiable getContainerCreate(String key, UUID uuid, int size) {
        if (!containerMap.containsKey(uuid) || !containerMap.get(uuid).containsKey(key)) {
            addContainer(key, uuid, size);
        }
        return getContainer(key, uuid);
    }

    /**
     * Retrieves a container from the registry, creating it with {@link #DEFAULT_SIZE the default size} if it does not exist
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @return The container object
     */
    public static IItemHandlerModifiable getContainerCreate(String key, UUID uuid) {
        return getContainerCreate(key, uuid, DEFAULT_SIZE);
    }

    /**
     * Adds a container to the registry
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @param size The initial size of the container
     */
    public static void addContainer(String key, UUID uuid, int size) {
        if(containerMap.containsKey(uuid) && containerMap.get(uuid).containsKey(key)) {
            GTLog.logger.warn("Overwriting virtual container " + key + "/" + (uuid == null ? "null" :uuid.toString()) + ", this might cause fluid loss!");
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
            boolean isEmpty = true;
            IItemHandler container = containerMap.get(uuid).get(key);

            for (int i = 0; i < container.getSlots(); i++) {
                if (!container.getStackInSlot(i).isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }

            if (removeContainer || isEmpty) {
                containerMap.get(uuid).remove(key);
                if (containerMap.get(uuid).size() <= 0) {
                    containerMap.remove(uuid);
                }
            }
        } else {
            GTLog.logger.warn("Attempted to delete container " + key + "/" + (uuid == null ? "null" :uuid.toString()) + ", which does not exist!");
        }
    }

    public static void clearMaps() {
        containerMap.clear();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Public")) {
            NBTTagCompound publicContainers = nbt.getCompoundTag("Public");
            for (String key : publicContainers.getKeySet()) {
                NBTTagCompound containerCompound = publicContainers.getCompoundTag(key);
                GTUtility.readItems(VirtualContainerRegistry.getContainer(key, null), "Slots", containerCompound);
            }
        }
        if (nbt.hasKey("Private")) {
            NBTTagCompound privateContainerUUIDs = nbt.getCompoundTag("Private");
            for (String uuidStr : privateContainerUUIDs.getKeySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                NBTTagCompound privateContainers = privateContainerUUIDs.getCompoundTag(uuidStr);
                for (String key : privateContainers.getKeySet()) {
                    NBTTagCompound containerCompound = privateContainers.getCompoundTag(key);
                    GTUtility.readItems(VirtualContainerRegistry.getContainer(key, uuid), "Slots", containerCompound);
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
                NBTTagCompound containerCompound = new NBTTagCompound();
                GTUtility.writeItems(container, "Slots", containerCompound);
                mapCompound.setTag(key, containerCompound);
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

    protected static class VirtualContainer implements IItemHandlerModifiable {

        private int size;
        private NonNullList<ItemStack> items;

        public VirtualContainer(int size){
            this.size = size;
            this.items = NonNullList.withSize(this.size, ItemStack.EMPTY);
            GTLog.logger.warn("Virtual Container of size: {} ({}) has been constructed", size, this.items.size());
        }

        @Override
        public int getSlots() {
            return items.size();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int i) {
            return items.get(i);
        }

        /**
         *
         * @param slot index of the array to insert the ItemStack into.
         * @param itemStack ItemStack to insert into the array
         * @param simulate Should the List<ItemStack> be modified?
         * @return a copy of the ItemStack with the adjusted amount
         */
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack itemStack, boolean simulate) {
            if (itemStack.isEmpty())
                return ItemStack.EMPTY;

            int amt; // theoretically, how much can be inserted?
            if (items.get(slot).isEmpty())
                amt = items.get(slot).getMaxStackSize();
            else
                amt = items.get(slot).getMaxStackSize() - items.get(slot).getCount();
            int remainder = Math.max(0, itemStack.getCount() - amt);  // what is left of the incoming item stack?

            if (!simulate){
                items.set(slot, itemStack);
                items.get(slot).setCount(itemStack.getCount());

            }

            if (remainder <= 0) { // not enough or exactly for a full stack
                return ItemStack.EMPTY;
            }
            else { // more than a full stack, there are leftover incoming items to return
                itemStack.setCount(remainder);
                return itemStack;
            }
        }

        /**
         *
         * @param slot - The slot to extract from
         * @param amtToExtract - The amount to extract from the slot
         * @param simulate - Should the List<ItemStack> be modified?
         * @return - Returns the item extracted
         */
        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amtToExtract, boolean simulate) {
            if (items.get(slot).isEmpty())
                return ItemStack.EMPTY;

            int remainder = amtToExtract - items.get(slot).getCount();
            ItemStack returnable = items.get(slot).copy();
            returnable.setCount(amtToExtract - remainder);

            if (!simulate) // extracted all item in slot
                items.set(slot, ItemStack.EMPTY);
            return returnable;
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.get(slot).getMaxStackSize();
        }

        @Override
        public void setStackInSlot(int i, @Nonnull ItemStack itemStack) {
            this.items.set(i, itemStack);
        }
    }
}
