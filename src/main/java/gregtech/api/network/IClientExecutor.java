package gregtech.api.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IClientExecutor {

    @SideOnly(Side.CLIENT)
    void executeClient(NetHandlerPlayClient handler);
}
