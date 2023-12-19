package gregtech.api.network;

import net.minecraft.network.PacketBuffer;

/**
 * The general structure of Network Packets. <br>
 * <br>
 * <p>
 * To implement a new packet, implement both {@link IPacket#encode(PacketBuffer)} and
 * {@link IPacket#decode(PacketBuffer)}, and register the packet to {@link gregtech.api.GregTechAPI#networkHandler}.<br>
 * <br>
 * <p>
 * Additionally, do one of the following:
 * <p>
 * - If this Packet is to be received on the SERVER, implement {@link IServerExecutor}.
 * <p>
 * - If this Packet is to be received on the CLIENT, implement {@link IClientExecutor}.<br>
 * <br>
 * <p>
 * Lastly, add a no-args constructor to your Packet class.
 */
public interface IPacket {

    /**
     * Used to write data from a Packet into a PacketBuffer.<br>
     * <br>
     * <p>
     * This is the first step in sending a Packet to a different thread,
     * and is done on the "sending" side.
     *
     * @param buf The PacketBuffer to write Packet data to.
     */
    void encode(PacketBuffer buf);

    /**
     * Used to read data from a PacketBuffer into this Packet.<br>
     * <br>
     * <p>
     * This is the next step of sending a Packet to a different thread,
     * and is done on the "receiving" side.
     *
     * @param buf The PacketBuffer to read Packet data from.
     */
    void decode(PacketBuffer buf);
}
