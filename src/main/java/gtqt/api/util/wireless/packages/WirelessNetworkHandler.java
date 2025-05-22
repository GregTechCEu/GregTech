package gtqt.api.util.wireless.packages;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class WirelessNetworkHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("wireless_cnl");

    public static void registerMessages() {
        int id = 0;
        INSTANCE.registerMessage(PacketSyncNetworkInfo.Handler.class, PacketSyncNetworkInfo.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketRequestRemoveNetwork.Handler.class, PacketRequestRemoveNetwork.class, id++, Side.CLIENT);

        INSTANCE.registerMessage(PacketRequestJoinNetwork.Handler.class, PacketRequestJoinNetwork.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketRequestRemoveNetwork.Handler.class, PacketRequestRemoveNetwork.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketRequestChangePassword.Handler.class, PacketRequestChangePassword.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketRequestChangeNetworkName.Handler.class, PacketRequestChangeNetworkName.class, id++, Side.SERVER);
    }

    public static void sendToAll(IMessage msg) {
        INSTANCE.sendToAll(msg);
    }

    public static void sendToPlayer(IMessage msg, EntityPlayerMP player) {
        INSTANCE.sendTo(msg, player);
    }
}
