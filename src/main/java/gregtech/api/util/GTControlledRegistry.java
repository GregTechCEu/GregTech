package gregtech.api.util;

import gregtech.api.GTValues;

import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.jetbrains.annotations.NotNull;

public class GTControlledRegistry<K, V> extends RegistryNamespaced<K, V> {

    protected boolean frozen = true;
    protected final int maxId;

    public GTControlledRegistry(int maxId) {
        this.maxId = maxId;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen!");
        }

        if (!checkActiveModContainerIsGregtech()) {
            return;
        }

        this.frozen = true;
    }

    public void unfreeze() {
        if (!frozen) {
            throw new IllegalStateException("Registry is already unfrozen!");
        }

        if (!checkActiveModContainerIsGregtech()) {
            return;
        }

        this.frozen = false;
    }

    private static boolean checkActiveModContainerIsGregtech() {
        ModContainer container = Loader.instance().activeModContainer();
        return container != null && container.getModId().equals(GTValues.MODID);
    }

    public void register(int id, @NotNull K key, @NotNull V value) {
        if (id < 0 || id >= maxId) {
            throw new IndexOutOfBoundsException("Id is out of range: " + id);
        }

        super.putObject(key, value);

        V objectWithId = getObjectById(id);
        if (objectWithId != null) {
            throw new IllegalArgumentException(
                    String.format("Tried to reassign id %d to %s (%s), but it is already assigned to %s (%s)!",
                            id, value, key, objectWithId, getNameForObject(objectWithId)));
        }
        underlyingIntegerMap.put(value, id);
    }

    @Override
    public void putObject(@NotNull K key, @NotNull V value) {
        throw new UnsupportedOperationException("Use #register(int, String, T)");
    }

    public int getIdByObjectName(K key) {
        V valueWithKey = getObject(key);
        return valueWithKey == null ? 0 : getIDForObject(valueWithKey);
    }
}
