package gregtech.client.shader;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;
import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static codechicken.lib.render.shader.ShaderHelper.getStream;
import static codechicken.lib.render.shader.ShaderHelper.readShader;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.FRAGMENT;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.VERTEX;

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
//    public static ShaderObject BLACK_HOLE;
    public static ShaderObject SCANNING;

    public static ShaderObject BLOOM_COMBINE;

    public static ShaderObject BLUR;

    // Unity
    public static ShaderObject DOWN_SAMPLING;
    public static ShaderObject UP_SAMPLING;

    // Unreal
    public static ShaderObject S_BLUR;
    public static ShaderObject COMPOSITE;

    // OptiFine
    private static BooleanSupplier isShaderPackLoaded;

    static {
        mc = Minecraft.getMinecraft();
        FULL_IMAGE_PROGRAMS = new HashMap<>();
        if (allowedShader()) {
            initShaders();
        }
        try { // hook optFine. thanks to Scannable.
            final Class<?> clazz = Class.forName("net.optifine.shaders.Shaders");
            final Field shaderPackLoaded = clazz.getDeclaredField("shaderPackLoaded");
            shaderPackLoaded.setAccessible(true);
            isShaderPackLoaded = () -> {
                try {
                    return shaderPackLoaded.getBoolean(null);
                } catch (final IllegalAccessException e) {
                    GTLog.logger.warn("Failed reading field indicating whether shaders are enabled. Shader mod integration disabled.");
                    isShaderPackLoaded = null;
                    return false;
                }
            };
            GTLog.logger.info("Find optiFine mod loaded.");
        } catch (ClassNotFoundException e) {
            GTLog.logger.info("No optiFine mod found.");
        } catch (NoSuchFieldException | NoClassDefFoundError e) {
            GTLog.logger.warn("Failed integrating with shader mod. Ignoring.");
        }
    }

    public static void initShaders() {
        IMAGE_V = initShader(IMAGE_V, VERTEX, "image.vert");
        IMAGE_F = initShader(IMAGE_F, FRAGMENT, "image.frag");
//        BLACK_HOLE = initShader(BLACK_HOLE, FRAGMENT, "blackhole.frag");
        SCANNING = initShader(SCANNING, FRAGMENT, "scanning.frag");
        BLOOM_COMBINE = initShader(BLOOM_COMBINE, FRAGMENT, "bloom_combine.frag");
        BLUR = initShader(BLUR, FRAGMENT, "blur.frag");
        DOWN_SAMPLING = initShader(DOWN_SAMPLING, FRAGMENT, "down_sampling.frag");
        UP_SAMPLING = initShader(UP_SAMPLING, FRAGMENT, "up_sampling.frag");
        S_BLUR = initShader(S_BLUR, FRAGMENT, "seperable_blur.frag");
        COMPOSITE = initShader(COMPOSITE, FRAGMENT, "composite.frag");
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
        return OpenGlHelper.shadersSupported && ConfigHolder.client.shader.useShader;
    }

    public static boolean isOptiFineShaderPackLoaded() {
        return isShaderPackLoaded != null && isShaderPackLoaded.getAsBoolean();
    }

    public static Framebuffer renderFullImageInFBO(Framebuffer fbo, ShaderObject frag, Consumer<ShaderProgram.UniformCache> uniformCache) {
        if (fbo == null || frag == null || !allowedShader()) return fbo;
//        int lastID = glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

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
//        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);

//        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
        return fbo;
    }
}
