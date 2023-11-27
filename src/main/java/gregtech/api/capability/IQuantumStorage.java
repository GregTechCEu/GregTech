package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

public interface IQuantumStorage<T> {

    Type getType();

    void setConnected(IQuantumController controller);

    void setDisconnected();

    BlockPos getControllerPos();

    @Nullable
    IQuantumController getController();

    BlockPos getPos();

    boolean isConnected();

    T getTypeValue();

    enum Type {
        ITEM,
        FLUID,
        EXTENDER,
        NONE
    }
}
