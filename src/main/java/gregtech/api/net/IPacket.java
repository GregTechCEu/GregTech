package gregtech.api.net;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The general structure of Network Packets. <br><br>
 * <p>
 * To implement a new packet, implement both {@link IPacket#encode(PacketBuffer)} and
 * {@link IPacket#decode(PacketBuffer)}, and register the packet in {@link NetworkHandler#init()} method.<br><br>
 * <p>
 * Additionally, do one of the following:<p>
 *     - If this Packet is to be received on the SERVER, implement {@link IPacket#executeServer(NetHandlerPlayServer)}
 *       and register in {@link NetworkHandler#initServer()}.
 * <p>
 *     - If this Packet is to be received on the CLIENT, implement {@link IPacket#executeClient(NetHandlerPlayClient)}
 *       and register in {@link NetworkHandler#initClient()}.<br><br>
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
     * Used to execute code on the client, after receiving a packet from the server.<br><br>
     * <p>
     * CANNOT be implemented with {@link IPacket#executeServer(NetHandlerPlayServer)}, only one at a time is supported.
     *
     * @param handler Network handler that contains useful data and helpers.
     */
    @SideOnly(Side.CLIENT)
    default void executeClient(NetHandlerPlayClient handler) {
    }

    /**
     * Used to execute code on the server, after receiving a packet from the client.<br><br>
     * <p>
     * CANNOT be implemented with {@link IPacket#executeClient(NetHandlerPlayClient)}, only one at a time is supported.
     *
     * @param handler Network handler that contains useful data and helpers.
     */
    default void executeServer(NetHandlerPlayServer handler) {
    }

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
