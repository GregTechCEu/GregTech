package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;

public interface IQuantumStorage<T> {

    Type getType();

    void setConnected(IQuantumController controller);

    void setDisconnected();

    BlockPos getControllerPos();

    IQuantumController getController();

    BlockPos getPos();

    boolean isConnected();

    T getTypeValue();

    enum Type {
        ITEM, FLUID, ENERGY
    }
}
