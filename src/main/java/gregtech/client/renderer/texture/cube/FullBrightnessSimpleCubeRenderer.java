package gregtech.client.renderer.texture.cube;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class FullBrightnessSimpleCubeRenderer extends SimpleOverlayRenderer {
    public FullBrightnessSimpleCubeRenderer(String basePath) {
        super(basePath);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        super.renderOrientedState(renderState, translation, ArrayUtils.addAll(pipeline, new LightMapOperation(0b10100000, 0b10100000), new ColourOperation(0xffffffff)), bounds, frontFacing, isActive, isWorkingEnabled);
    }
}
