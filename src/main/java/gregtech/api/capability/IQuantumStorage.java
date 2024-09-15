package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

public interface IQuantumStorage<T> {

    Type getType();

    void setConnected(IQuantumController controller);

    void setDisconnected();

    BlockPos getControllerPos();

    @Nullable
    IQuantumController getQuantumController();

    BlockPos getPos();

    default boolean isConnected() {
        // use controllerPos here because it is synced
        // on both sides, where controller is not
        return getControllerPos() != null;
    }

    T getTypeValue();

    enum Type {
        ITEM,
        FLUID,
        EXTENDER,
        PROXY,
        ENERGY
    }
}
