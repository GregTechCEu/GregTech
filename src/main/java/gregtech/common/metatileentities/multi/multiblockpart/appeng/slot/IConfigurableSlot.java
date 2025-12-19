package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import appeng.api.storage.data.IAEStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IConfigurableSlot<T extends IAEStack<T>> {

    @Nullable
    T getConfig();

    @Nullable
    T getStock();

    void setConfig(@Nullable T val);

    void setStock(@Nullable T val);

    @NotNull
    IConfigurableSlot<T> copy();
}
