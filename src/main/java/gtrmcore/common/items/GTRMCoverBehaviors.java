package gtrmcore.common.items;

import gregtech.api.GTValues;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.CoverRoboticArm;

import static gregtech.common.covers.CoverBehaviors.registerBehavior;
import static gtrmcore.api.util.GTRMUtility.gtrmId;

public class GTRMCoverBehaviors {

    public static void init() {
        registerBehavior(gtrmId("electric.conveyor.ulv"), GTRMMetaItems.ELECTRIC_MOTOR_ULV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.ULV, 2));
        registerBehavior(gtrmId("electric.pump.ulv"), GTRMMetaItems.ELECTRIC_PUMP_ULV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.ULV, 320));
        registerBehavior(gtrmId("robot.arm.ulv"), GTRMMetaItems.ROBOT_ARM_ULV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.ULV, 2));
        registerBehavior(gtrmId("fluid.regulator.ulv"), GTRMMetaItems.FLUID_REGULATOR_ULV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.ULV, 16));
    }
}
