package gregtech.client.renderer.scene;

import gregtech.api.util.PositionedRect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * Created with IntelliJ IDEA.
 * 
 * @Author: KilaBash
 * @Date: 2021/8/24
 * @Description: Real-time rendering renderer.
 *               If you need to render scene as a texture, use the FBO {@link FBOWorldSceneRenderer}.
 */
@SideOnly(Side.CLIENT)
public class ImmediateWorldSceneRenderer extends WorldSceneRenderer {

    public ImmediateWorldSceneRenderer(World world) {
        super(world);
    }

    @Override
    protected PositionedRect getPositionedRect(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        // compute window size from scaled width & height
        int windowWidth = (int) (width / (resolution.getScaledWidth() * 1.0) * mc.displayWidth);
        int windowHeight = (int) (height / (resolution.getScaledHeight() * 1.0) * mc.displayHeight);
        // translate gui coordinates to window's ones (y is inverted)
        int windowX = (int) (x / (resolution.getScaledWidth() * 1.0) * mc.displayWidth);
        int windowY = mc.displayHeight - (int) (y / (resolution.getScaledHeight() * 1.0) * mc.displayHeight) -
                windowHeight;

        return super.getPositionedRect(windowX, windowY, windowWidth, windowHeight);
    }

    @Override
    protected void clearView(int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
        super.clearView(x, y, width, height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
