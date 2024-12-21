package gregtech.api.graphnet;

import gregtech.api.graphnet.logic.NetLogicEntry;
import gregtech.api.graphnet.logic.NetLogicType;

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

public final class GraphClassRegistry {

    private static final Int2ObjectArrayMap<GraphClassType<?>> REGISTRY;

    private static final Object2IntOpenHashMap<String> NAMES_TO_NETWORK_IDS;

    static {
        GraphClassRegistrationEvent event = new GraphClassRegistrationEvent();
        MinecraftForge.EVENT_BUS.post(event);
        Set<GraphClassType<?>> gather = event.getGather();
        NAMES_TO_NETWORK_IDS = new Object2IntOpenHashMap<>(gather.size());
        REGISTRY = new Int2ObjectArrayMap<>(gather.size());
        int id = 1;
        for (GraphClassType<?> type : gather) {
            NAMES_TO_NETWORK_IDS.put(type.getName(), id);
            REGISTRY.put(id, type);
            id++;
        }
    }

    public static String getName(int networkID) {
        return REGISTRY.get(networkID).getName();
    }

    public static int getNetworkID(@NotNull String name) {
        return NAMES_TO_NETWORK_IDS.getInt(name);
    }

    public static int getNetworkID(@NotNull NetLogicType<?> type) {
        return getNetworkID(type.getName());
    }

    public static int getNetworkID(@NotNull NetLogicEntry<?, ?> entry) {
        return getNetworkID(entry.getType());
    }

    public static @Nullable GraphClassType<?> getTypeNullable(int networkID) {
        return REGISTRY.get(networkID);
    }

    public static @Nullable GraphClassType<?> getTypeNullable(@NotNull String name) {
        return getTypeNullable(getNetworkID(name));
    }

    public static @NotNull GraphClassType<?> getType(int networkID) {
        GraphClassType<?> type = REGISTRY.get(networkID);
        if (type == null) throwNonexistenceError();
        assert type != null;
        return type;
    }

    public static @NotNull GraphClassType<?> getType(@NotNull String name) {
        return getType(getNetworkID(name));
    }

    public static void throwNonexistenceError() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) disconnect();
        throw new RuntimeException("Could not find the type of an encoded Graph Class. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    public static void throwDecodingError() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) disconnect();
        throw new RuntimeException("Failed to decode an encoded Graph Class. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    private static void disconnect() {
        if (Minecraft.getMinecraft().getConnection() != null)
            Minecraft.getMinecraft().getConnection()
                    .onDisconnect(new TextComponentTranslation("gregtech.universal.netlogicdisconnect"));
    }

    private GraphClassRegistry() {}
}
