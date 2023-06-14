package gregtech.api.unification.ore.handler;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.function.TriConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static gregtech.api.unification.material.info.MaterialFlags.NO_UNIFICATION;

public final class OreProcessorHandler implements IOreProcessorHandler {

    private static OreProcessorHandler INSTANCE;

    public static OreProcessorHandler getInstance() {
        if (INSTANCE == null) INSTANCE = new OreProcessorHandler();
        return INSTANCE;
    }

    private final Map<OrePrefix, Map<ResourceLocation, IOreProcessor>> registry = new Object2ObjectOpenHashMap<>();

    private final ThreadLocal<ResourceLocation> currentProcessingHandler = new ThreadLocal<>();
    private final ThreadLocal<OrePrefix> currentProcessingPrefix = new ThreadLocal<>();
    private final ThreadLocal<Material> currentMaterial = new ThreadLocal<>();

    private Phase phase = Phase.REGISTRATION;

    private OreProcessorHandler() {}

    @Override
    public void registerHandler(@Nonnull OrePrefix prefix, @Nonnull ResourceLocation name, @Nonnull IOreProcessor handler) {
        if (phase != Phase.REGISTRATION) {
            throw new UnsupportedOperationException("Cannot register handlers when in phase " + phase);
        }
        var map = registry.computeIfAbsent(prefix, v -> new Object2ObjectOpenHashMap<>());
        if (map.containsKey(name)) {
            throw new IllegalArgumentException("Processor " + name + " is already registered.");
        }
        map.put(name, handler);
    }

    @Override
    public <T extends IMaterialProperty> void registerHandler(@Nonnull OrePrefix prefix, @Nonnull ResourceLocation name,
                                                              @Nonnull PropertyKey<T> propertyKey, @Nonnull TriConsumer<OrePrefix, Material, T> handler) {
        registerHandler(prefix, name, (orePrefix, material) -> {
            if (material.hasProperty(propertyKey) && !material.hasFlag(NO_UNIFICATION)) {
                handler.accept(orePrefix, material, material.getProperty(propertyKey));
            }
        });
    }

    @Override
    public boolean removeHandler(@Nonnull OrePrefix prefix, @Nonnull ResourceLocation name) {
        if (phase != Phase.REMOVAL) {
            throw new UnsupportedOperationException("Cannot remove handlers when in phase " + phase);
        }
        var map = registry.get(prefix);
        return map != null && map.remove(name) != null;
    }

    @Nonnull
    @Override
    public @UnmodifiableView Collection<ResourceLocation> getRegisteredHandlerNames(@Nonnull OrePrefix prefix) {
        if (phase == Phase.REGISTRATION) {
            throw new UnsupportedOperationException("Cannot get registered handlers when in phase " + phase);
        }
        var map = registry.get(prefix);
        if (map == null) return Collections.emptyList();
        return Collections.unmodifiableCollection(map.keySet());
    }

    @Nullable
    @Override
    public ResourceLocation getCurrentProcessingHandler() {
        return currentProcessingHandler.get();
    }

    @Nullable
    @Override
    public OrePrefix getCurrentProcessingPrefix() {
        return currentProcessingPrefix.get();
    }

    @Nullable
    @Override
    public Material getCurrentMaterial() {
        return currentMaterial.get();
    }

    @Nonnull
    @Override
    public Phase getPhase() {
        return this.phase;
    }

    @ApiStatus.Internal
    public void startRegistration() {
        MinecraftForge.EVENT_BUS.post(new OreProcessorEvent(phase));
    }

    @ApiStatus.Internal
    public void startRemoval() {
        this.phase = Phase.REMOVAL;
        MinecraftForge.EVENT_BUS.post(new OreProcessorEvent(phase));
    }

    @ApiStatus.Internal
    public void runGeneratedMaterialHandlers(@Nonnull OrePrefix prefix, boolean isLate) {
        this.phase = isLate ? Phase.PROCESSING_POST : Phase.PROCESSING;

        var map = registry.get(prefix);
        if (map != null) {
            currentProcessingPrefix.set(prefix);
            for (Material registeredMaterial : prefix.getGeneratedMaterials()) {
                currentMaterial.set(registeredMaterial);
                for (var entry : map.entrySet()) {
                    currentProcessingHandler.set(entry.getKey());
                    entry.getValue().processMaterial(prefix, registeredMaterial);
                    currentProcessingHandler.set(null);
                }
            }
            currentMaterial.set(null);
        }

        // clear generated materials for next pass
        prefix.clearGeneratedMaterials();
        currentProcessingPrefix.set(null);
    }
}
