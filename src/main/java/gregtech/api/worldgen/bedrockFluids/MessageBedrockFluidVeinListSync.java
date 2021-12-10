package gregtech.api.worldgen.bedrockFluids;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class MessageBedrockFluidVeinListSync implements IMessage {
    Map<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> map = new HashMap<>();

    public MessageBedrockFluidVeinListSync(HashMap<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> map) {
        this.map = map;
    }

    public MessageBedrockFluidVeinListSync() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            if (tag == null || tag.isEmpty())
                continue;

            BedrockFluidVeinHandler.FluidVeinWorldEntry entry = BedrockFluidVeinHandler.FluidVeinWorldEntry.readFromNBT(tag);
            map.put(entry, tag.getInteger("weight"));
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(map.size());
        for (Map.Entry<BedrockFluidVeinHandler.FluidVeinWorldEntry, Integer> entry : map.entrySet()) {
            NBTTagCompound tag = entry.getKey().writeToNBT();
            tag.setInteger("weight", entry.getValue());
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    public static class Handler implements IMessageHandler<MessageBedrockFluidVeinListSync, IMessage> {
        @Override
        public IMessage onMessage(MessageBedrockFluidVeinListSync message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> onMessageMain(message));
            return null;
        }

        private void onMessageMain(MessageBedrockFluidVeinListSync message) {
            BedrockFluidVeinHandler.veinList.clear();
            for (BedrockFluidVeinHandler.FluidVeinWorldEntry min : message.map.keySet()) {
                BedrockFluidVeinHandler.veinList.put(min.getVein(), message.map.get(min));
            }
        }
    }
}
