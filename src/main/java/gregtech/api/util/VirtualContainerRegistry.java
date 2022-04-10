package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverConveyor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemCarrotOnAStick;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class VirtualContainerRegistry extends WorldSavedData{
    private static final int DEFAULT_SIZE = 27; // 27 slots
    private static final String DATA_ID = GTValues.MODID + ".vcontainer_data";

    protected static Map<UUID, Map<String, VirtualContainer>> containerMap = new HashMap<>();

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
    public static VirtualContainer getContainer(String key, UUID uuid) {
        return containerMap.get(uuid).get(key);
    }

    /**
     * @return the internal Map of containers.
     * Do not use to modify the map!
     */
    public static Map<UUID, Map<String, VirtualContainer>> getContainerMap() {
        return containerMap;
    }

    /**
     * Retrieves a container from the registry, creating it if it does not exist
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @param size The initial size of the container
     * @return The container object
     */
    public static VirtualContainer getContainerCreate(String key, UUID uuid, int size) {
        if (!containerMap.containsKey(uuid) || !containerMap.get(uuid).containsKey(key)) {
            addContainer(key, uuid, size);
            /*GTLog.logger.warn(
                    "\nCreated new container" +
                    "\nKey: " + key +
                    "\nUUID: " + (uuid == null ? "null" : uuid) +
                    "\nSize: " + size
            );*/
        }
        /*GTLog.logger.warn(
                "\nRetrieved existing container" +
                "\nKey: " + key +
                "\nUUID: " + (uuid == null ? "null" : uuid)
        );*/
        return getContainer(key, uuid);
    }

    /**
     * Retrieves a container from the registry, creating it with {@link #DEFAULT_SIZE the default size} if it does not exist
     * @param key The name of the container
     * @param uuid The uuid of the player the container is private to, or null if the container is public
     * @return The container object
     */
    public static VirtualContainer getContainerCreate(String key, UUID uuid) {
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
                NBTTagCompound itemCompound = containerCompound.getCompoundTag(key);
                VirtualContainerRegistry.addContainer(key, null, containerCompound.getInteger("Size"));

                GTUtility.readItems(
                        getContainerCreate(key, null, containerCompound.getInteger("Size")),
                        "Slots",
                        itemCompound);
            }
        }
        if (nbt.hasKey("Private")) {
            NBTTagCompound privateContainerUUIDs = nbt.getCompoundTag("Private");
            for (String uuidStr : privateContainerUUIDs.getKeySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                NBTTagCompound privateContainers = privateContainerUUIDs.getCompoundTag(uuidStr);
                for (String key : privateContainers.getKeySet()) {
                    NBTTagCompound containerCompound = privateContainers.getCompoundTag(key);
                    NBTTagCompound itemCompound = containerCompound.getCompoundTag(key);
                    GTUtility.readItems(
                            getContainerCreate(key, uuid, containerCompound.getInteger("Size")),
                            "Slots",
                            itemCompound
                    );

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
                NBTTagCompound itemCompound = new NBTTagCompound();
                containerCompound.setInteger("Size", container.getSlots());
                GTUtility.writeItems(
                        container,
                        "Slots",
                        itemCompound
                );
                containerCompound.setTag(key, itemCompound);
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

        protected int size;
        protected ArrayList<ItemStack> items;

        public VirtualContainer(int size){
            this.size = size;
            // items = new ArrayList<>(this.size);
            items = new ArrayList<>(this.size);
            for (int slot = 0; slot < this.size; slot++) {
                items.add(slot, ItemStack.EMPTY);
            }
            GTLog.logger.debug("Virtual Container of size: " + size + " (" + items.size() + ") " + " has been constructed");
        }

        @Override
        public int getSlots() {
            return items.size();
        }

        @Override
        public ItemStack getStackInSlot(int i) {
            return items.get(i) != ItemStack.EMPTY ? items.get(i) : ItemStack.EMPTY;
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
            /*GTLog.logger.warn(
                    "\nItem to be inserted: " + itemStack +
                    "\nAmount to be inserted: " + itemStack.getCount() +
                    "\nItem to return: " + itemStack.getDisplayName() + " count: " + remainder +
                    "\nSimulate: " + simulate);*/
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

            /*GTLog.logger.warn(
                    "\nItem to be extracted: " + items.get(slot) +
                    "\nAmount to extract: " + amtToExtract +
                    "\nItem to return: " + returnable +
                    "\nSimulate: " + simulate);*/
            return returnable;
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.get(slot).getMaxStackSize();
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack itemStack) {
            items.set(slot, itemStack);
        }

        public boolean isEmpty(){
            for (ItemStack item : items) {
                if (!item.isEmpty())
                    return false;
            }
            return true;
        }
    }
}
