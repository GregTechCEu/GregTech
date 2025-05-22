package gtqt.api.util.wireless.packages;

import gtqt.api.util.wireless.NetworkInfo;
import gtqt.api.util.wireless.NetworkInfoManager;
import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketRequestJoinNetwork implements IMessage {
    private UUID ownerid;  // 玩家自己ID，用于确认身份
    private String networkName;
    private String password;

    public PacketRequestJoinNetwork() {}
    public PacketRequestJoinNetwork(UUID ownerid, String networkName, String password) {
        this.ownerid = ownerid;
        this.networkName = networkName;
        this.password = password;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(ownerid.getMostSignificantBits());
        buf.writeLong(ownerid.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, networkName);
        ByteBufUtils.writeUTF8String(buf, password);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long most = buf.readLong();
        long least = buf.readLong();
        ownerid = new UUID(most, least);
        networkName = ByteBufUtils.readUTF8String(buf);
        password = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketRequestJoinNetwork, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestJoinNetwork message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                NetworkInfoManager manager = NetworkInfoManager.get(ctx.getServerHandler().player.getServerWorld());
                for (NetworkInfo info : manager.getAllNetworks()) {
                    if (info.NetworkName.equals(message.networkName)) {
                        if (!info.ownerid.equals(message.ownerid)) {
                            return;
                        }
                    }
                }
                NetworkInfo target = manager.getNetworkInfo(message.ownerid);
                if (target == null) {
                    target = new NetworkInfo(message.ownerid);
                    target.NetworkName = message.networkName;
                    target.NetworkPassword = message.password;
                    target.isGloba = false;
                    manager.addOrUpdateNetwork(target);
                } else {
                    if (!target.NetworkPassword.equals(message.password)) {
                        return;
                    }
                }
            });
            return null;
        }
    }
}
