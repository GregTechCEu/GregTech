package gregtech.api.net.packets;

import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class PacketProspecting {
    public int chunkX;
    public int chunkZ;
    public int posX;
    public int posZ;
    public int mode;
    public HashMap<Byte, String>[][] map;
    public Set<String> ores;

    public PacketProspecting(int chunkX, int chunkZ, int posX, int posZ, int mode) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.posX = posX;
        this.posZ = posZ;
        this.mode = mode;
        if (mode == 1)
            map = new HashMap[1][1];
        else
            map = new HashMap[16][16];

        ores = new HashSet<>();
    }

    public static PacketProspecting readPacketData(PacketBuffer buffer) {
        PacketProspecting packet = new PacketProspecting(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
        int aSize = 0;
        if (packet.mode == 0)
            aSize = 16;
        else if (packet.mode == 1)
            aSize = 1;
        int checkOut = 0;
        for (int i = 0; i < aSize; i++)
            for (int j = 0; j < aSize; j++) {
                byte kSize = buffer.readByte();
                if (kSize == 0) continue;
                packet.map[i][j] = new HashMap<>();
                for (int k = 0; k < kSize; k++) {
                    byte y = buffer.readByte();
                    String name = buffer.readString(1000);
                    packet.map[i][j].put(y, name);
                    if (packet.mode != 1 || y == 1)
                        packet.ores.add(name);
                    checkOut++;
                }
            }
        int checkOut2 = buffer.readInt();
        if (checkOut != checkOut2) {
            return null;
        }
        return packet;
    }


    public static PacketProspecting readPacketData(NBTTagCompound nbt) {
        if (nbt.hasKey("buffer")) {
            return PacketProspecting.readPacketData(new PacketBuffer(Unpooled.wrappedBuffer(nbt.getByteArray("buffer"))));
        }
        return null;
    }

    public NBTTagCompound writePacketData() {
        NBTTagCompound nbt = new NBTTagCompound();
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        writePacketData(buffer);
        byte[] bytes = buffer.array();
        nbt.setByteArray("buffer", bytes);
        return nbt;
    }

    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeInt(posX);
        buffer.writeInt(posZ);
        buffer.writeInt(mode);
        int aSize = 0;
        if (this.mode == 0)
            aSize = 16;
        else if (this.mode == 1)
            aSize = 1;
        int checkOut = 0;
        for (int i = 0; i < aSize; i++)
            for (int j = 0; j < aSize; j++) {
                if (map[i][j] == null)
                    buffer.writeByte(0);
                else {
                    buffer.writeByte(map[i][j].keySet().size());
                    for (byte key : map[i][j].keySet()) {
                        buffer.writeByte(key);
                        buffer.writeString(map[i][j].get(key));
                        checkOut++;
                    }
                }
            }
        buffer.writeInt(checkOut);
    }

    public void addBlock(int x, int y, int z, String orePrefix) {
        if (this.mode == 0) {
            if (map[x][z] == null)
                map[x][z] = new HashMap<>();
            map[x][z].put((byte) y, orePrefix);
            ores.add(orePrefix);
        } else if (this.mode == 1) {
            if (map[x][z] == null)
                map[x][z] = new HashMap<>();
            map[x][z].put((byte) y, orePrefix);
            if (y == 1) {
                ores.add(orePrefix);
            }
        }
    }

}
