package gregtech.core.network.packets;

import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.core.network.NetworkUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketUIWidgetUpdate implements IPacket, IClientExecutor {

    public int windowId;
    public int widgetId;
    public PacketBuffer updateData;

    @SuppressWarnings("unused")
    public PacketUIWidgetUpdate() {}

    public PacketUIWidgetUpdate(int windowId, int widgetId, PacketBuffer updateData) {
        this.windowId = windowId;
        this.widgetId = widgetId;
        this.updateData = updateData;
    }

    @Override
    public void encode(PacketBuffer buf) {
        NetworkUtils.writePacketBuffer(buf, updateData);
        buf.writeVarInt(windowId);
        buf.writeVarInt(widgetId);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.updateData = NetworkUtils.readPacketBuffer(buf);
        this.windowId = buf.readVarInt();
        this.widgetId = buf.readVarInt();
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
