package gregtech.core.network.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;

import org.jetbrains.annotations.NotNull;

public class PacketPollution implements IPacket, IClientExecutor {

    private long pos;
    private int dimension;
    private int pollution;

    @SuppressWarnings("unused")
    public PacketPollution() {}

    public PacketPollution(int chunkX, int chunkZ, int dimension, int pollution) {
        this.pos = ChunkPos.asLong(chunkX, chunkZ);
        this.dimension = dimension;
        this.pollution = pollution;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        GregTechAPI.pollutionManager.processPollutionPacket(pos, dimension, pollution);
    }

    @Override
    public void encode(@NotNull PacketBuffer buf) {
        buf.writeLong(pos);
        buf.writeByte(dimension);
        buf.writeInt(pollution);
    }

    @Override
    public void decode(@NotNull PacketBuffer buf) {
        this.pos = buf.readLong();
        this.dimension = buf.readByte();
        this.pollution = buf.readInt();
    }
}
