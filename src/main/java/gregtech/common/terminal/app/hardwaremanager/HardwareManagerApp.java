package gregtech.common.terminal.app.hardwaremanager;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.render.shader.Shaders;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.items.MetaItems;
import org.lwjgl.opengl.GL11;

public class HardwareManagerApp extends AbstractApplication {

    public HardwareManagerApp() {
        super("hardware", new ItemStackTexture(MetaItems.INTEGRATED_CIRCUIT.getStackForm()));
    }

    @Override
    protected void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        float time = (gui.entityPlayer.ticksExisted + partialTicks) / 20f;
        if (Shaders.allowedShader()) {
            ResourceHelper.bindTexture("textures/shaders/font1.png");
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            Shaders.renderFullImageInFBO(Shaders.BUFFER_GUI, Shaders.CIRCUIT, uniformCache -> {
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform2F("u_mouse", (float)(mouseX - x) * 3, (float)(mouseY - y) * 3);
            }).bindFramebufferTexture();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            drawTextureRect(x, y, width, height);
        } else {
            drawSolidRect(x, y, width, height, TerminalTheme.COLOR_B_2.getColor());
        }
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
