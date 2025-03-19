package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

public interface IConfigurableSlot<T> {

    T getConfig();

    T getStock();

    void setConfig(T val);

    void setStock(T val);

    IConfigurableSlot<T> copy();
}
