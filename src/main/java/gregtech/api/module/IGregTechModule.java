package gregtech.api.module;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IGregTechModule {

    @Nonnull
    default Set<ResourceLocation> getDependencyUids() {
        return Collections.emptySet();
    }

    @Nonnull
    default Set<String> getModDependencyIDs() {
        return Collections.emptySet();
    }

    default void preInit(FMLPreInitializationEvent event) {
    }

    default void init(FMLInitializationEvent event) {
    }

    default void postInit(FMLPostInitializationEvent event) {
    }

    default void loadComplete(FMLLoadCompleteEvent event) {
    }

    default void serverStarting(FMLServerStartingEvent event) {
    }

    default void serverStarted(FMLServerStartedEvent event) {
    }

    default void serverStopped(FMLServerStoppedEvent event) {
    }

    default void registerPackets() {
    }

    @Nonnull
    default List<Class<?>> getEventBusSubscribers() {
        return Collections.emptyList();
    }

    default boolean processIMC(FMLInterModComms.IMCMessage message) {
        return false;
    }

    @Nonnull
    Logger getLogger();
}
