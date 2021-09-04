package gregtech.api.render.shader;

import codechicken.lib.render.shader.ShaderObject;
import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.OpenGlHelper;

import java.io.IOException;

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
public class Shaders {

    static {
        if (allowedShader()) {
            initShaders();
        }
    }

    public static void initShaders() {
    }

    public static ShaderObject loadShader(ShaderObject.ShaderType shaderType, String location) {
        try {
            return new ShaderObject(shaderType, readShader(getStream(String.format("/assets/%s/shaders/%s", GTValues.MODID, location))));
        } catch (IOException exception) {
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


}
