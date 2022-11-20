package gregtech.api.network;

import net.minecraft.network.NetHandlerPlayServer;

public interface IServerExecutor {

    void executeServer(NetHandlerPlayServer handler);
}
