package gregtech.api.statemachine;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

/**
 * Transient operators have access to runtime data storage, but should not have their execution or modifications to
 * serializable data saved after execution. The runtime data should be cleared after a non-transient operator is
 * executed, thus the {@link #of(GTStateMachineOperator)} helper method.
 */
@FunctionalInterface
public interface GTStateMachineTransientOperator {

    void operate(NBTTagCompound data, Map<String, Object> transientData);

    static GTStateMachineTransientOperator emptyOp() {
        return (d, t) -> {};
    }

    static GTStateMachineTransientOperator of(GTStateMachineOperator operator) {
        return (d, t) -> operator.operate(d);
    }
}
