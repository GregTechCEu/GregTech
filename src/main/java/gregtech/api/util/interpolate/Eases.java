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
            if ((t /= 0.5f) < 1) {
                return 0.5f * t * t;
            }
            return -0.5f * ((--t) * (t - 2) - 1);
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
