package gregtech.api.util.enderlink;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

public class VirtualContainerRegistry extends WorldSavedData {
    private static final int DEFAULT_SIZE = 9; // 9 slots
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
        }
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
        if (containerMap.containsKey(uuid) && containerMap.get(uuid).containsKey(key)) {
            GTLog.logger.warn("Overwriting virtual container " + key + "/" + (uuid == null ? "null" :uuid.toString()) + ", this might cause item loss!");
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
                if (containerMap.get(uuid).size() == 0) {
                    containerMap.remove(uuid);
                }
            }
        } else {
            GTLog.logger.warn("Attempted to delete container " + key + "/" + (uuid == null ? "null" :uuid.toString()) + ", which does not exist!");
        }
    }

    /**
     * To be called on server stopped event
     */
    public static void clearMaps() {
        containerMap.clear();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Public")) {
            NBTTagCompound publicContainers = nbt.getCompoundTag("Public");
            for (String key : publicContainers.getKeySet()) {
                NBTTagCompound containerCompound = publicContainers.getCompoundTag(key);
                VirtualContainerRegistry.getContainerCreate(key, null).deserializeNBT(containerCompound);
            }
        }
        if (nbt.hasKey("Private")) {
            NBTTagCompound privateContainerUUIDs = nbt.getCompoundTag("Private");
            for (String uuidStr : privateContainerUUIDs.getKeySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                NBTTagCompound privateContainers = privateContainerUUIDs.getCompoundTag(uuidStr);
                for (String key : privateContainers.getKeySet()) {
                    NBTTagCompound containerCompound = privateContainers.getCompoundTag(key);
                    VirtualContainerRegistry.getContainerCreate(key, uuid).deserializeNBT(containerCompound);
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
                NBTTagCompound containerCompound = container.serializeNBT();
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

    protected static class VirtualContainer implements IItemHandlerModifiable, IItemHandler, INBTSerializable<NBTTagCompound> {
        private NonNullList<ItemStack> stacks;
        public VirtualContainer(int size){
            this.setSize(size);
        }

        public void setSize(int size)
        {
            stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            validateSlotIndex(slot);
            this.stacks.set(slot, stack);
        }

        @Override
        public int getSlots()
        {
            return stacks.size();
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            validateSlotIndex(slot);
            return this.stacks.get(slot);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            validateSlotIndex(slot);

            ItemStack existing = this.stacks.get(slot);

            int limit = getStackLimit(slot, stack);

            if (!existing.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                    return stack;

                limit -= existing.getCount();
            }

            if (limit <= 0)
                return stack;

            boolean reachedLimit = stack.getCount() > limit;

            if (!simulate) {
                if (existing.isEmpty()) {
                    this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                } else {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
            }

            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0)
                return ItemStack.EMPTY;

            validateSlotIndex(slot);

            ItemStack existing = this.stacks.get(slot);

            if (existing.isEmpty())
                return ItemStack.EMPTY;

            int toExtract = Math.min(amount, existing.getMaxStackSize());

            if (existing.getCount() <= toExtract) {
                if (!simulate) {
                    this.stacks.set(slot, ItemStack.EMPTY);
                }
                return existing;
            } else {
                if (!simulate) {
                    this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                }

                return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
            }
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 64;
        }

        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagList nbtTagList = new NBTTagList();
            for (int i = 0; i < stacks.size(); i++) {
                if (!stacks.get(i).isEmpty()) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    itemTag.setInteger("Slot", i);
                    stacks.get(i).writeToNBT(itemTag);
                    nbtTagList.appendTag(itemTag);
                }
            }
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("Items", nbtTagList);
            nbt.setInteger("Size", stacks.size());
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
            NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
                int slot = itemTags.getInteger("Slot");

                if (slot >= 0 && slot < stacks.size()) {
                    stacks.set(slot, new ItemStack(itemTags));
                }
            }
        }

        protected void validateSlotIndex(int slot) {
            if (slot < 0 || slot >= stacks.size())
                throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }
}
