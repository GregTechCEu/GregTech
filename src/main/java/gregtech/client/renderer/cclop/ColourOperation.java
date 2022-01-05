package gregtech.client.renderer.cclop;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ColourOperation implements IVertexOperation {
    public static final int operationIndex = CCRenderState.registerOperation();
    int colour;

    public ColourOperation(int colour) {
        this.colour = colour;
    }

    @Override
    public boolean load(CCRenderState ccRenderState) {
        return true;
    }

    @Override
    public void operate(CCRenderState ccRenderState) {
        ccRenderState.colour = colour;
    }

    @Override
    public int operationID() {
        return operationIndex;
    }
}
