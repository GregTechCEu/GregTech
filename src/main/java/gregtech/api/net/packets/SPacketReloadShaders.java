package gregtech.api.net.packets;

import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.net.IPacket;
import gregtech.client.shader.Shaders;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

public class SPacketReloadShaders implements IPacket {

    @Override
    public void encode(PacketBuffer buf) {
    }

    @Override
    public void decode(PacketBuffer buf) {
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        if (Shaders.allowedShader()) {
            Shaders.initShaders();
            ShaderTexture.clear();
        }
    }
}
