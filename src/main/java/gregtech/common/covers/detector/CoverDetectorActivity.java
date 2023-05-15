package gregtech.common.covers.detector;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.ICoverable;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class CoverDetectorActivity extends CoverDetectorBase implements ITickable {
    public CoverDetectorActivity(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ACTIVITY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IWorkable workable = coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (workable == null)
            return;

        if (isInverted()) setRedstoneSignalOutput(workable.isActive() && workable.isWorkingEnabled() ? 0 : 15);
        else setRedstoneSignalOutput(workable.isActive() && workable.isWorkingEnabled() ? 15 : 0);
    }
}
