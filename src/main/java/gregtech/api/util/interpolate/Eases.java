package gregtech.api.util.interpolate;

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
    public static final Eases EaseLinear = LINEAR;
    @Deprecated
    public static final Eases EaseQuadIn = QUAD_IN;
    @Deprecated
    public static final Eases EaseQuadInOut = QUAD_IN_OUT;
    @Deprecated
    public static final Eases EaseQuadOut = QUAD_OUT;
}
