package gregtech.core.network.packets;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class PacketFluidVeinList implements IPacket, IClientExecutor {

    private Map<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> map;

    @SuppressWarnings("unused")
    public PacketFluidVeinList() {}

    public PacketFluidVeinList(HashMap<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> map) {
        this.map = map;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(map.size());
        for (Map.Entry<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> entry : map.entrySet()) {
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

            BedrockFluidVeinHandler.FluidVeinWorldEntry entry = BedrockFluidVeinHandler.FluidVeinWorldEntry
                    .readFromNBT(tag);
            this.map.put(entry, tag.getInteger("weight"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        BedrockFluidVeinHandler.veinList.clear();
        for (Map.Entry<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> entry : map.entrySet()) {
            BedrockFluidVeinHandler.veinList.put(entry.getKey().getDefinition(), entry.getValue());
        }
    }
}
