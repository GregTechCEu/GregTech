package gregtech.api.render.shader.postprocessing;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/10/03
 * @Description:
 */
public interface IPostRender {
    void render(double x, double y, double z, float partialTicks);
}
