package gregtech.client.renderer.cclop;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: hack light map in CCL;
 */
@SideOnly(Side.CLIENT)
public class LightMapOperation implements IVertexOperation {

    public static final int operationIndex = CCRenderState.registerOperation();
    int lightmapX;
    int lightmapY;

    public LightMapOperation(int lightmapX, int lightmapY) {
        this.lightmapX = lightmapX;
        this.lightmapY = lightmapY;
    }

    @Override
    public boolean load(CCRenderState ccRenderState) {
        return true;
    }

    @Override
    public void operate(CCRenderState ccRenderState) {
        ccRenderState.brightness = lightmapY << 16 | lightmapX;
    }

    @Override
    public int operationID() {
        return operationIndex;
    }
}
