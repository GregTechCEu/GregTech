package gregtech.api.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

// this class should extend RegistryNamespaced but due to
// ForgeGradle bug (https://github.com/MinecraftForge/ForgeGradle/issues/498) it gives compile errors in CI environment
public class GTControlledRegistry<V> extends RegistrySimple<ResourceLocation, V> {

    protected boolean frozen = false;
    protected final int maxId;

    public GTControlledRegistry(int maxId) {
        this.maxId = maxId;
        this.inverseObjectRegistry = ((BiMap<ResourceLocation, V>) this.registryObjects).inverse();
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freezeRegistry() {
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen!");
        }
        this.frozen = true;
    }

    public void register(int id, ResourceLocation key, V value) {
        if (id < 0 || id >= maxId) {
            throw new IndexOutOfBoundsException("Id is out of range: " + id);
        }
        key = GameData.checkPrefix(key.toString());
        super.putObject(key, value);

        V objectWithId = getObjectById(id);
        if (objectWithId != null) {
            throw new IllegalArgumentException(String.format("Tried to reassign id %d to %s (%s), but it is already assigned to %s (%s)!",
                    id, value, key, objectWithId, getNameForObject(objectWithId)));
        }
        underlyingIntegerMap.put(value, id);
    }

    @Override
    public void putObject(@Nonnull ResourceLocation key, @Nonnull V value) {
        throw new UnsupportedOperationException("Use #register(int, String, T)");
    }

    public int getIdByObjectName(ResourceLocation key) {
        V valueWithKey = getObject(key);
        return valueWithKey == null ? 0 : getIDForObject(valueWithKey);
    }

//     =================== RegistryNamespaced stuff ===================

    protected final IntIdentityHashBiMap<V> underlyingIntegerMap = new IntIdentityHashBiMap<>(256);
    protected final Map<V, ResourceLocation> inverseObjectRegistry;

    @Nonnull
    @Override
    protected Map<ResourceLocation, V> createUnderlyingMap() {
        return HashBiMap.create();
    }

    @Nullable
    public ResourceLocation getNameForObject(V value) {
        return this.inverseObjectRegistry.get(value);
    }

    public int getIDForObject(@Nullable V value) {
        return this.underlyingIntegerMap.getId(value);
    }

    @Nullable
    public V getObjectById(int id) {
        return this.underlyingIntegerMap.get(id);
    }

    @Nonnull
    @Override
    public Iterator<V> iterator() {
        return this.underlyingIntegerMap.iterator();
    }
}
