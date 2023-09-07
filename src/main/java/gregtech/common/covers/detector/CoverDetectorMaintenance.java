package gregtech.common.covers.detector;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class CoverDetectorMaintenance extends CoverDetectorBase implements ITickable {

    public CoverDetectorMaintenance(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return ConfigHolder.machines.enableMaintenance
                && coverHolder instanceof IMaintenance maintenance
                && maintenance.hasMaintenanceMechanics();
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_MAINTENANCE.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0) {
            return;
        }

        IMaintenance maintenance = (IMaintenance) coverHolder;
        int signal = getRedstoneSignalOutput();
        boolean shouldSignal = isInverted() != maintenance.hasMaintenanceProblems();
        if (shouldSignal && signal != 15) {
            setRedstoneSignalOutput(15);
        } else if (!shouldSignal && signal == 15) {
            setRedstoneSignalOutput(0);
        }
    }
}
