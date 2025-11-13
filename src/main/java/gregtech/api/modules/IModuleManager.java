package gregtech.api.modules;

import gregtech.api.util.GTUtility;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModuleManager {

    default boolean isModuleEnabled(@NotNull String containerID, @NotNull String moduleID) {
        return isModuleEnabled(new ResourceLocation(containerID, moduleID));
    }

    default boolean isModuleEnabled(@NotNull String moduleID) {
        return isModuleEnabled(GTUtility.gregtechId(moduleID));
    }

    boolean isModuleEnabled(@NotNull ResourceLocation id);

    void registerContainer(@NotNull IModuleContainer container);

    @Nullable
    IModuleContainer getLoadedContainer();

    @NotNull
    ModuleStage getStage();

    boolean hasPassedStage(@NotNull ModuleStage stage);
}
