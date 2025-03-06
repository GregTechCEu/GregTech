package gregtech.api.statemachine;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Normal operators only have access to serializable data, and this data should be saved after execution.
 */
@FunctionalInterface
public interface GTStateMachineOperator {

    void operate(NBTTagCompound data);

    static GTStateMachineOperator emptyOp() {
        return d -> {};
    }
}
