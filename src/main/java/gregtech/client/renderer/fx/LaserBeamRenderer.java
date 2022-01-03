package gregtech.client.renderer.fx;

import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LaserBeamRenderer {
    public static ResourceLocation LASER_RED = new ResourceLocation(GTValues.MODID,"textures/fx/laser/laser.png");
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static ResourceLocation LASER_RED_START = new ResourceLocation(GTValues.MODID, "textures/fx/laser/laser_start.png");

    /**
     * Render the Laser Beam.
     *
     * @param body            body texture.
     * @param head            head texture. wont render the head texture if null.
     * @param direction       direction and length vector of laser beam.
     * @param cameraDirection Vector from the eye to the origin position of the laser.
     *                        <p>
     *                        if NULL, a 3D laser rendering will be simulated by two quads (perpendicular to each other)
     *                        </p>
     *                        <p>
     *                        else render normal vertical quad.
     *                        </p>
     * @param beamHeight beam width.
     * @param headWidth head width.
     * @param alpha alpha.
     * @param emit add emit animation for body texture.
     */
    public static void renderBeam(ResourceLocation body, ResourceLocation head, Vector3 direction, Vector3 cameraDirection, double beamHeight, double headWidth, float alpha, float emit) {
        TextureManager renderEngine = MINECRAFT.getRenderManager().renderEngine;
        ITextureObject bodyTexture = null;
        if (body != null) {
            bodyTexture = renderEngine.getTexture(body);
            if (bodyTexture == null) {
                bodyTexture = new SimpleTexture(body);
                renderEngine.loadTexture(body, bodyTexture);
            }
        }
        ITextureObject headTexture = null;
        if (head != null) {
            headTexture = renderEngine.getTexture(head);
            if (headTexture == null) {
                headTexture = new SimpleTexture(head);
                renderEngine.loadTexture(head, headTexture);
            }
        }
        emit = - emit * (MINECRAFT.player.ticksExisted + MINECRAFT.getRenderPartialTicks());
        renderRawBeam(bodyTexture == null ? -1 : bodyTexture.getGlTextureId(), headTexture == null ? -1 : headTexture.getGlTextureId(), direction, cameraDirection, beamHeight, headWidth, alpha, emit);
    }

    /**
     * Render the Laser Beam.
     *
     * @param texture            body texture id.
     * @param headTexture        head texture id. wont render the head texture if -1.
     * @param direction       direction and length vector of laser beam.
     * @param cameraDirection Vector from the eye to the origin position of the laser.
     *                        <p>
     *                        if NULL, a 3D laser rendering will be simulated by two quads (perpendicular to each other)
     *                        </p>
     *                        <p>
     *                        else render normal vertical quad.
     *                        </p>
     * @param beamHeight beam width.
     * @param headWidth head width.
     * @param alpha alpha.
     * @param offset offset of the UV texture.
     */
    public static void renderRawBeam(int texture, int headTexture, Vector3 direction, Vector3 cameraDirection, double beamHeight, double headWidth, float alpha, double offset){
        if (texture != -1) {
            GlStateManager.bindTexture(texture);
        }

        double distance = direction.mag();
        double start = Math.min(headWidth, distance * headWidth);
        distance -= start;

        float degree = (float)Math.toDegrees(new Vector3(direction.x, 0, -direction.z).angle(new Vector3(1,0,0)));
        if (direction.z > 0) {
            degree = -degree;
        }

        GlStateManager.pushMatrix();
        GlStateManager.rotate(degree, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90 - (float)Math.toDegrees(direction.copy().angle(new Vector3(0,1,0))), 0, 0, 1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        if (cameraDirection != null) {
            // Linear algebra drives me crazy
            Vector3 v1 = cameraDirection.copy().project(direction).subtract(cameraDirection);
            Vector3 v2 = new Vector3(0,1,0).crossProduct(direction);
            float rowX = (float)Math.toDegrees(v1.copy().angle(v2));
            // TODO I feel like I'm having a problem with the normal vector flip, but I should leave it here atm and to do other important things.
            if (v1.add(v2).y < 0) rowX = - rowX;
            GlStateManager.rotate(rowX, 1.0F, 0.0F, 0.0F);
            GlStateManager.glNormal3f(0.0F, 0.0F, 1);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(distance, - beamHeight, 0).tex(offset + distance, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            bufferbuilder.pos(start, - beamHeight, 0).tex(offset, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            bufferbuilder.pos(start, beamHeight, 0).tex(offset, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            bufferbuilder.pos(distance, beamHeight, 0).tex(offset + distance, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            tessellator.draw();
            if (headTexture != -1) { // head
                GlStateManager.bindTexture(headTexture);
                GlStateManager.glNormal3f(0.0F, 0.0F, 1);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(start, - beamHeight, 0).tex(1, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(0, - beamHeight, 0).tex(0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(0, beamHeight, 0).tex(0, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(start, beamHeight, 0).tex(1, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                tessellator.draw();
                GlStateManager.glNormal3f(0.0F, 0.0F, 1);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(distance + start, - beamHeight, 0).tex(0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(distance, - beamHeight, 0).tex(1, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(distance, beamHeight, 0).tex(1, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(distance + start, beamHeight, 0).tex(0, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                tessellator.draw();
            }
        } else {
            for (int i = 0; i < 2; ++i) {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.glNormal3f(0.0F, 0.0F, 1);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(distance, - beamHeight, 0).tex(offset + distance, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(start, - beamHeight, 0).tex(offset, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(start, beamHeight, 0).tex(offset, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                bufferbuilder.pos(distance, beamHeight, 0).tex(offset + distance, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                tessellator.draw();
            }

            if (headTexture != -1) { // head
                GlStateManager.bindTexture(headTexture);
                for (int i = 0; i < 2; ++i) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.glNormal3f(0.0F, 0.0F, 1);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    bufferbuilder.pos(start, - beamHeight, 0).tex(1, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    bufferbuilder.pos(0, - beamHeight, 0).tex(0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    bufferbuilder.pos(0, beamHeight, 0).tex(0, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    bufferbuilder.pos(start, beamHeight, 0).tex(1, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    tessellator.draw();
                }
                for (int i = 0; i < 2; ++i) { // tail
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.glNormal3f(0.0F, 0.0F, 1);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    bufferbuilder.pos(distance + start, - beamHeight, 0).tex(0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    bufferbuilder.pos(distance, - beamHeight, 0).tex(1, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    bufferbuilder.pos(distance, beamHeight, 0).tex(1, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    bufferbuilder.pos(distance + start, beamHeight, 0).tex(0, 1).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
                    tessellator.draw();
                }
            }
        }
        GlStateManager.popMatrix();
    }
}
