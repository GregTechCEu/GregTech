package gregtech.client.particle;

import codechicken.lib.render.shader.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.EXTFramebufferObject;

import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/01
 * @Description: using shader program render the texture in FBO and then bind this texture for particle.
 */
@SideOnly(Side.CLIENT)
public abstract class GTTexturedShaderParticle extends GTParticle {
    public GTTexturedShaderParticle(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }

    @Override
    public abstract FBOShaderHandler getGLHandler();

    public abstract static class FBOShaderHandler implements IGTParticleHandler, ShaderProgram.IUniformCallback {
        protected final ShaderProgram program;
        private final Framebuffer fbo;

        public FBOShaderHandler(ShaderProgram program) {
            this.program = program;
            fbo = new Framebuffer(1000, 1000, false);
        }

        public void hookPreDraw() {
            program.useShader();
        }

        @Override
        public final void preDraw(BufferBuilder buffer) {
            if (program != null) {
                int lastID = glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
                fbo.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
                fbo.framebufferClear();
                fbo.bindFramebuffer(true);
                GlStateManager.pushMatrix();
                GlStateManager.pushAttrib();
                GlStateManager.viewport(0, 0, 1000, 1000);
                Tessellator tessellator = Tessellator.getInstance();

                hookPreDraw();

                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-1, 1, 0).tex(0, 0).endVertex();
                buffer.pos(-1, -1, 0).tex(0, 1).endVertex();
                buffer.pos(1, -1, 0).tex(1, 1).endVertex();
                buffer.pos(1, 1, 0).tex(1, 0).endVertex();

                tessellator.draw();

                program.releaseShader();

                Minecraft minecraft = Minecraft.getMinecraft();
                GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);
                GlStateManager.popAttrib();
                GlStateManager.popMatrix();
                fbo.unbindFramebufferTexture();
                OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);

                GlStateManager.bindTexture(fbo.framebufferTexture);
            }
            GlStateManager.color(1,1,1,1);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        @Override
        public final void postDraw(BufferBuilder buffer) {
            Tessellator.getInstance().draw();
        }

    }
}
