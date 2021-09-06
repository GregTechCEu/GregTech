package gregtech.api.gui.resources;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;
import gregtech.api.render.shader.Shaders;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

//todo unfinished
public class ShaderTexture implements IGuiTexture{
    @SideOnly(Side.CLIENT)
    private static final Map<String, ShaderProgram> PROGRAMS = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private ShaderProgram program;

    public ShaderTexture(String location) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (!PROGRAMS.containsKey(location)) {
                ShaderObject object = Shaders.loadShader(ShaderObject.ShaderType.FRAGMENT, location);
                if (object != null) {
                    program = new ShaderProgram();
                    program.attachShader(object);
                    PROGRAMS.put(location, program);
                }
            } else {
                program = PROGRAMS.get(location);
            }
        }
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        this.draw(x, y, width, height, 0,0, 0);
    }

    public void draw(double x, double y, int width, int height, int mouseX, int mouseY, float time) {
        if (program != null) {
            program.useShader(cache->{
                cache.glUniform1F("u_time", time);
                cache.glUniform2F("u_mouse", (float)(mouseX - x), (float)(mouseY - y));
                cache.glUniform2F("u_resolution", width, height);
            });
        }
    }
}
