package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.api.util.input.KeyBind;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class PacketKeysDown implements IPacket, IServerExecutor {

    private Object updateKeys;

    @SuppressWarnings("unused")
    public PacketKeysDown() {}

    public PacketKeysDown(List<KeyBind> updateKeys) {
        this.updateKeys = updateKeys;
    }

    @Override
    public void encode(PacketBuffer buf) {
        List<KeyBind> updateKeys = (List<KeyBind>) this.updateKeys;
        buf.writeVarInt(updateKeys.size());
        for (KeyBind keyBind : updateKeys) {
            buf.writeVarInt(keyBind.ordinal());
            buf.writeBoolean(keyBind.isKeyDown());
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.updateKeys = new boolean[KeyBind.VALUES.length];
        boolean[] updateKeys = (boolean[]) this.updateKeys;
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys[buf.readVarInt()] = buf.readBoolean();
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        KeyBind[] keybinds = KeyBind.VALUES;
        boolean[] updateKeys = (boolean[]) this.updateKeys;
        for (int i = 0; i < updateKeys.length; i++) {
            keybinds[i].updateKeyDown(updateKeys[i], handler.player);
        }
    }
}
