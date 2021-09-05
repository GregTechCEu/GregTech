package gregtech.common.terminal.app.batterymanager;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.render.shader.Shaders;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/03
 * @Description:
 */
public class BatteryWidget extends Widget {
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
        float time = (gui.entityPlayer.ticksExisted + partialTicks) / 20f;
        float left = 0;
        int color = TerminalTheme.COLOR_F_1.getColor();
        IElectricItem electricItem = os.hardwareProvider.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            left = electricItem.getCharge() / (float)electricItem.getMaxCharge();
        }
        if (Shaders.allowedShader()) {
            float progress = left;
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            Shaders.renderFullImageInFBO(Shaders.BUFFER_A, Shaders.BATTERY, uniformCache -> {
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform1F("progress", progress);
                uniformCache.glUniform3F("c_ring", .55f, .7f, .7f);
                uniformCache.glUniform3F("c_sector", (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
                uniformCache.glUniform3F("c_water", 1f * (1 - progress), 1f * progress, 0f);
            }).bindFramebufferTexture();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x, y + height, 0.0D).tex(0, 0).endVertex();
            buffer.pos(x + width, y + height, 0.0D).tex(1, 0).endVertex();
            buffer.pos(x + width, y, 0.0D).tex(1, 1).endVertex();
            buffer.pos(x, y, 0.0D).tex(0, 1).endVertex();
            tessellator.draw();
        } else {
            int b_color = (int)(255 * (1 - left)) << 16 | (int)(255 * left) << 8 | 255 << 24;
            drawBorder(x, y, width, height, TerminalTheme.COLOR_1.getColor(), 2);
            drawSolidRect(x, y + height - (int)(height * left), width, (int)(height * left), b_color);
        }
        drawStringSized(String.format("%.2f%%", left * 100), x + width / 2f + 3, y + height / 2f - 7, -1, true, 2, true);
    }
}
