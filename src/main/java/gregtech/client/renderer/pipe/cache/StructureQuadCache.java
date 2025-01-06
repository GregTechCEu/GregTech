package gregtech.client.renderer.pipe.cache;

import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class StructureQuadCache {

    public static final float OVERLAY_DIST_1 = 0.003f;
    public static final float OVERLAY_DIST_2 = 0.006f;
    public static final float OVERLAY_DIST_3 = 0.009f;
    public static final float OVERLAY_DIST_4 = 0.012f;
    public static final float OVERLAY_DIST_5 = 0.015f;

    protected final PipeQuadHelper helper;

    protected ColorQuadCache cache;

    protected StructureQuadCache(PipeQuadHelper helper) {
        this.helper = helper;
    }

    protected void buildPrototype() {
        this.cache = new ColorQuadCache(this.buildPrototypeInternal());
    }

    protected abstract List<RecolorableBakedQuad> buildPrototypeInternal();

    public abstract void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask,
                                   ColorData data, byte coverMask);
}
