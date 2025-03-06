package gregtech.api.recipes.logic.statemachine.parallel;

import gregtech.api.statemachine.GTStateMachineTransientOperator;

import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public interface ParallelLimitOperatorFactory {

    GTStateMachineTransientOperator produce(@NotNull IntSupplier limit, boolean canDownTransform);
}
