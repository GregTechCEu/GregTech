package gregtech.client.renderer.pipe.cover;

import gregtech.api.cover.CoverUtil;
import gregtech.client.renderer.pipe.quad.QuadHelper;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;

import javax.vecmath.Vector3f;

public class CoverRendererValues {

    private static final float OVERLAY_DIST_1 = 0.003f;
    private static final float OVERLAY_DIST_2 = 0.006f;

    public static final EnumMap<EnumFacing, AxisAlignedBB> PLATE_AABBS = new EnumMap<>(EnumFacing.class);
    static final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> PLATE_BOXES = new EnumMap<>(EnumFacing.class);
    static final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> OVERLAY_BOXES_1 = new EnumMap<>(EnumFacing.class);
    static final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> OVERLAY_BOXES_2 = new EnumMap<>(EnumFacing.class);

    static {
        for (EnumFacing facing : EnumFacing.VALUES) {
            PLATE_AABBS.put(facing, CoverUtil.getCoverPlateBox(facing, 1d / 16).aabb());
        }
        for (var value : PLATE_AABBS.entrySet()) {
            // make sure that plates render slightly below any normal block quad
            PLATE_BOXES.put(value.getKey(), QuadHelper.fullOverlay(value.getKey(), value.getValue(), -OVERLAY_DIST_1));
            OVERLAY_BOXES_1.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue(), OVERLAY_DIST_1));
            OVERLAY_BOXES_2.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue(), OVERLAY_DIST_2));
        }
    }
}
