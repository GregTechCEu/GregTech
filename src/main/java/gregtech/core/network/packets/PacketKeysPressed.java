package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.api.util.input.KeyBind;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PacketKeysPressed implements IPacket, IServerExecutor {

    private List<KeyBind> clientKeysUpdated;
    private int[] serverKeysUpdated;
    private boolean[] serverKeysPressed;
    private boolean[] serverKeysDown;

    @SuppressWarnings("unused")
    public PacketKeysPressed() {}

    public PacketKeysPressed(@NotNull List<KeyBind> clientKeysUpdated) {
        this.clientKeysUpdated = clientKeysUpdated;
    }

    @Override
    public void encode(@NotNull PacketBuffer buf) {
        buf.writeVarInt(clientKeysUpdated.size());
        for (KeyBind keyBind : clientKeysUpdated) {
            buf.writeByte(keyBind.ordinal());
            buf.writeBoolean(keyBind.isPressed());
            buf.writeBoolean(keyBind.isKeyDown());
        }
    }

    @Override
    public void decode(@NotNull PacketBuffer buf) {
        final int size = buf.readVarInt();
        this.serverKeysUpdated = new int[size];
        this.serverKeysPressed = new boolean[size];
        this.serverKeysDown = new boolean[size];
        for (int i = 0; i < size; i++) {
            serverKeysUpdated[i] = buf.readByte();
            serverKeysPressed[i] = buf.readBoolean();
            serverKeysDown[i] = buf.readBoolean();
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        if (serverKeysUpdated == null) {
            throw new IllegalStateException("PacketKeysPressed called executeServer() before decode()");
        }

        for (int i = 0; i < serverKeysUpdated.length; i++) {
            KeyBind.VALUES[i].updateServerState(handler.player, serverKeysPressed[i], serverKeysDown[i]);
        }
    }
}
