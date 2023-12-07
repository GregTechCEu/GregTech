package gregtech.core.network.packets;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.api.util.CapesRegistry;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class PacketNotifyCapeChange implements IPacket, IClientExecutor {

    public ResourceLocation cape;
    public UUID uuid;

    @SuppressWarnings("unused")
    public PacketNotifyCapeChange() {}

    public PacketNotifyCapeChange(UUID uuid, ResourceLocation cape) {
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
