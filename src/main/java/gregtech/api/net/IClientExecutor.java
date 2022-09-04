package gregtech.api.net;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IClientExecutor {

    /**
     * Used to execute code on the client, after receiving a packet from the server.<br><br>
     * <p>
     * CANNOT be implemented with {@link IServerExecutor#executeServer(NetHandlerPlayServer)}, only one at a time is supported.
     *
     * @param handler Network handler that contains useful data and helpers.
     */
    @SideOnly(Side.CLIENT)
    void executeClient(NetHandlerPlayClient handler);
}
