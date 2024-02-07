package gregtech.core.network.packets;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;

import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class PacketOreVeinList implements IPacket, IClientExecutor {

    private Map<BedrockOreVeinHandler.OreVeinWorldEntry, Integer> map;

    @SuppressWarnings("unused")
    public PacketOreVeinList() {}

    public PacketOreVeinList(HashMap<BedrockOreVeinHandler.OreVeinWorldEntry, Integer> map) {
        this.map = map;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(map.size());
        for (Map.Entry<BedrockOreVeinHandler.OreVeinWorldEntry, Integer> entry : map.entrySet()) {
            NBTTagCompound tag = entry.getKey().writeToNBT();
            tag.setInteger("weight", entry.getValue());
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.map = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            if (tag == null || tag.isEmpty()) continue;

            BedrockOreVeinHandler.OreVeinWorldEntry entry = BedrockOreVeinHandler.OreVeinWorldEntry
                    .readFromNBT(tag);
            this.map.put(entry, tag.getInteger("weight"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        BedrockOreVeinHandler.veinList.clear();
        for (Map.Entry<BedrockOreVeinHandler.OreVeinWorldEntry, Integer> entry : map.entrySet()) {
            BedrockOreVeinHandler.veinList.put(entry.getKey().getDefinition(), entry.getValue());
        }
    }
}
