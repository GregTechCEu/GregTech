package gregtech.api.mui.sync;

import com.cleanroommc.modularui.value.sync.SyncHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.function.Consumer;

public final class MappedSyncHandler extends SyncHandler {

    private final Int2ObjectMap<Consumer<PacketBuffer>> clientHandlers = new Int2ObjectArrayMap<>();
    private final Int2ObjectMap<Consumer<PacketBuffer>> serverHandlers = new Int2ObjectArrayMap<>();

    public MappedSyncHandler addClientHandler(int id, Consumer<PacketBuffer> handler) {
        clientHandlers.put(id, handler);
        return this;
    }

    public MappedSyncHandler addServerHandler(int id, Consumer<PacketBuffer> handler) {
        serverHandlers.put(id, handler);
        return this;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        clientHandlers.get(id).accept(buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        serverHandlers.get(id).accept(buf);
    }
}
