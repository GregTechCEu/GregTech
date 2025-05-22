package gtqt.api.util.wireless.packages;

import gtqt.api.util.wireless.ClientNetworkInfoManager;
import gtqt.api.util.wireless.NetworkInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PacketSyncNetworkInfo implements IMessage {

    private NBTTagCompound data; // 用NBT传网络数据

    public PacketSyncNetworkInfo() {}

    public PacketSyncNetworkInfo(NetworkInfo info) {
        this.data = info.writeToNBT();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(this.data, baos);
            byte[] bytes = baos.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int length = buf.readInt();
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            this.data = CompressedStreamTools.readCompressed(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<PacketSyncNetworkInfo, IMessage> {

        @Override
        public IMessage onMessage(PacketSyncNetworkInfo message, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                NetworkInfo info = new NetworkInfo();
                info.readFromNBT(message.data);
                ClientNetworkInfoManager.getInstance().addOrUpdateNetwork(info);
            });
            return null;
        }
    }
}
