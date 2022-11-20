package gregtech.core.network.packets;

import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.client.shader.Shaders;
import lombok.NoArgsConstructor;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

@NoArgsConstructor
public class PacketReloadShaders implements IPacket, IClientExecutor {

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
