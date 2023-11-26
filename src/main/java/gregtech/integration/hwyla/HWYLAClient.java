package gregtech.integration.hwyla;

import gregtech.integration.hwyla.renderer.RendererOffsetString;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

@SideOnly(Side.CLIENT)
@WailaPlugin
public class HWYLAClient implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerTooltipRenderer("gregtech.text", new RendererOffsetString());
    }
}
