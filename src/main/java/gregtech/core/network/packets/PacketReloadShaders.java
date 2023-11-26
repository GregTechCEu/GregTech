package gregtech.core.network.packets;

import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.client.shader.Shaders;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

public class PacketReloadShaders implements IPacket, IClientExecutor {

    @SuppressWarnings("unused")
    public PacketReloadShaders() {}

    @Override
    public void encode(PacketBuffer buf) {}

    @Override
    public void decode(PacketBuffer buf) {}

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        if (Shaders.allowedShader()) {
            Shaders.initShaders();
            ShaderTexture.clear();
        }
    }
}
