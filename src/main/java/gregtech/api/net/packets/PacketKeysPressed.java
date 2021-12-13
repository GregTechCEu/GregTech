package gregtech.api.net.packets;

import gregtech.api.net.IPacket;
import gregtech.api.util.input.EnumKey;
import gregtech.api.util.input.Key;
import gregtech.api.util.input.KeyBinds;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class PacketKeysPressed implements IPacket {

    private List<Key> playerKeys;

    public PacketKeysPressed(List<Key> playerKeys) {
        this.playerKeys = playerKeys;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(playerKeys.size());
        for (Key key : playerKeys) {
            buf.writeEnumValue(key.KEY);
            buf.writeBoolean(key.state);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.playerKeys = new ArrayList<>();
        int totalKeys = buf.readVarInt();
        for (int i = 0; i < totalKeys; i++) {
            this.playerKeys.add(new Key(buf.readEnumValue(EnumKey.class), buf.readBoolean()));
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.player;
        if (playerKeys != null && !playerKeys.isEmpty()) {
            KeyBinds.PLAYER_KEYS.put(player, playerKeys);
        }
    }
}
