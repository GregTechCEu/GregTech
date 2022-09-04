package gregtech.api.net;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;

public interface IServerExecutor {

    /**
     * Used to execute code on the server, after receiving a packet from the client.<br><br>
     * <p>
     * CANNOT be implemented with {@link IClientExecutor#executeClient(NetHandlerPlayClient)}, only one at a time is supported.
     *
     * @param handler Network handler that contains useful data and helpers.
     */
    void executeServer(NetHandlerPlayServer handler);
}
