package gregtech.api.util;

import org.lwjgl.util.vector.Vector2f;

import static net.minecraft.util.math.MathHelper.clamp;

// Huge thanks to https://noonat.github.io/intersect!
public class TwoDimensionalRayTracer {
    public static class TwoDimensionalRayTraceResult {
        public Vector2f pos = new Vector2f();
        public Vector2f delta = new Vector2f();
        public Vector2f normal = new Vector2f();
        public float time = -1;
    }

    /**
     * Detects the intersection between a segment and a box, if there is any.
     * @param pos The original position of the point.
     * @param delta The proposed new position of the point.
     * @param boxSize The half-width and half-height of the box
     * @return
     */
    public static TwoDimensionalRayTraceResult intersectBoxSegment(Vector2f pos, Vector2f delta, Vector2f boxCenter, Vector2f boxSize) {
        float scaleX = (float) (1.0 / delta.x);
        float scaleY = (float) (1.0 / delta.y);
        float signX = Math.signum(scaleX);
        float signY = Math.signum(scaleY);
        float nearTimeX = (boxCenter.x - signX * (boxSize.x) - pos.x) * scaleX;
        float nearTimeY = (boxCenter.y - signY * (boxSize.y) - pos.y) * scaleY;
        float farTimeX = (boxCenter.x + signX * (boxSize.x) - pos.x) * scaleX;
        float farTimeY = (boxCenter.y + signY * (boxSize.y) - pos.y) * scaleY;

        if (nearTimeX > farTimeY || nearTimeY > farTimeX) {
            return null;
        }

        double nearTime = Math.max(nearTimeX, nearTimeY);
        double farTime = Math.min(farTimeX, farTimeY);

        if (nearTime >= 1 || farTime <= 0) {
            return null;
        }

        TwoDimensionalRayTraceResult result = new TwoDimensionalRayTraceResult();

        result.time = (float) clamp(nearTime, 0, 1);
        if (nearTimeX > nearTimeY) {
            result.normal.x = -signX;
            result.normal.y = 0;
        } else {
            result.normal.x = 0;
            result.normal.y = -signY;
        }
        result.delta.x = (float) ((1.0 - result.time) * -delta.x);
        result.delta.y = (float) ((1.0 - result.time) * -delta.y);
        result.pos.x = pos.x + delta.x * result.time;
        result.pos.y = pos.y + delta.y * result.time;
        return result;
    }
}
