package gregtech.client.particle;

import gregtech.api.gui.resources.ResourceHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/01
 * @Description:
 */
@SideOnly(Side.CLIENT)
public class GTTexturedParticle extends GTParticle {
    private static final Map<ResourceLocation, IGTParticleHandler[]> textureMap = new HashMap<>();

    private ResourceLocation customTexture;

    public GTTexturedParticle(World worldIn, double posXIn, double posYIn, double posZIn, ResourceLocation texture) {
        super(worldIn, posXIn, posYIn, posZIn);
        setTexture(texture);
    }

    public GTTexturedParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, ResourceLocation texture) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        setTexture(texture);
    }

    public void setTexture(ResourceLocation texture) {
        this.customTexture = texture;
        if (!textureMap.containsKey(texture)) {
            textureMap.put(texture, new IGTParticleHandler[]{
                new TexturedParticleHandler(true,texture),
                new TexturedParticleHandler(false,texture)
            });
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public final IGTParticleHandler getGLHandler() {
        return shouldDisableDepth() ? textureMap.get(customTexture)[0] : textureMap.get(customTexture)[1];
    }

    private static class TexturedParticleHandler implements IGTParticleHandler {
        private final boolean depth;
        private final ResourceLocation texture;

        public TexturedParticleHandler(boolean depth, ResourceLocation texture) {
            this.depth = depth;
            this.texture = texture;
        }

        @Override
        public void preDraw(BufferBuilder buffer) {
            if (depth) {
                GlStateManager.depthMask(true);
            } else {
                GlStateManager.depthMask(false);
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
            }
            ResourceHelper.bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        @Override
        public void postDraw(BufferBuilder buffer) {
            Tessellator.getInstance().draw();
        }
    }

}
