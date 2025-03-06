package gregtech.api.recipes.logic.statemachine.overclock;

import gregtech.api.statemachine.GTStateMachineTransientOperator;

import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;

public interface OverclockingOperatorFactory {

    GTStateMachineTransientOperator produce(double costFactor, double speedFactor, boolean canUpTransform,
                                            @Nullable DoubleSupplier durationDiscount);
}
