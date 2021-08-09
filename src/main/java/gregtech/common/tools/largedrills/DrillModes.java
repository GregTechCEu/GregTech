package gregtech.common.tools.largedrills;

public class DrillModes {
    public enum DrillMode implements IDrillMode {
        SINGLE_BLOCK("metaitem.drill.mode.single_block", 1, 2.0f);

        private final String localizationKey;
        private final int cubeSize;
        private final float digSpeedMultiplier;

        DrillMode(String localizationKey, int cubeSize, float digSpeedMultiplier) {
            this.localizationKey = localizationKey;
            this.cubeSize = cubeSize;
            this.digSpeedMultiplier = digSpeedMultiplier;
        }

        public int getCubeSize() {
            return cubeSize;
        }

        public float getDigSpeedMultiplier() {
            return digSpeedMultiplier;
        }

        public String getUnlocalizedName() {
            return localizationKey;
        }

        public static DrillMode getSingleBlock() {
            return SINGLE_BLOCK;
        }
    }

    public enum DrillMVMode implements IDrillMode {
        SINGLE_BLOCK("metaitem.drill.mode.single_block", 1, 4.0f),
        THREE_CUBE("metaitem.drill.mode.three_cube", 3, 1.0f);

        private final String localizationKey;
        private final int cubeSize;
        private final float digSpeedMultiplier;

        DrillMVMode(String localizationKey, int cubeSize, float digSpeedMultiplier) {
            this.localizationKey = localizationKey;
            this.cubeSize = cubeSize;
            this.digSpeedMultiplier = digSpeedMultiplier;
        }

        public int getCubeSize() {
            return cubeSize;
        }

        public float getDigSpeedMultiplier() {
            return digSpeedMultiplier;
        }

        public String getUnlocalizedName() {
            return localizationKey;
        }
    }

    public enum DrillHVMode implements IDrillMode {
        SINGLE_BLOCK("metaitem.drill.mode.single_block", 1, 8.0f),
        THREE_CUBE("metaitem.drill.mode.three_cube", 3, 2.0f),
        FIVE_CUBE("metaitem.drill.mode.five_cube", 5, 1.0f);

        private final String localizationKey;
        private final int cubeSize;
        private final float digSpeedMultiplier;

        DrillHVMode(String localizationKey, int cubeSize, float digSpeedMultiplier) {
            this.localizationKey = localizationKey;
            this.cubeSize = cubeSize;
            this.digSpeedMultiplier = digSpeedMultiplier;
        }

        public int getCubeSize() {
            return cubeSize;
        }

        public float getDigSpeedMultiplier() {
            return digSpeedMultiplier;
        }

        public String getUnlocalizedName() {
            return localizationKey;
        }
    }

    public enum DrillEVMode implements IDrillMode {
        SINGLE_BLOCK("metaitem.drill.mode.single_block", 1, 16.0f),
        THREE_CUBE("metaitem.drill.mode.three_cube", 3, 4.0f),
        FIVE_CUBE("metaitem.drill.mode.five_cube", 5, 2.0f),
        SEVEN_CUBE("metaitem.drill.mode.seven_cube", 7, 1.0f);

        private final String localizationKey;
        private final int cubeSize;
        private final float digSpeedMultiplier;

        DrillEVMode(String localizationKey, int cubeSize, float digSpeedMultiplier) {
            this.localizationKey = localizationKey;
            this.cubeSize = cubeSize;
            this.digSpeedMultiplier = digSpeedMultiplier;
        }

        public int getCubeSize() {
            return cubeSize;
        }

        public float getDigSpeedMultiplier() {
            return digSpeedMultiplier;
        }

        public String getUnlocalizedName() {
            return localizationKey;
        }
    }

    public enum DrillIVMode implements IDrillMode {
        SINGLE_BLOCK("metaitem.drill.mode.single_block", 1, 32.0f),
        THREE_CUBE("metaitem.drill.mode.three_cube", 3, 8.0f),
        FIVE_CUBE("metaitem.drill.mode.five_cube", 5, 4.0f),
        SEVEN_CUBE("metaitem.drill.mode.seven_cube", 7, 2.0f),
        NINE_CUBE("metaitem.drill.mode.nine_cube", 9, 1.0f);

        private final String localizationKey;
        private final int cubeSize;
        private final float digSpeedMultiplier;

        DrillIVMode(String localizationKey, int cubeSize, float digSpeedMultiplier) {
            this.localizationKey = localizationKey;
            this.cubeSize = cubeSize;
            this.digSpeedMultiplier = digSpeedMultiplier;
        }

        public int getCubeSize() {
            return cubeSize;
        }

        public float getDigSpeedMultiplier() {
            return digSpeedMultiplier;
        }

        public String getUnlocalizedName() {
            return localizationKey;
        }
    }

}
