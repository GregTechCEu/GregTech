package gregtech.api.net;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * The general structure of Network Packets. <br><br>
 * <p>
 * To implement a new packet, implement both {@link IPacket#encode(PacketBuffer)} and
 * {@link IPacket#decode(PacketBuffer)}, and register the packet in {@link NetworkHandler#init()} method.<br><br>
 * <p>
 * Additionally, do one of the following:<p>
 *     - If this Packet is to be received on the SERVER, implement {@link IServerExecutor#executeServer(NetHandlerPlayServer)}.
 * <p>
 *     - If this Packet is to be received on the CLIENT, implement {@link IClientExecutor#executeClient(NetHandlerPlayClient)}.<br><br>
 * <p>
 * Lastly, add the {@link lombok.NoArgsConstructor} annotation to your Packet class.
 */
public interface IPacket {

    /**
     * Used to write data from a Packet into a PacketBuffer.<br><br>
     * <p>
     * This is the first step in sending a Packet to a different thread,
     * and is done on the "sending" side.
     *
     * @param buf The PacketBuffer to write Packet data to.
     */
    void encode(PacketBuffer buf);

    /**
     * Used to read data from a PacketBuffer into this Packet.<br><br>
     * <p>
     * This is the next step of sending a Packet to a different thread,
     * and is done on the "receiving" side.
     *
     * @param buf The PacketBuffer to read Packet data from.
     */
    void decode(PacketBuffer buf);

    /**
     * Convenience method that redirects to {@link NetworkUtils#packet2proxy(IPacket)}. Converts an instance of this
     * class to a Packet that Forge can understand.<br><br>
     *
     * @return An FMLProxyPacket representation of this Packet.
     */
    default FMLProxyPacket toFMLPacket() {
        return NetworkUtils.packet2proxy(this);
    }
}
