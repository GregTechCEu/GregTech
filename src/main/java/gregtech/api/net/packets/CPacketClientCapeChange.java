package gregtech.api.net.packets;

import gregtech.api.net.IPacket;
import gregtech.api.util.CapesRegistry;
import lombok.NoArgsConstructor;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

@NoArgsConstructor
public class CPacketClientCapeChange implements IPacket {

    public ResourceLocation cape;
    public UUID uuid;

    public CPacketClientCapeChange(UUID uuid, ResourceLocation cape) {
        this.uuid = uuid;
        this.cape = cape;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(this.uuid);
        buf.writeResourceLocation(this.cape);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.uuid = buf.readUniqueId();
        this.cape = buf.readResourceLocation();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        CapesRegistry.handleCapeChange(uuid, cape);
    }
}
