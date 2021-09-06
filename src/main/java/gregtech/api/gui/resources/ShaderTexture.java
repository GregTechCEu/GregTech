package gregtech.api.gui.resources;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;
import gregtech.api.gui.Widget;
import gregtech.api.render.shader.Shaders;
import gregtech.common.ConfigHolder;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShaderTexture implements IGuiTexture{
    @SideOnly(Side.CLIENT)
    private static final Map<String, Tuple<ShaderProgram, ShaderObject>> PROGRAMS = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private ShaderProgram program;
    private float resolution = (float)ConfigHolder.U.clientConfig.resolution;

    public static void clear(){
        for (Tuple<ShaderProgram, ShaderObject> value : PROGRAMS.values()) {
            value.getSecond().disposeObject();
        }
        PROGRAMS.clear();
    }

    public ShaderTexture(String location) {
//        if (!ConfigHolder.debug) {
//            ConfigHolder.debug = !ConfigHolder.debug;
//            clear();
//        }
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (!PROGRAMS.containsKey(location)) {
                ShaderObject object = Shaders.loadShader(ShaderObject.ShaderType.FRAGMENT, location);
                if (object != null) {
                    program = new ShaderProgram();
                    program.attachShader(object);
                    PROGRAMS.put(location, new Tuple<>(program, object));
                }
            } else {
                program = PROGRAMS.get(location).getFirst();
            }
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
