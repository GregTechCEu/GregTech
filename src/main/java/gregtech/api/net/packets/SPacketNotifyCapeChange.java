package gregtech.api.net.packets;

import gregtech.api.net.IPacket;
import gregtech.api.util.CapesRegistry;
import lombok.NoArgsConstructor;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

@NoArgsConstructor
public class SPacketNotifyCapeChange implements IPacket {

    public ResourceLocation cape;
    public UUID uuid;

    public SPacketNotifyCapeChange(UUID uuid, ResourceLocation cape) {
        this.uuid = uuid;
        this.cape = cape;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(this.uuid);
        buf.writeBoolean(this.cape != null);
        if (this.cape != null) {
            buf.writeResourceLocation(this.cape);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.uuid = buf.readUniqueId();
        this.cape = buf.readBoolean() ? buf.readResourceLocation() : null;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        CapesRegistry.giveRawCape(uuid, cape);
    }

}
