package gregtech.api.recipes.logic.statemachine.parallel;

import gregtech.api.statemachine.GTStateMachineTransientOperator;

public interface ParallelLimitOperatorFactory {

    GTStateMachineTransientOperator produce(boolean canDownTransform);
}
