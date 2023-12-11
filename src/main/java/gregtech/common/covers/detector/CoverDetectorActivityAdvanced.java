package gregtech.common.covers.detector;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorActivityAdvanced extends CoverDetectorActivity {

    public CoverDetectorActivityAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                         @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_ACTIVITY_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline,
                translation);
    }

    @Override
    public void update() {
        if (getOffsetTimer() % 20 != 0) return;

        IWorkable workable = getCoverableView().getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (workable == null) return;

        if (workable.getMaxProgress() == 0) return;

        int outputAmount = RedstoneUtil.computeRedstoneValue(workable.getProgress(), workable.getMaxProgress(),
                isInverted());

        // nonstandard logic for handling off state
        if (!workable.isWorkingEnabled()) {
            outputAmount = 0;
        }

        setRedstoneSignalOutput(outputAmount);
    }
}
