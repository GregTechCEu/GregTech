package gregtech.api.fluids.store;

import gregtech.api.fluids.FluidState;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;

import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class FluidStorageKey {

    private static final Map<ResourceLocation, FluidStorageKey> keys = new Object2ObjectOpenHashMap<>();

    private final ResourceLocation resourceLocation;
    private final MaterialIconType iconType;
    private final UnaryOperator<String> registryNameOperator;
    private final Function<Material, String> translationKeyFunction;
    private final int hashCode;
    private final FluidState defaultFluidState;

    public FluidStorageKey(@NotNull ResourceLocation resourceLocation, @NotNull MaterialIconType iconType,
                           @NotNull UnaryOperator<@NotNull String> registryNameOperator,
                           @NotNull Function<@NotNull Material, @NotNull String> translationKeyFunction,
                           @Nullable FluidState defaultFluidState) {
        this.resourceLocation = resourceLocation;
        this.iconType = iconType;
        this.registryNameOperator = registryNameOperator;
        this.translationKeyFunction = translationKeyFunction;
        this.hashCode = resourceLocation.hashCode();
        this.defaultFluidState = defaultFluidState;
        if (keys.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Cannot create duplicate keys");
        }
        keys.put(resourceLocation, this);
    }

    public static @Nullable FluidStorageKey getByName(@NotNull ResourceLocation location) {
        return keys.get(location);
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    public @NotNull MaterialIconType getIconType() {
        return this.iconType;
    }

    /**
     * @param baseName the base name of the fluid
     * @return the registry name to use
     */
    public @NotNull String getRegistryNameFor(@NotNull String baseName) {
        return registryNameOperator.apply(baseName);
    }

    /**
     * @return the translation key for fluids with this key
     */
    public @NotNull String getTranslationKeyFor(@NotNull Material material) {
        return this.translationKeyFunction.apply(material);
    }

    /**
     * @return the default fluid state for this storage key, if it exists.
     */
    public @Nullable FluidState getDefaultFluidState() {
        return defaultFluidState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluidStorageKey fluidKey = (FluidStorageKey) o;

        return resourceLocation.equals(fluidKey.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidStorageKey{" + resourceLocation + '}';
    }
}
