package gregtech.api.net;


import gregtech.api.util.input.EnumKey;
import gregtech.api.util.input.Key;
import gregtech.api.util.input.Keybinds;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class KeysPacket implements IMessage {
    private List<Key> keys;

    public KeysPacket() {
    }

    public KeysPacket(List<Key> keys) {
        this.keys = keys;
    }

    public List<Key> getList() {
        return this.keys;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (int i = 0; i < keys.size(); i++) {
            buf.writeByte(keys.get(i).KEY.getID());
            buf.writeBoolean(keys.get(i).state);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        keys = new ArrayList<>();
        for (int i = 0; i < Keybinds.REGISTERY.size(); i++) {
            int id = buf.readByte();
            Key current = new Key(EnumKey.getKeyByID(id));
            current.state = buf.readBoolean();
            keys.add(current);
        }
    }
}
