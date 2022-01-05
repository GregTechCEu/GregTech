package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.ICoverable;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

public class CoverActivityDetectorAdvanced extends CoverActivityDetector {

    public CoverActivityDetectorAdvanced(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ACTIVITY_ADVANCED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (this.coverHolder.getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (this.isInverted) {
            this.setInverted();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.cover.activity_detector_advanced.message_activity_normal"));
        } else {
            this.setInverted();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.cover.activity_detector_advanced.message_activity_inverted"));
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IWorkable workable = coverHolder.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (workable == null)
            return;

        int outputAmount = (int) (15.0 * workable.getProgress() / workable.getMaxProgress());

        if (!workable.isWorkingEnabled())
            outputAmount = 0;
        else if (this.isInverted)
            outputAmount = 15 - outputAmount;

        setRedstoneSignalOutput(outputAmount);
    }
}
