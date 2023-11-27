package gregtech.api.modules;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.*;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * All modules must implement this interface.
 * <p>
 * Provides methods for responding to FML lifecycle events, adding event bus subscriber classes, and processing IMC
 * messages.
 */
public interface IGregTechModule {

    /**
     * What other modules this module depends on.
     * <p>
     * e.g. <code>new ResourceLocation("gregtech", "foo_module")</code> represents a dependency on the module
     * "foo_module" in the container "gregtech"
     */
    @NotNull
    default Set<ResourceLocation> getDependencyUids() {
        return Collections.emptySet();
    }

    default void construction(FMLConstructionEvent event) {}

    default void preInit(FMLPreInitializationEvent event) {}

    default void init(FMLInitializationEvent event) {}

    default void postInit(FMLPostInitializationEvent event) {}

    default void loadComplete(FMLLoadCompleteEvent event) {}

    default void serverAboutToStart(FMLServerAboutToStartEvent event) {}

    default void serverStarting(FMLServerStartingEvent event) {}

    default void serverStarted(FMLServerStartedEvent event) {}

    default void serverStopping(FMLServerStoppingEvent event) {}

    default void serverStopped(FMLServerStoppedEvent event) {}

    /**
     * Register packets using GregTech's packet handling API here.
     */
    default void registerPackets() {}

    /**
     * @return A list of classes to subscribe to the Forge event bus.
     *         As the class gets subscribed, not any specific instance, event handlers must be static!
     */
    @NotNull
    default List<Class<?>> getEventBusSubscribers() {
        return Collections.emptyList();
    }

    default boolean processIMC(FMLInterModComms.IMCMessage message) {
        return false;
    }

    /**
     * @return A logger to use for this module.
     */
    @NotNull
    Logger getLogger();
}
