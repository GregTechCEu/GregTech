package gregtech.common.items.gtrmcore;

import gregtech.api.GTValues;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.CoverRoboticArm;
import gregtech.common.items.MetaItems;

import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.covers.CoverBehaviors.registerBehavior;

public class GTRMCoverBehaviors {

    public static void init() {
        registerBehavior(gregtechId("electric.conveyor.ulv"), MetaItems.ELECTRIC_MOTOR_ULV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.ULV, 2));
        registerBehavior(gregtechId("electric.pump.ulv"), MetaItems.ELECTRIC_PUMP_ULV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.ULV, 320));
        registerBehavior(gregtechId("robot.arm.ulv"), MetaItems.ROBOT_ARM_ULV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.ULV, 2));
        registerBehavior(gregtechId("fluid.regulator.ulv"), MetaItems.FLUID_REGULATOR_ULV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.ULV, 16));
    }
}
