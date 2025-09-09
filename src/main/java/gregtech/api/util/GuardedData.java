package gregtech.api.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides a strict system for guarding generic data and an NBT tag,
 * allowing mutability and copying without affecting what is saved to disk until prompted.
 * Useful in multithreaded contexts, or where changes to data may need to be undone,
 * or where an operation must be done in one go and yet spread across a period of time.
 */
public final class GuardedData<T> implements INBTSerializable<NBTTagCompound> {

    private @Nullable T transientData;
    private @Nullable NBTTagCompound unsavedData;
    private @NotNull NBTTagCompound savedData = new NBTTagCompound();

    private final @Nullable Supplier<T> newData;
    private final Function<T, T> copyData;

    public GuardedData(@Nullable Supplier<T> newData, @NotNull Function<T, T> copyData) {
        this.newData = newData;
        this.copyData = copyData;
    }

    public static GuardedData<Map<String, Object>> stringKeyedData() {
        return new GuardedData<>(Object2ObjectOpenHashMap::new, m -> {
            Map<String, Object> map = new Object2ObjectOpenHashMap<>(Math.max(16, m.size()));
            map.putAll(m);
            return map;
        });
    }

    /**
     * Sets the transient data storage.
     * @param transientData the data to set to.
     */
    public void setTransientData(@Nullable T transientData) {
        this.transientData = transientData;
    }

    /**
     * @return the transient data storage. Safe to mutate.
     */
    public T getTransientData() {
        if (newData != null && transientData == null) transientData = newData.get();
        return transientData;
    }

    /**
     * @return the unsaved data storage. Safe to mutate.
     */
    public @NotNull NBTTagCompound getData() {
        if (unsavedData == null) unsavedData = savedData.copy();
        return unsavedData;
    }

    /**
     * Copies mutable NBT data to saved data and clears the transient data.
     * Transient data is always cleared on this operation, to protect from relying on
     * transient data from before the cache, and the transient data not being there
     * if a disk save & load interrupts.
     * @apiNote This does not invalidate external references to unsaved data. If spooky
     *          action at a distance is suspected, call {@link #clearTransientData()} as well.
     * @return this object, for convenience.
     */
    @Contract("->this")
    public GuardedData<T> cacheState() {
        transientData = null;
        if (unsavedData == null) {
            savedData = new NBTTagCompound();
        } else {
            savedData = unsavedData.copy();
        }
        return this;
    }

    /**
     * Clears transient data and unsaved changes to data. Can be used to simulate a
     * disk save & load, or just undo an operation that was deemed undesirable after the fact.
     * @return this object, for convenience.
     */
    @Contract("->this")
    public GuardedData<T> resetState() {
        transientData = null;
        unsavedData = savedData.copy();
        return this;
    }

    /**
     * Clears transient data and invalidates any external references to unsaved data.
     * Useful if this object is about to be sent across threads.
     * @return this object, for convenience.
     */
    @Contract("->this")
    public GuardedData<T> clearTransientData() {
        transientData = null;
        if (unsavedData != null) unsavedData = unsavedData.copy();
        return this;
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        return savedData;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        savedData = compound;
        unsavedData = compound.copy();
        transientData = null;
    }

    /**
     * Creates a complete copy of this object, including unsaved and saved data.
     * @return the full copy
     */
    public GuardedData<T> copyFullState() {
        GuardedData<T> copy = new GuardedData<>(newData, copyData);
        if (transientData != null) {
            copy.transientData = copyData.apply(transientData);
        }
        if (unsavedData != null) copy.unsavedData = unsavedData.copy();
        copy.savedData = savedData.copy();
        return copy;
    }

    /**
     * Creates a copy of this object containing only the saved data.
     * @return the safe copy
     */
    public GuardedData<T> copySafeState() {
        GuardedData<T> copy = new GuardedData<>(newData, copyData);
        copy.savedData = savedData.copy();
        return copy;
    }

    /**
     * Sets this object's unsaved state to a copy of the other object's unsaved state.
     * This object's saved state is not affected, unless {@link #cacheState()} is called.
     * @param other the object to grab data from
     * @return this object, for convenience.
     */
    @Contract("_->this")
    public GuardedData<T> grabUnsafeState(GuardedData<T> other) {
        if (other.transientData != null) {
            transientData = copyData.apply(other.transientData);
        } else {
            this.transientData = null;
        }
        if (other.unsavedData != null) {
            this.unsavedData = other.unsavedData.copy();
        } else {
            this.unsavedData = null;
        }
        return this;
    }

    /**
     * Sets this object's unsaved state to a copy of the other object's saved state.
     * This object's saved state is not affected, unless {@link #cacheState()} is called.
     * @param other the object to grab data from
     * @return this object, for convenience.
     */
    @Contract("_->this")
    public GuardedData<T> grabSafeState(GuardedData<?> other) {
        this.transientData = null;
        this.unsavedData = other.savedData.copy();
        return this;
    }
}
