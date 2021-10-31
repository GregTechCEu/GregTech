package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

public class FullBrightnessSimpleCubeRenderer extends SimpleCubeRenderer {
    public FullBrightnessSimpleCubeRenderer(String basePath) {
        super(basePath);
    }

    @Override
    public void renderSided(EnumFacing side, Matrix4 translation, Cuboid6 bounds, CCRenderState renderState, IVertexOperation[] pipeline) {
        super.renderSided(side, translation, bounds, renderState, ArrayUtils.addAll(pipeline, new LightMapOperation(0b10100000, 0b10100000), new ColourOperation(0xffffffff)));
    }
}
