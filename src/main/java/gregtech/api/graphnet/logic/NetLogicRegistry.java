package gregtech.api.graphnet.logic;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class NetLogicRegistry {

    private static final Int2ObjectArrayMap<NetLogicType<?>> REGISTRY;

    private static final Object2IntOpenHashMap<String> NAMES_TO_NETWORK_IDS;

    static {
        NetLogicRegistrationEvent event = new NetLogicRegistrationEvent();
        MinecraftForge.EVENT_BUS.post(event);
        Set<NetLogicType<?>> gather = event.getGather();
        NAMES_TO_NETWORK_IDS = new Object2IntOpenHashMap<>(gather.size());
        REGISTRY = new Int2ObjectArrayMap<>(gather.size());
        int id = 1;
        for (NetLogicType<?> type : gather) {
            NAMES_TO_NETWORK_IDS.put(type.getName(), id);
            REGISTRY.put(id, type);
            id++;
        }
    }

    public static String getName(int networkID) {
        return REGISTRY.get(networkID).getName();
    }

    public static int getNetworkID(@NotNull String name) {
        int id = NAMES_TO_NETWORK_IDS.getInt(name);
        if (id == -1) throwUnregisteredError(name);
        return id;
    }

    public static int getNetworkID(@NotNull NetLogicType<?> type) {
        return getNetworkID(type.getName());
    }

    public static int getNetworkID(@NotNull NetLogicEntry<?, ?> entry) {
        return getNetworkID(entry.getType());
    }

    public static @Nullable NetLogicType<?> getTypeNullable(int networkID) {
        return REGISTRY.get(networkID);
    }

    public static @Nullable NetLogicType<?> getTypeNullable(@NotNull String name) {
        return getTypeNullable(getNetworkID(name));
    }

    public static @NotNull NetLogicType<?> getType(int networkID) {
        NetLogicType<?> type = REGISTRY.get(networkID);
        if (type == null) throwNonexistenceError();
        assert type != null;
        return type;
    }

    public static @NotNull NetLogicType<?> getType(@NotNull String name) {
        return getType(getNetworkID(name));
    }

    public static void throwNonexistenceError() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) disconnect();
        throw new RuntimeException("Could not find the type of an encoded NetLogicEntry. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    public static void throwDecodingError() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) disconnect();
        throw new RuntimeException("Failed to decode an encoded NetLogicEntry. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    public static void throwUnregisteredError(String n) {
        throw new RuntimeException("Could not determine the network ID of a to-encode NetLogicEntry. " +
                "This suggests that the NetLogicEntry does not have a registered type. The offending name is: " + n);
    }

    private static void disconnect() {
        if (Minecraft.getMinecraft().getConnection() != null)
            Minecraft.getMinecraft().getConnection()
                    .onDisconnect(new TextComponentTranslation("gregtech.universal.netlogicdisconnect"));
    }

    private NetLogicRegistry() {}
}
