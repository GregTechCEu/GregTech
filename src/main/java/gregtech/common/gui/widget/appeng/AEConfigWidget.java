package gregtech.common.gui.widget.appeng;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.gui.widget.appeng.slot.AEConfigSlot;
import gregtech.common.gui.widget.appeng.slot.AmountSetSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.IOException;

/**
 * @Author GlodBlock
 * @Description Display the config like ME Interface
 * @Date 2023/4/21-0:27
 */
public abstract class AEConfigWidget<T extends IAEStack<T>> extends AbstractWidgetGroup {

    protected final IConfigurableSlot<T>[] config;
    protected IConfigurableSlot<T>[] cached;
    protected Int2ObjectMap<IConfigurableSlot<T>> changeMap = new Int2ObjectOpenHashMap<>();
    protected IConfigurableSlot<T>[] displayList;
    protected AmountSetSlot<T> amountSetWidget;
    protected final static int UPDATE_ID = 1000;

    public AEConfigWidget(int x, int y, IConfigurableSlot<T>[] config) {
        super(new Position(x, y), new Size(config.length * 18, 18 * 2));
        this.config = config;
        this.init();
        this.amountSetWidget = new AmountSetSlot<>(80, -40, this);
        this.addWidget(this.amountSetWidget);
        this.addWidget(this.amountSetWidget.getText());
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getText().setVisible(false);
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

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.amountSetWidget.isVisible()) {
            if (this.amountSetWidget.getText().mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        for (Widget w : this.widgets) {
            if (w instanceof AEConfigSlot<?>) {
                ((AEConfigSlot<?>) w).setSelect(false);
            }
        }
        this.disableAmount();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract void init();

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.changeMap.clear();
        for (int index = 0; index < this.config.length; index++) {
            IConfigurableSlot<T> newSlot = this.config[index];
            IConfigurableSlot<T> oldSlot = this.cached[index];
            T nConfig = newSlot.getConfig();
            T nStock = newSlot.getStock();
            T oConfig = oldSlot.getConfig();
            T oStock = oldSlot.getStock();
            if (!areAEStackCountEquals(nConfig, oConfig) || !areAEStackCountEquals(nStock, oStock)) {
                this.changeMap.put(index, newSlot.copy());
                this.cached[index] = this.config[index].copy();
                this.gui.holder.markAsDirty();
            }
        }
        if (!this.changeMap.isEmpty()) {
            this.writeUpdateInfo(UPDATE_ID, buf -> {
                try {
                    buf.writeVarInt(this.changeMap.size());
                    for (int index : this.changeMap.keySet()) {
                        T sConfig = this.changeMap.get(index).getConfig();
                        T sStock = this.changeMap.get(index).getStock();
                        buf.writeVarInt(index);
                        if (sConfig != null) {
                            buf.writeBoolean(true);
                            sConfig.writeToPacket(buf);
                        } else {
                            buf.writeBoolean(false);
                        }
                        if (sStock != null) {
                            buf.writeBoolean(true);
                            sStock.writeToPacket(buf);
                        } else {
                            buf.writeBoolean(false);
                        }
                    }
                } catch (IOException ignored) {}
            });
        }
    }

    public final IConfigurableSlot<T> getConfig(int index) {
        return this.config[index];
    }

    public final IConfigurableSlot<T> getDisplay(int index) {
        return this.displayList[index];
    }

    protected final boolean areAEStackCountEquals(T s1, T s2) {
        if (s2 == s1) {
            return true;
        }
        if (s1 != null && s2 != null) {
            return s1.getStackSize() == s2.getStackSize() && s1.equals(s2);
        }
        return false;
    }
}
