package gregtech.api.render.scene;

import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.PositionedRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * @Author: KilaBash
 * @Date: 2021/8/24
 * @Description:
 */
public class GuiWorldSceneRenderer extends WorldSceneRenderer {
    private int clearColor;

    @Override
    protected PositionedRect getPositionedRect(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        //compute window size from scaled width & height
        int windowWidth = (int) (width / (resolution.getScaledWidth() * 1.0) * mc.displayWidth);
        int windowHeight = (int) (height / (resolution.getScaledHeight() * 1.0) * mc.displayHeight);
        //translate gui coordinates to window's ones (y is inverted)
        int windowX = (int) (x / (resolution.getScaledWidth() * 1.0) * mc.displayWidth);
        int windowY = mc.displayHeight - (int) (y / (resolution.getScaledHeight() * 1.0) * mc.displayHeight) - windowHeight;

        return super.getPositionedRect(windowX, windowY, windowWidth, windowHeight);
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    @Override
    protected void clearView(int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
        RenderUtil.setGlClearColorFromInt(clearColor, 255);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public BlockPosFace screenPos2BlockPosFace(int mouseX, int mouseY, int x, int y, int width, int height) {
        return super.screenPos2BlockPosFace(mouseX, mouseY, x, y, width, height);
    }

    @Override
    public Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth, int x, int y, int width, int height) {
        return super.blockPos2ScreenPos(pos, depth, x, y, width, height);
    }
}
