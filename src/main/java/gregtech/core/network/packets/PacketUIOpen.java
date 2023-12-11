package gregtech.core.network.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.UIFactory;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.api.util.GTLog;
import gregtech.core.network.NetworkUtils;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class PacketUIOpen implements IPacket, IClientExecutor {

    private int uiFactoryId;
    private PacketBuffer serializedHolder;
    private int windowId;
    private List<PacketUIWidgetUpdate> initialWidgetUpdates;

    @SuppressWarnings("unused")
    public PacketUIOpen() {}

    public PacketUIOpen(int uiFactoryId, PacketBuffer serializedHolder, int windowId,
                        List<PacketUIWidgetUpdate> initialWidgetUpdates) {
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
        for (PacketUIWidgetUpdate packet : initialWidgetUpdates) {
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
            PacketUIWidgetUpdate packet = new PacketUIWidgetUpdate();
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
