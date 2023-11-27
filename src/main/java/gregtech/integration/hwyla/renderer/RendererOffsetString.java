package gregtech.integration.hwyla.renderer;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.config.OverlayConfig;
import mcp.mobius.waila.overlay.DisplayUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/** Adapted from Jade 1.12.2, a HWYLA addon mod. */
public class RendererOffsetString implements IWailaTooltipRenderer {

    @NotNull
    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        int x = Integer.parseInt(params[1]);
        int y = Integer.parseInt(params[2]);
        return new Dimension(x + DisplayUtil.getDisplayWidth(params[0]), y + (params[0].equals("") ? 0 : 8));
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        int x = Integer.parseInt(params[1]);
        int y = Integer.parseInt(params[2]);
        DisplayUtil.drawString(params[0], x, y, OverlayConfig.fontcolor, true);
    }
}
