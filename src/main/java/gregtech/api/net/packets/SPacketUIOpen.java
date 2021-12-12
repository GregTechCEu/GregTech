package gregtech.api.net.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.UIFactory;
import gregtech.api.net.IPacket;
import gregtech.api.net.NetworkUtils;
import gregtech.api.util.GTLog;
import lombok.NoArgsConstructor;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class SPacketUIOpen implements IPacket {

    private int uiFactoryId;
    private PacketBuffer serializedHolder;
    private int windowId;
    private List<SPacketUIWidgetUpdate> initialWidgetUpdates;

    public SPacketUIOpen(int uiFactoryId, PacketBuffer serializedHolder, int windowId, List<SPacketUIWidgetUpdate> initialWidgetUpdates) {
        this.uiFactoryId = uiFactoryId;
        this.serializedHolder = serializedHolder;
        this.windowId = windowId;
        this.initialWidgetUpdates = initialWidgetUpdates;
    }

    @Override
    public void encode(PacketBuffer buf) {
        NetworkUtils.writePacketBuffer(buf, serializedHolder);
        buf.writeVarInt(uiFactoryId);
        buf.writeVarInt(windowId);
        buf.writeVarInt(initialWidgetUpdates.size());
        for (SPacketUIWidgetUpdate packet : initialWidgetUpdates) {
            packet.encode(buf);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.serializedHolder = NetworkUtils.readPacketBuffer(buf);
        this.uiFactoryId = buf.readVarInt();
        this.windowId = buf.readVarInt();
        this.initialWidgetUpdates = new ArrayList<>();

        int packetsToRead = buf.readVarInt();
        for (int i = 0; i < packetsToRead; i++) {
            SPacketUIWidgetUpdate packet = new SPacketUIWidgetUpdate();
            packet.decode(buf);
            this.initialWidgetUpdates.add(packet);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        UIFactory<?> uiFactory = GregTechAPI.UI_FACTORY_REGISTRY.getObjectById(uiFactoryId);
        if (uiFactory == null) {
            GTLog.logger.warn("Couldn't find UI Factory with id '{}'", uiFactoryId);
        } else {
            uiFactory.initClientUI(serializedHolder, windowId, initialWidgetUpdates);
        }
    }
}
