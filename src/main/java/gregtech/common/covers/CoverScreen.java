package gregtech.common.covers;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverScreen extends CoverBase {

    public CoverScreen(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                       @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DISPLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public boolean shouldAutoConnectToPipes() {
        return false;
    }
}
