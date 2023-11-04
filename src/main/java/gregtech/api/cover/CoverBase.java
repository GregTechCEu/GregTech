package gregtech.api.cover;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class CoverBase implements Cover {

    private final CoverDefinition definition;
    private final CoverableView coverableView;
    private final EnumFacing attachedSide;

    public CoverBase(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                     @NotNull EnumFacing attachedSide) {
        this.definition = definition;
        this.coverableView = coverableView;
        this.attachedSide = attachedSide;
    }

    @Override
    public final @NotNull CoverDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public final @NotNull CoverableView getCoverableView() {
        return this.coverableView;
    }

    @Override
    public final @NotNull EnumFacing getAttachedSide() {
        return this.attachedSide;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderCoverPlate(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, @NotNull IVertexOperation[] pipeline,
                                  @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        TextureAtlasSprite casingSide = getPlateSprite();
        for (EnumFacing coverPlateSide : EnumFacing.VALUES) {
            boolean isAttachedSide = getAttachedSide().getAxis() == coverPlateSide.getAxis();
            if (isAttachedSide || !getCoverableView().hasCover(coverPlateSide)) {
                Textures.renderFace(renderState, translation, pipeline, coverPlateSide, plateBox, casingSide, BlockRenderLayer.CUTOUT_MIPPED);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[GTValues.LV].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
