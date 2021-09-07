package gregtech.common.terminal.app.hardwaremanager;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.render.shader.Shaders;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.items.MetaItems;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HardwareManagerApp extends AbstractApplication {
    @SideOnly(Side.CLIENT)
    private static final TextureArea CIRCUIT_LINE = TextureArea.fullImage("textures/gui/terminal/hardware_manager/circuit.png");
    @SideOnly(Side.CLIENT)
    private ShaderTexture circuit;

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
            if (circuit == null) {
                circuit = ShaderTexture.createShader("circuit.frag");
            }
            ResourceHelper.bindTexture("textures/shaders/font1.png");
            circuit.draw(x, y, width, height, uniformCache -> {
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform2F("u_mouse",
                        (float)(mouseX - x) * circuit.getResolution(),
                        (float)(mouseY - y) * circuit.getResolution());
            });
        } else {
            drawSolidRect(x, y, width, height, TerminalTheme.COLOR_B_2.getColor());
        }
        CIRCUIT_LINE.draw(x, y, width, height);
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
