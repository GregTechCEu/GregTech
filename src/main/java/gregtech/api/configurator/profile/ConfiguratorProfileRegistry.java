package gregtech.api.configurator.profile;

import gregtech.api.configurator.playerdata.PlayerConfiguratorData;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConfiguratorProfileRegistry {

    private static final Map<String, IMachineConfiguratorProfile> configuratorProfiles = new Object2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<IMachineConfiguratorProfile> profileByNetworkID = new Int2ObjectArrayMap<>();
    private static int rollingNetworkID = 0;

    public static void registerMachineConfiguratorProfile(IMachineConfiguratorProfile profile) {
        if (configuratorProfiles.containsKey(profile.getName())) {
            throw new IllegalStateException(
                    String.format("A machine configurator profile with the id %s already exists!", profile.getName()));
        }

        configuratorProfiles.put(profile.getName(), profile);
        profile.setNetworkID(rollingNetworkID++);
        profileByNetworkID.put(profile.networkID(), profile);
    }

    @Nullable
    public static IMachineConfiguratorProfile getConfiguratorProfileByName(String name) {
        return name.equals(PlayerConfiguratorData.NO_PROFILE_NBT_KEY) ? null : configuratorProfiles.get(name);
    }

    @Nullable
    public static IMachineConfiguratorProfile getConfiguratorProfileByNetworkID(int id) {
        return profileByNetworkID.get(id);
    }

    @NotNull
    public static Collection<IMachineConfiguratorProfile> getConfiguratorProfiles() {
        return Collections.unmodifiableCollection(configuratorProfiles.values());
    }
}
