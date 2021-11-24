package gregtech.api.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class PacketClientCapeChange implements NetworkHandler.Packet {

    public final ResourceLocation cape;
    public final UUID uuid;

    public PacketClientCapeChange(UUID uuid, ResourceLocation cape) {
        this.uuid = uuid;
        this.cape = cape;
    }

}
