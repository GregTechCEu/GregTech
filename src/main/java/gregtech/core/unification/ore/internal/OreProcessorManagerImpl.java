package gregtech.core.unification.ore.internal;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.handler.OreProcessor;
import gregtech.api.unification.ore.handler.OreProcessorEvent;
import gregtech.api.unification.ore.handler.OreProcessorManager;
import gregtech.api.util.GTLog;
import gregtech.api.util.function.TriConsumer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static gregtech.api.unification.material.info.MaterialFlags.NO_UNIFICATION;

public final class OreProcessorManagerImpl implements OreProcessorManager {

    private static OreProcessorManagerImpl INSTANCE;

    public static OreProcessorManagerImpl getInstance() {
        if (INSTANCE == null) INSTANCE = new OreProcessorManagerImpl();
        return INSTANCE;
    }

    private final Map<OrePrefix, Map<ResourceLocation, OreProcessor>> registry = new Object2ObjectOpenHashMap<>();

    private @Nullable ResourceLocation currentProcessingHandler;
    private @Nullable OrePrefix currentProcessingPrefix;
    private @Nullable Material currentMaterial;

    private Phase phase = Phase.REGISTRATION;

    private OreProcessorManagerImpl() {}

    @Override
    public void registerProcessor(@NotNull OrePrefix prefix, @NotNull ResourceLocation name,
                                  @NotNull OreProcessor handler) {
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
    public <T extends IMaterialProperty> void registerProcessor(@NotNull OrePrefix prefix,
                                                                @NotNull ResourceLocation name,
                                                                @NotNull PropertyKey<T> propertyKey,
                                                                @NotNull TriConsumer<OrePrefix, Material, T> handler) {
        registerProcessor(prefix, name, (orePrefix, material) -> {
            if (material.hasProperty(propertyKey) && !material.hasFlag(NO_UNIFICATION)) {
                handler.accept(orePrefix, material, material.getProperty(propertyKey));
            }
        });
    }

    @Override
    public boolean removeHandler(@NotNull OrePrefix prefix, @NotNull ResourceLocation name) {
        if (phase != Phase.REMOVAL) {
            throw new UnsupportedOperationException("Cannot remove handlers when in phase " + phase);
        }
        var map = registry.get(prefix);
        return map != null && map.remove(name) != null;
    }

    @Override
    public @UnmodifiableView @NotNull Collection<@NotNull ResourceLocation> getRegisteredHandlerNames(@NotNull OrePrefix prefix) {
        if (phase == Phase.REGISTRATION) {
            throw new UnsupportedOperationException("Cannot get registered handlers when in phase " + phase);
        }
        var map = registry.get(prefix);
        if (map == null) return Collections.emptyList();
        return Collections.unmodifiableCollection(map.keySet());
    }

    @Override
    public @NotNull Phase getPhase() {
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
    public void runGeneratedMaterialHandlers(@NotNull OrePrefix prefix, boolean isLate) {
        this.phase = isLate ? Phase.PROCESSING_POST : Phase.PROCESSING;

        var map = registry.get(prefix);
        if (map != null) {
            currentProcessingPrefix = prefix;
            for (Material registeredMaterial : prefix.getGeneratedMaterials()) {
                currentMaterial = registeredMaterial;
                for (var entry : map.entrySet()) {
                    currentProcessingHandler = entry.getKey();
                    entry.getValue().processMaterial(prefix, registeredMaterial);
                    currentProcessingHandler = null;
                }
            }
            currentMaterial = null;
        }

        // clear generated materials for next pass
        prefix.clearGeneratedMaterials();
        currentProcessingPrefix = null;
    }

    /**
     * Notify that an invalid recipe was found
     */
    @ApiStatus.Internal
    public void notifyInvalidRecipe() {
        if (currentProcessingPrefix != null) {
            GTLog.logger.error(
                    "Error occurred during oredict processing of prefix {} and material {} using handler {}. Likely a cross-mod compatibility issue. Report to GTCEu github.",
                    currentProcessingPrefix, currentMaterial, currentProcessingHandler);
        }
    }
}
