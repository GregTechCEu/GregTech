package gregtech.common.tools.largedrills;

import gregtech.common.items.behaviors.ModeSwitchBehavior;
import static gregtech.common.tools.largedrills.DrillModes.*;


//If you want to add more electric drills to GT, modify this class, and add a DrillMode in DrillModes.
public class ToolDrills {
    public static class ToolDrillLV extends ToolDrillLarge<DrillMode> {
        public final ModeSwitchBehavior<DrillMode> MODE_SWITCH_BEHAVIOR = new ModeSwitchBehavior<>(DrillMode.class);
        
        @Override
        ModeSwitchBehavior<DrillMode> getModeSwitchBehavior() {
            return MODE_SWITCH_BEHAVIOR;
        }

        @Override
        int getTier() {
            return 1;
        }
    }

    public static class ToolDrillMV extends ToolDrillLarge<DrillMVMode> {
        public final ModeSwitchBehavior<DrillMVMode> MODE_SWITCH_BEHAVIOR = new ModeSwitchBehavior<>(DrillMVMode.class);

        @Override
        ModeSwitchBehavior<DrillMVMode> getModeSwitchBehavior() {
            return MODE_SWITCH_BEHAVIOR;
        }

        @Override
        int getTier() {
            return 2;
        }
    }

    public static class ToolDrillHV extends ToolDrillLarge<DrillHVMode> {
        public final ModeSwitchBehavior<DrillHVMode> MODE_SWITCH_BEHAVIOR = new ModeSwitchBehavior<>(DrillHVMode.class);

        @Override
        ModeSwitchBehavior<DrillHVMode> getModeSwitchBehavior() {
            return MODE_SWITCH_BEHAVIOR;
        }

        @Override
        int getTier() {
            return 3;
        }
    }

    public static class ToolDrillEV extends ToolDrillLarge<DrillEVMode> {
        public final ModeSwitchBehavior<DrillEVMode> MODE_SWITCH_BEHAVIOR = new ModeSwitchBehavior<>(DrillEVMode.class);

        @Override
        ModeSwitchBehavior<DrillEVMode> getModeSwitchBehavior() {
            return MODE_SWITCH_BEHAVIOR;
        }

        @Override
        int getTier() {
            return 4;
        }
    }

    public static class ToolDrillIV extends ToolDrillLarge<DrillIVMode> {
        public final ModeSwitchBehavior<DrillIVMode> MODE_SWITCH_BEHAVIOR = new ModeSwitchBehavior<>(DrillIVMode.class);

        @Override
        ModeSwitchBehavior<DrillIVMode> getModeSwitchBehavior() {
            return MODE_SWITCH_BEHAVIOR;
        }

        @Override
        int getTier() {
            return 5;
        }
    }
}
