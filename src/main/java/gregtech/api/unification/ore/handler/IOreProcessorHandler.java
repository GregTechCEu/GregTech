package gregtech.api.unification.ore.handler;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.function.TriConsumer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface IOreProcessorHandler {

    /**
     * Register a processor for an OrePrefix.
     *
     * @param prefix  the OrePrefix to associate with the processor
     * @param name    the name of the processor
     * @param handler the handler to register
     * @throws IllegalArgumentException if a handler with the same name has already been registered for the OrePrefix
     */
    void registerHandler(@Nonnull OrePrefix prefix, @Nonnull ResourceLocation name, @Nonnull IOreProcessor handler);

    /**
     * Register a processor for an OrePrefix.
     *
     * @param prefix      the OrePrefix to associate with the processor
     * @param name        the name of the processor
     * @param propertyKey the property key materials must have in order to be processed in the handler
     * @param handler     the handler to register
     * @throws IllegalArgumentException if a handler with the same name has already been registered for the OrePrefix
     */
    <T extends IMaterialProperty> void registerHandler(@Nonnull OrePrefix prefix, @Nonnull ResourceLocation name,
                                                       @Nonnull PropertyKey<T> propertyKey, @Nonnull TriConsumer<OrePrefix, Material, T> handler);

    /**
     * Remove an {@link IOreProcessor} for an OrePrefix.
     *
     * @param prefix the OrePrefix associated with the processor
     * @param name   the name of the handler
     * @return if removal was successful
     */
    @SuppressWarnings("unused")
    boolean removeHandler(@Nonnull OrePrefix prefix, @Nonnull ResourceLocation name);

    /**
     * @param prefix the prefix to get handlers for
     * @return all the registered handler names for the OrePrefix
     */
    @SuppressWarnings("unused")
    @Nonnull
    Collection<ResourceLocation> getRegisteredHandlerNames(@Nonnull OrePrefix prefix);

    /**
     * @return the name of the currently active {@link IOreProcessor}
     */
    @Nullable
    ResourceLocation getCurrentProcessingHandler();

    /**
     * @return the currently active {@link OrePrefix} being processed in an {@link IOreProcessor}
     */
    @Nullable
    OrePrefix getCurrentProcessingPrefix();

    /**
     * @return the currently active {@link Material} being processed in an {@link IOreProcessor}
     */
    @Nullable
    Material getCurrentMaterial();

    /**
     * @return the current phase of the handler
     */
    @Nonnull
    Phase getPhase();

    enum Phase {
        /** Phase when new handlers can be registered */
        REGISTRATION,
        /** Phase when handlers can be removed */
        REMOVAL,
        /** Phase when handlers are run */
        PROCESSING,
        /** Phase when handlers are run late for other mods like CraftTweaker */
        PROCESSING_POST
    }
}
