package gregtech.api.render.cuberenderer;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.render.cclop.ColourOperation;
import gregtech.api.render.cclop.LightMapOperation;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

public class FullBrightnessSimpleCubeRenderer extends SimpleCubeRenderer {
    public FullBrightnessSimpleCubeRenderer(String basePath) {
        super(basePath);
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        super.renderOrientedState(renderState, translation, ArrayUtils.addAll(pipeline, new LightMapOperation(0b10100000, 0b10100000), new ColourOperation(0xffffffff)), bounds, frontFacing, isActive, isWorkingEnabled);
    }
}
