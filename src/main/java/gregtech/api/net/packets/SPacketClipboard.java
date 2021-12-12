package gregtech.api.net.packets;

import gregtech.api.net.IPacket;
import gregtech.api.util.ClipboardUtil;
import lombok.NoArgsConstructor;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NoArgsConstructor
public class SPacketClipboard implements IPacket {

    private String text;

    public SPacketClipboard(final String text) {
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
