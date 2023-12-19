package gregtech.common.terminal.app.batterymanager;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.client.shader.Shaders;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BatteryWidget extends Widget {

    @SideOnly(Side.CLIENT)
    private ShaderTexture battery;
    private final TerminalOSWidget os;

    public BatteryWidget(int x, int y, int width, int height, TerminalOSWidget os) {
        super(x, y, width, height);
        this.os = os;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        float left = 0;
        IElectricItem electricItem = os.hardwareProvider.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,
                null);
        if (electricItem != null) {
            left = electricItem.getCharge() / (float) electricItem.getMaxCharge();
        }
        if (Shaders.allowedShader()) {
            float progress = left;
            float time = (gui.entityPlayer.ticksExisted + partialTicks) / 20f;
            int color = TerminalTheme.COLOR_F_1.getColor();
            if (battery == null) {
                battery = ShaderTexture.createShader("battery.frag");
            }
            battery.draw(x, y, width, height, uniformCache -> {
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform1F("progress", progress);
                uniformCache.glUniform3F("c_ring", .55f, .7f, .7f);
                uniformCache.glUniform3F("c_sector", (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                        (color & 255) / 255.0F);
                uniformCache.glUniform3F("c_water", (1 - progress), progress, 0f);
            });
        } else {
            int b_color = (int) (255 * (1 - left)) << 16 | (int) (255 * left) << 8 | 255 << 24;
            drawBorder(x, y, width, height, TerminalTheme.COLOR_1.getColor(), 2);
            drawSolidRect(x, y + height - (int) (height * left), width, (int) (height * left), b_color);
        }
        drawStringSized(String.format("%.2f%%", left * 100), x + width / 2f + 3, y + height / 2f - 7, -1, true, 2,
                true);
    }
}
