package gregtech.client.renderer.handler;

import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.ARApplication;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/13
 * @Description: Renderer for AR applications.
 *               Please don't render your stuff here, it's just a handler
 */
@SideOnly(Side.CLIENT)
public class TerminalARRenderer {

    public static ARApplication APP;
    public static int x, y, width, height;
    public static EnumHand HELD_HAND;

    public static void renderHandEvent(RenderSpecificHandEvent event) {
        if (APP != null && event.getHand() == HELD_HAND) {
            event.setCanceled(true);
        }
    }

    public static void renderGameOverlayEvent(RenderGameOverlayEvent.Pre event) {
        if (APP != null && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int sWidth = event.getResolution().getScaledWidth();
            int sHeight = event.getResolution().getScaledHeight();
            width = (int) (380 * 0.8 * sHeight / 256);
            height = (int) (0.8 * sHeight);
            x = (sWidth - width) / 2;
            y = (sHeight - height) / 2;
            GlStateManager.enableBlend();
            TerminalOSWidget.TERMINAL_FRAME.draw(x, y, width, height);
            GlStateManager.disableBlend();
        }
    }

    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        if (APP != null) {
            RenderUtil.useScissor(x, y, width, height, () -> APP.drawARScreen(event));
        }
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null) {
                if (APP != null) {
                    APP.onARClosed();
                    APP = null;
                }
                return;
            }
            HELD_HAND = EnumHand.MAIN_HAND;
            NBTTagCompound tag = player.getHeldItem(EnumHand.MAIN_HAND).getSubCompound("terminal");
            if (tag == null) {
                tag = player.getHeldItem(EnumHand.OFF_HAND).getSubCompound("terminal");
                HELD_HAND = EnumHand.OFF_HAND;
            }
            if (tag != null && tag.hasKey("_ar")) {
                AbstractApplication app = TerminalRegistry.getApplication(tag.getString("_ar"));
                if (app instanceof ARApplication) {
                    if (APP != app) {
                        if (APP != null) {
                            APP.onARClosed();
                        }
                        APP = (ARApplication) app;
                        APP.setAROpened(player.getHeldItem(HELD_HAND));
                        APP.onAROpened();
                    }
                    APP.tickAR(player);
                    return;
                }
            }
            if (APP != null) {
                APP.onARClosed();
                APP = null;
            }
        }
    }
}
