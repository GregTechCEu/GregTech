package gregtech.api.gui.resources.picturetexture;

import gregtech.api.gui.resources.IGuiTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public abstract class PictureTexture implements IGuiTexture {
    public int width;
    public int height;

    public PictureTexture(int width, int height) {
        this.width = width;
        this.height = height;

    }

    public void beforeRender() {

    }

    @Override
    public void draw(double x, double y, int width, int height) {
        render((float)x, (float)y, 1, 1, 0, width, height, false, false);
    }

    public void render(float x, float y, float width, float height, float rotation, float scaleX, float scaleY, boolean flippedX, boolean flippedY) {
        this.beforeRender();
        GlStateManager.color(1,1,1,1);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.bindTexture(this.getTextureID());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.pushMatrix();
        GL11.glRotated(rotation, 0, 0, 1);
        GlStateManager.enableRescaleNormal();
        GL11.glScaled(scaleX, scaleY, 1);
        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glTexCoord3f(flippedY ? 1 : 0, flippedX ? 1 : 0, 0);
        GL11.glVertex3f(x, y, 0.01f);
        GL11.glTexCoord3f(flippedY ? 1 : 0, flippedX ? 0 : 1, 0);
        GL11.glVertex3f(x, y + height, 0.01f);
        GL11.glTexCoord3f(flippedY ? 0 : 1, flippedX ? 0 : 1, 0);
        GL11.glVertex3f(x + width, y + height, 0.01f);
        GL11.glTexCoord3f(flippedY ? 0 : 1, flippedX ? 1 : 0, 0);
        GL11.glVertex3f(x + width, y, 0.01f);
        GL11.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }

    public abstract void tick();

    public abstract int getTextureID();

    public boolean hasTexture() {
        return getTextureID() != -1;
    }

    public void release() {
        GlStateManager.deleteTexture(getTextureID());
    }
}
