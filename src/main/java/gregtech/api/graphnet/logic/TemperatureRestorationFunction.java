package gregtech.api.graphnet.logic;

/**
 * A bunch of loss functions. By the power of Wolfram Alpha.
 * <a href="https://www.desmos.com/calculator/vjuksr3ut0">Demonstration Graph</a>
 */
public enum TemperatureRestorationFunction {
    // DO NOT REORDER FUNCTIONS, THE ORDER IS USED FOR NBT SERIALIZATION
    /**
     * 100 thermal energy is lost every tick, modified by restoration speed factor.
     * <br> A constant rate.
     */
    ARITHMETIC {
        @Override
        public float restoreTemperature(float thermalEnergy, float restorationSpeedFactor, int timePassed) {
            float initialThermalEnergy = thermalEnergy;
            thermalEnergy -= b(thermalEnergy, restorationSpeedFactor);
            if (thermalEnergy < initialThermalEnergy) return 0;
            return tolerate(thermalEnergy);
        }
    },
    /**
     * 10% of thermal energy is lost every tick, modified by restoration speed factor.
     * <br> Faster than {@link TemperatureRestorationFunction#ARITHMETIC} at large values, but slower at small values.
     */
    GEOMETRIC {
        @Override
        public float restoreTemperature(float thermalEnergy, float restorationSpeedFactor, int timePassed) {
            thermalEnergy *= Math.pow(a(restorationSpeedFactor), timePassed);
            return tolerate(thermalEnergy);
        }
    },
    /**
     * thermal energy is raised to the power of 1 - 0.02 every tick, modified by restoration speed factor.
     * <br> Faster than {@link TemperatureRestorationFunction#GEOMETRIC} at large values, but incredibly slow at small values.
     */
    POWER {
        @Override
        public float restoreTemperature(float thermalEnergy, float restorationSpeedFactor, int timePassed) {
            thermalEnergy = (float) (Math.signum(thermalEnergy) *
                    Math.pow(Math.abs(thermalEnergy), Math.pow(c(restorationSpeedFactor), timePassed)));
            return tolerate(thermalEnergy);
        }
    },
    /**
     * 10% of thermal energy is lost, then 100 more, every tick, both values modified by restoration speed factor.
     * <br> Slightly faster than {@link TemperatureRestorationFunction#GEOMETRIC} at large values,
     * slightly faster than {@link TemperatureRestorationFunction#ARITHMETIC} at small values.
     */
    GEOMETRIC_ARITHMETIC {
        @Override
        public float restoreTemperature(float thermalEnergy, float restorationSpeedFactor, int timePassed) {
            float initialThermalEnergy = thermalEnergy;

            float a = a(restorationSpeedFactor);
            float b = b(thermalEnergy, restorationSpeedFactor);
            thermalEnergy = (float) ((b - Math.pow(a, timePassed) *
                    (-a * thermalEnergy + b + thermalEnergy)) / (a - 1));

            if (thermalEnergy < initialThermalEnergy) return 0;
            return tolerate(thermalEnergy);
        }
    },
    /**
     * thermal energy is raised to the power of 1 - 0.02, then 10% more is lost, every tick, both values modified by restoration speed factor.
     * <br> Slightly faster than {@link TemperatureRestorationFunction#POWER} at large values,
     * slightly faster than {@link TemperatureRestorationFunction#GEOMETRIC} at small values.
     */
    POWER_GEOMETRIC {
        @Override
        public float restoreTemperature(float thermalEnergy, float restorationSpeedFactor, int timePassed) {
            float c = c(restorationSpeedFactor);
            thermalEnergy = (float) (Math.pow(a(restorationSpeedFactor), (Math.pow(c, timePassed) - 1) / (c - 1)) *
                    Math.pow(Math.abs(thermalEnergy), Math.pow(c, timePassed)) * Math.signum(thermalEnergy));
            return tolerate(thermalEnergy);
        }
    };

    public static final float TOLERANCE = 0.1f;

    protected float tolerate(float value) {
        return Math.abs(value) < TOLERANCE ? 0 : value;
    }

    protected float a(float restorationSpeedFactor) {
        return 1 - 0.1f * restorationSpeedFactor;
    }

    protected float b(float thermalEnergy, float restorationSpeedFactor) {
        return Math.signum(thermalEnergy) * 100 * restorationSpeedFactor;
    }

    protected float c(float restorationSpeedFactor) {
        return 1 - 0.02f * restorationSpeedFactor;
    }

    public abstract float restoreTemperature(float thermalEnergy, float restorationSpeedFactor, int timePassed);
}
