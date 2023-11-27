package gregtech.common.metatileentities.multi.multiblockpart.appeng;

/**
 * @Author GlodBlock
 * @Description A slot that can be set to keep requesting.
 * @Date 2023/4/21-0:34
 */
public interface IConfigurableSlot<T> {

    T getConfig();

    T getStock();

    void setConfig(T val);

    void setStock(T val);

    IConfigurableSlot<T> copy();
}
