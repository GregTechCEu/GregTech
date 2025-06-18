package gregtech.api.configurator.profile;

import gregtech.api.configurator.playerdata.PlayerConfiguratorData;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

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

    public static @Nullable IMachineConfiguratorProfile getConfiguratorProfileByName(String name) {
        return name.equals(PlayerConfiguratorData.NO_PROFILE_NBT_KEY) ? null : configuratorProfiles.get(name);
    }

    public static @Nullable IMachineConfiguratorProfile getConfiguratorProfileByNetworkID(int id) {
        return profileByNetworkID.get(id);
    }

    public static @NotNull @UnmodifiableView Collection<IMachineConfiguratorProfile> getConfiguratorProfiles() {
        return Collections.unmodifiableCollection(configuratorProfiles.values());
    }
}
