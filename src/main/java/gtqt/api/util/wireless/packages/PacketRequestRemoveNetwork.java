package gtqt.api.util.wireless.packages;

import gtqt.api.util.wireless.ClientNetworkInfoManager;
import gtqt.api.util.wireless.NetworkInfo;
import gtqt.api.util.wireless.NetworkInfoManager;
import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketRequestRemoveNetwork implements IMessage {
    private UUID ownerid;
    private UUID networkOwnerId;

    public PacketRequestRemoveNetwork() {}

    public PacketRequestRemoveNetwork(UUID ownerid, UUID networkOwnerId) {
        this.ownerid = ownerid;
        this.networkOwnerId = networkOwnerId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(ownerid.getMostSignificantBits());
        buf.writeLong(ownerid.getLeastSignificantBits());
        buf.writeLong(networkOwnerId.getMostSignificantBits());
        buf.writeLong(networkOwnerId.getLeastSignificantBits());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ownerid = new UUID(buf.readLong(), buf.readLong());
        networkOwnerId = new UUID(buf.readLong(), buf.readLong());
    }

    public static class Handler implements IMessageHandler<PacketRequestRemoveNetwork, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestRemoveNetwork message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                NetworkInfoManager manager = NetworkInfoManager.get(ctx.getServerHandler().player.getServerWorld());

                NetworkInfo networkInfo = manager.getNetworkInfo(message.networkOwnerId);
                if (networkInfo != null) {
                    if (networkInfo.ownerid.equals(message.ownerid)) {
                        manager.removeNetwork(message.networkOwnerId);
                    } else {
                    }
                }
            });
            return null;
        }
    }
}

