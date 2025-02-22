package gregtech.client.renderer.pipe;

import gregtech.api.block.UnlistedBooleanProperty;
import gregtech.api.block.UnlistedByteProperty;
import gregtech.api.block.UnlistedFloatProperty;
import gregtech.api.block.UnlistedIntegerProperty;
import gregtech.api.block.UnlistedPropertyMaterial;

// holder class for render-required blockstate properties that also exists on server.
public final class PipeRenderProperties {

    // AbstractPipeModel
    public static final UnlistedPropertyMaterial MATERIAL_PROPERTY = new UnlistedPropertyMaterial("material");
    public static UnlistedFloatProperty THICKNESS_PROPERTY = new UnlistedFloatProperty("thickness");
    public static UnlistedPropertyMaterial FRAME_MATERIAL_PROPERTY = new UnlistedPropertyMaterial("frame_material");
    public static UnlistedByteProperty FRAME_MASK_PROPERTY = new UnlistedByteProperty("frame_mask");
    public static UnlistedByteProperty CLOSED_MASK_PROPERTY = new UnlistedByteProperty("closed_mask");
    public static UnlistedByteProperty BLOCKED_MASK_PROPERTY = new UnlistedByteProperty("blocked_mask");
    public static UnlistedIntegerProperty COLOR_PROPERTY = new UnlistedIntegerProperty("color");
    // ActivablePipeModel
    public static final UnlistedBooleanProperty ACTIVE_PROPERTY = new UnlistedBooleanProperty("active");

    private PipeRenderProperties() {}
}
