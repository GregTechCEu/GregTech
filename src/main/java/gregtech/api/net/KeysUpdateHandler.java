package gregtech.api.net;


import gregtech.api.util.input.Key;
import gregtech.api.util.input.Keybinds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class KeysUpdateHandler implements IMessageHandler<KeysPacket, IMessage> {

    @Override
    public IMessage onMessage(KeysPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        List<Key> keys = message.getList();
        if (keys.isEmpty()) return null;
        if (Keybinds.PLAYER_KEYS.containsKey(player)) {
            Keybinds.PLAYER_KEYS.replace(player, keys);
        } else {
            Keybinds.PLAYER_KEYS.put(player, keys);
        }
        return null;
    }
}
