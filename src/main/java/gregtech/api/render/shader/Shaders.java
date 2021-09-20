package gregtech.api.render.shader;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static codechicken.lib.render.shader.ShaderHelper.getStream;
import static codechicken.lib.render.shader.ShaderHelper.readShader;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.FRAGMENT;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.VERTEX;
import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/30
 * @Description: Shaders are magic!!!
 */
@SideOnly(Side.CLIENT)
public class Shaders {
    public static Minecraft mc;
    private final static Map<ShaderObject, ShaderProgram> FULL_IMAGE_PROGRAMS;

    public static ShaderObject IMAGE_V;
    public static ShaderObject IMAGE_F;
    public static ShaderObject BLACK_HOLE;
    public static ShaderObject SCANNING;

    static {
        mc = Minecraft.getMinecraft();
        FULL_IMAGE_PROGRAMS = new HashMap<>();
        if (allowedShader()) {
            initShaders();
        }
    }

    public static void initShaders() {
        IMAGE_V = initShader(IMAGE_V, VERTEX, "image.vert");
        IMAGE_F = initShader(IMAGE_F, FRAGMENT, "image.frag");
        BLACK_HOLE = initShader(BLACK_HOLE, FRAGMENT, "blackhole.frag");
        SCANNING = initShader(SCANNING, FRAGMENT, "scanning.frag");
        FULL_IMAGE_PROGRAMS.clear();
    }

    private static ShaderObject initShader(ShaderObject object, ShaderObject.ShaderType shaderType, String location) {
        unloadShader(object);
        return loadShader(shaderType, location);
    }

    public static ShaderObject loadShader(ShaderObject.ShaderType shaderType, String location) {
        try {
            return new ShaderObject(shaderType, readShader(getStream(String.format("/assets/%s/shaders/%s", GTValues.MODID, location)))).compileShader();
        } catch (Exception exception) {
            GTLog.logger.error("error while loading shader {}", location, exception);
        }
        return null;
    }

    public static void unloadShader(ShaderObject shaderObject) {
        if (shaderObject != null) {
            shaderObject.disposeObject();
        }
    }

    public static boolean allowedShader() {
        return OpenGlHelper.shadersSupported && ConfigHolder.U.clientConfig.useShader;
    }

    public static Framebuffer renderFullImageInFBO(Framebuffer fbo, ShaderObject frag, Consumer<ShaderProgram.UniformCache> uniformCache) {
        if (fbo == null || frag == null) return fbo;
        int lastID = glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        fbo.bindFramebuffer(true);

        ShaderProgram program = FULL_IMAGE_PROGRAMS.get(frag);
        if (program == null) {
            program = new ShaderProgram();
            program.attachShader(IMAGE_V);
            program.attachShader(frag);
            FULL_IMAGE_PROGRAMS.put(frag, program);
        }

        program.useShader(cache->{
            cache.glUniform2F("u_resolution", fbo.framebufferWidth, fbo.framebufferHeight);
            if (uniformCache != null) {
                uniformCache.accept(cache);
            }
        });

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-1, 1, 0).tex(0, 0).endVertex();
        buffer.pos(-1, -1, 0).tex(0, 1).endVertex();
        buffer.pos(1, -1, 0).tex(1, 1).endVertex();
        buffer.pos(1, 1, 0).tex(1, 0).endVertex();
        tessellator.draw();

        program.releaseShader();
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);

        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
        return fbo;
    }

    public static class ShaderCommand extends CommandBase {

        @Override
        @Nonnull
        public String getName() {
            return "gt_rs";
        }

        @Override
        @Nonnull
        public String getUsage(@Nonnull ICommandSender sender) {
            return "reload GTCEus' shaders";
        }

        @Override
        public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
            if (allowedShader()) {
                initShaders();
                ShaderTexture.clear();
                sender.sendMessage(new TextComponentString("reload all shaders"));
            } else {
                sender.sendMessage(new TextComponentString("disable shaders"));
            }
        }
    }

}
