package gregtech.api.net.packets;

import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.net.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NoArgsConstructor
public class PacketUIWidgetUpdate implements IPacket {

    public int windowId;
    public int widgetId;
    public PacketBuffer updateData;

    public PacketUIWidgetUpdate(int windowId, int widgetId, PacketBuffer updateData) {
        this.windowId = windowId;
        this.widgetId = widgetId;
        this.updateData = updateData;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(updateData.readableBytes());
        buf.writeBytes(updateData);
        buf.writeVarInt(windowId);
        buf.writeVarInt(widgetId);
    }

    @Override
    public void decode(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();

        this.windowId = buf.readVarInt();
        this.widgetId = buf.readVarInt();
        this.updateData = new PacketBuffer(copiedDataBuffer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof ModularUIGui) {
            ((ModularUIGui) currentScreen).handleWidgetUpdate(this);
        }
    }
}
