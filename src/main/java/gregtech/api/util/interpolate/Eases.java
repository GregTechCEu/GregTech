package gregtech.api.util.interpolate;

import org.jetbrains.annotations.ApiStatus;

public enum Eases implements IEase {

    LINEAR {

        @Override
        public float getInterpolation(float t) {
            return t;
        }
    },
    QUAD_IN {

        @Override
        public float getInterpolation(float t) {
            return t * t;
        }
    },
    QUAD_IN_OUT {

        @Override
        public float getInterpolation(float t) {
            if (t <= 0.5f) return 2f * t * t;
            t = -2f * t + 2f;
            return 1f - t * t * 0.5f;
        }
    },
    QUAD_OUT {

        @Override
        public float getInterpolation(float t) {
            return -t * (t - 2);
        }
    };

    // Deprecated names below - will be removed on future update

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static final Eases EaseLinear = LINEAR;
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static final Eases EaseQuadIn = QUAD_IN;
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static final Eases EaseQuadInOut = QUAD_IN_OUT;
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static final Eases EaseQuadOut = QUAD_OUT;
}
