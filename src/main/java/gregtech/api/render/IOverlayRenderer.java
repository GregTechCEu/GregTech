package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IOverlayRenderer {

    @SideOnly(Side.CLIENT)
    default void renderSided(EnumFacing side, CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        renderSided(side, Cuboid6.full, renderState, pipeline, translation);
    }

    @SideOnly(Side.CLIENT)
    void renderSided(EnumFacing side, Cuboid6 bounds, CCRenderState renderState, IVertexOperation[] pipeline, Matrix4 translation);

}
