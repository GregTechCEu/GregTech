package gregtech.common.covers.detector;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CoverDetectorItem extends CoverDetectorBase implements ITickable {
    public CoverDetectorItem(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ITEM.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IItemHandler itemHandler = coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler == null)
            return;

        int storedItems = 0;
        int itemCapacity = itemHandler.getSlots() * itemHandler.getSlotLimit(0);

        if (itemCapacity == 0)
            return;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            storedItems += itemHandler.getStackInSlot(i).getCount();
        }

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedItems, itemCapacity, isInverted()));
    }
}
