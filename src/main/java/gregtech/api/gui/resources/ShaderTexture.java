package gregtech.api.gui.resources;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;
import gregtech.api.gui.Widget;
import gregtech.client.shader.Shaders;
import gregtech.common.ConfigHolder;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShaderTexture implements IGuiTexture{
    @SideOnly(Side.CLIENT)
    private static final Map<String, ShaderTexture> PROGRAMS = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private ShaderProgram program;
    @SideOnly(Side.CLIENT)
    private ShaderObject object;
    private float resolution = (float)ConfigHolder.client.resolution;

    public static void clear(){
        PROGRAMS.values().forEach(ShaderTexture::dispose);
        PROGRAMS.clear();
    }

    private ShaderTexture() {

    }

    public void dispose() {
        if (object != null) {
            object.disposeObject();
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateRawShader(String rawShader) {
        dispose();
        object = new ShaderObject(ShaderObject.ShaderType.FRAGMENT, rawShader).compileShader();
        program = new ShaderProgram();
        program.attachShader(object);
    }

    @SideOnly(Side.CLIENT)
    private ShaderTexture(ShaderProgram program, ShaderObject object) {
        this.program = program;
        this.object = object;
    }

    public static ShaderTexture createShader(String location) {
        if (FMLCommonHandler.instance().getSide().isClient() && Shaders.allowedShader()) {
            if (!PROGRAMS.containsKey(location)) {
                ShaderObject object = Shaders.loadShader(ShaderObject.ShaderType.FRAGMENT, location);
                if (object != null) {
                    ShaderProgram program = new ShaderProgram();
                    program.attachShader(object);
                    PROGRAMS.put(location, new ShaderTexture(program, object));
                } else {
                    return new ShaderTexture();
                }
            }
            return PROGRAMS.get(location);
        } else {
            return new ShaderTexture();
        }
    }

    public static ShaderTexture createRawShader(String rawShader) {
        if (FMLCommonHandler.instance().getSide().isClient() && Shaders.allowedShader()) {
            ShaderProgram program = new ShaderProgram();
            ShaderObject object = new ShaderObject(ShaderObject.ShaderType.FRAGMENT, rawShader).compileShader();
            program.attachShader(object);
            return new ShaderTexture(program, object);
        } else {
            return new ShaderTexture();
        }
    }

    public ShaderTexture setResolution(float resolution) {
        this.resolution = resolution;
        return this;
    }

    public float getResolution() {
        return resolution;
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        this.draw(x, y, width, height, null);
    }

    public void draw(double x, double y, int width, int height, Consumer<ShaderProgram.UniformCache> uniformCache) {
        if (program != null) {
            program.useShader(cache->{
                cache.glUniform2F("u_resolution", width * resolution, height * resolution);
                if (uniformCache != null) {
                    uniformCache.accept(cache);
                }
            });
            Widget.drawTextureRect(x, y, width, height);
            program.releaseShader();
        }
    }
}
