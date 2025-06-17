package gregtech.api.configurator.profile;

import gregtech.api.configurator.playerdata.PlayerConfiguratorData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConfiguratorProfileRegistry {

    private static final Map<String, IMachineConfiguratorProfile> MACHINE_CONFIGURATOR_PROFILES = new Object2ObjectOpenHashMap<>();

    public static void registerMachineConfiguratorProfile(IMachineConfiguratorProfile profile) {
        if (MACHINE_CONFIGURATOR_PROFILES.containsKey(profile.getName())) {
            throw new IllegalStateException(
                    String.format("A machine configurator profile with the id %s already exists!", profile.getName()));
        }

        MACHINE_CONFIGURATOR_PROFILES.put(profile.getName(), profile);
    }

    @Nullable
    public static IMachineConfiguratorProfile getMachineConfiguratorProfile(String name) {
        return name.equals(PlayerConfiguratorData.NO_PROFILE_NBT_KEY) ? null : MACHINE_CONFIGURATOR_PROFILES.get(name);
    }

    @NotNull
    public static Collection<IMachineConfiguratorProfile> getMachineConfiguratorProfiles() {
        return Collections.unmodifiableCollection(MACHINE_CONFIGURATOR_PROFILES.values());
    }
}
