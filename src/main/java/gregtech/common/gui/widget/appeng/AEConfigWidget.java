package gregtech.common.gui.widget.appeng;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.gui.widget.appeng.slot.AmountSetSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;

/**
 * @Author GlodBlock
 * @Description Display the config like ME Interface
 * @Date 2023/4/21-0:27
 */
public abstract class AEConfigWidget<T extends IAEStack<T>> extends AbstractWidgetGroup {

    protected final IConfigurableSlot<T>[] config;
    protected IConfigurableSlot<IAEFluidStack>[] displayList;
    protected AmountSetSlot amountSetWidget;
    protected final static int UPDATE_ID = 1000;

    public AEConfigWidget(int x, int y, IConfigurableSlot<T>[] config) {
        super(new Position(x, y), new Size(config.length * 18, 18 * 2));
        this.config = config;
        this.init();
    }

    public void enableAmount(int slotIndex) {
        this.amountSetWidget.setSlotIndex(slotIndex);
        this.amountSetWidget.setVisible(true);
        this.amountSetWidget.getText().setVisible(true);
    }

    public void disableAmount() {
        this.amountSetWidget.setSlotIndex(-1);
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getText().setVisible(false);
    }

    abstract void init();

    public IConfigurableSlot<T> getConfig(int index) {
        return this.config[index];
    }

    abstract public IConfigurableSlot<T> getDisplay(int index);

}
