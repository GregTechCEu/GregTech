package gtqt.api.util.wireless.packages;

import gtqt.api.util.wireless.NetworkInfo;
import gtqt.api.util.wireless.NetworkInfoManager;
import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketRequestChangePassword implements IMessage {
    private UUID ownerid;
    private UUID networkOwnerId;
    private String newPassword;

    public PacketRequestChangePassword() {}

    public PacketRequestChangePassword(UUID ownerid, UUID networkOwnerId, String newPassword) {
        this.ownerid = ownerid;
        this.networkOwnerId = networkOwnerId;
        this.newPassword = newPassword;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(ownerid.getMostSignificantBits());
        buf.writeLong(ownerid.getLeastSignificantBits());
        buf.writeLong(networkOwnerId.getMostSignificantBits());
        buf.writeLong(networkOwnerId.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, newPassword);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ownerid = new UUID(buf.readLong(), buf.readLong());
        networkOwnerId = new UUID(buf.readLong(), buf.readLong());
        newPassword = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketRequestChangePassword, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestChangePassword message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                NetworkInfoManager manager = NetworkInfoManager.get(ctx.getServerHandler().player.getServerWorld());
                NetworkInfo networkInfo = manager.getNetworkInfo(message.networkOwnerId);
                if (networkInfo != null) {
                    if (networkInfo.ownerid.equals(message.ownerid)) {
                        networkInfo.NetworkPassword = message.newPassword;
                        manager.addOrUpdateNetwork(networkInfo);
                        // addOrUpdateNetwork 会同步给所有客户端
                    } else {
                        // 权限不足，拒绝操作
                    }
                }
            });
            return null;
        }
    }
}

