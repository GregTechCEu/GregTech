package gregtech.api.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public interface INetworkHandler {

    void registerPacket(Class<? extends IPacket> packetClass);

    void sendToAll(IPacket packet);

    void sendTo(IPacket packet, EntityPlayerMP player);

    void sendToAllAround(IPacket packet, TargetPoint point);

    void sendToAllTracking(IPacket packet, TargetPoint point);

    void sendToAllTracking(IPacket packet, Entity entity);

    void sendToDimension(IPacket packet, int dimensionId);

    void sendToServer(IPacket packet);
}
