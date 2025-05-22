package gtqt.api.util.wireless.packages;

import gtqt.api.util.wireless.NetworkInfo;
import gtqt.api.util.wireless.NetworkInfoManager;
import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketRequestChangeNetworkName implements IMessage {
    private UUID ownerid;
    private String newNetworkName;

    public PacketRequestChangeNetworkName() {}

    public PacketRequestChangeNetworkName(UUID ownerid, String newNetworkName) {
        this.ownerid = ownerid;
        this.newNetworkName = newNetworkName;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(ownerid.getMostSignificantBits());
        buf.writeLong(ownerid.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, newNetworkName);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ownerid = new UUID(buf.readLong(), buf.readLong());
        newNetworkName = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketRequestChangeNetworkName, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestChangeNetworkName message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                NetworkInfoManager manager = NetworkInfoManager.get(ctx.getServerHandler().player.getServerWorld());
                NetworkInfo networkInfo = manager.getNetworkInfo(message.ownerid);
                if (networkInfo == null) return;
                for (NetworkInfo info : manager.getAllNetworks()) {
                    if (info.NetworkName.equals(message.newNetworkName) && !info.ownerid.equals(message.ownerid)) {
                        return;
                    }
                }
                networkInfo.NetworkName = message.newNetworkName;
                manager.addOrUpdateNetwork(networkInfo);
            });
            return null;
        }
    }
}
