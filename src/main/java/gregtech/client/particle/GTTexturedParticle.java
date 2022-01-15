package gregtech.client.particle;

import gregtech.api.gui.resources.ResourceHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    private static final Map<ResourceLocation, IGTParticleHandler> textureMap = new HashMap<>();

    private ResourceLocation customTexture;

    public GTTexturedParticle(World worldIn, double posXIn, double posYIn, double posZIn, ResourceLocation texture, int textureCount) {
        super(worldIn, posXIn, posYIn, posZIn);
        setTexture(texture);
        setTexturesCount(textureCount);
    }

    public GTTexturedParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, ResourceLocation texture, int textureCount) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        setTexture(texture);
        setTexturesCount(textureCount);
    }

    public void setTexture(ResourceLocation texture) {
        this.customTexture = texture;
        if (!textureMap.containsKey(texture)) {
            textureMap.put(texture, new TexturedParticleHandler(texture));
        }
    }

    @Override
    public final IGTParticleHandler getGLHandler() {
        return textureMap.get(customTexture);
    }

    private static class TexturedParticleHandler implements IGTParticleHandler {
        private final ResourceLocation texture;

        public TexturedParticleHandler(ResourceLocation texture) {
            this.texture = texture;
        }

        @Override
        public void preDraw(BufferBuilder buffer) {
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
