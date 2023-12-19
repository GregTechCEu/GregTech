package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.api.util.input.KeyBind;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class PacketKeysPressed implements IPacket, IServerExecutor {

    private Object updateKeys;

    @SuppressWarnings("unused")
    public PacketKeysPressed() {}

    public PacketKeysPressed(List<KeyBind> updateKeys) {
        this.updateKeys = updateKeys;
    }

    @Override
    public void encode(PacketBuffer buf) {
        List<KeyBind> updateKeys = (List<KeyBind>) this.updateKeys;
        buf.writeVarInt(updateKeys.size());
        for (KeyBind keyBind : updateKeys) {
            buf.writeVarInt(keyBind.ordinal());
            buf.writeBoolean(keyBind.isPressed());
            buf.writeBoolean(keyBind.isKeyDown());
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.updateKeys = new Pair[KeyBind.VALUES.length];
        Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) this.updateKeys;
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys[buf.readVarInt()] = Pair.of(buf.readBoolean(), buf.readBoolean());
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        KeyBind[] keybinds = KeyBind.VALUES;
        Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) this.updateKeys;
        for (int i = 0; i < updateKeys.length; i++) {
            Pair<Boolean, Boolean> pair = updateKeys[i];
            if (pair != null) {
                keybinds[i].update(pair.getLeft(), pair.getRight(), handler.player);
            }
        }
    }
}
