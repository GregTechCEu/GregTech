package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IConfigurableSlot<T> {

    @Nullable
    T getConfig();

    @Nullable
    T getStock();

    void setConfig(T val);

    void setStock(T val);

    @NotNull
    IConfigurableSlot<T> copy();
}
