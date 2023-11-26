package gregtech.core.network.packets;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.api.util.ClipboardUtil;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketClipboard implements IPacket, IClientExecutor {

    private String text;

    @SuppressWarnings("unused")
    public PacketClipboard() {}

    public PacketClipboard(final String text) {
        this.text = text;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(text);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.text = buf.readString(32767);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        ClipboardUtil.copyToClipboard(text);
    }
}
