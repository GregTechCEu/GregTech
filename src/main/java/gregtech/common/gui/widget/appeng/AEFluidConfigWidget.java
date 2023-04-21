package gregtech.common.gui.widget.appeng;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import gregtech.api.gui.Widget;
import gregtech.common.gui.widget.appeng.slot.AEFluidConfigSlot;
import gregtech.common.gui.widget.appeng.slot.AmountSetSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * @Author GlodBlock
 * @Description Display {@link IAEFluidStack} config
 * @Date 2023/4/21-1:45
 */
public class AEFluidConfigWidget extends AEConfigWidget<IAEFluidStack> {

    protected IConfigurableSlot<IAEFluidStack>[] cached;
    protected Int2ObjectMap<IConfigurableSlot<IAEFluidStack>> changeMap = new Int2ObjectOpenHashMap<>();
    public AEFluidConfigWidget(int x, int y, IConfigurableSlot<IAEFluidStack>[] config) {
        super(x, y, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index ++) {
            this.displayList[index] = new MetaTileEntityMEInputHatch.ExportOnlyAETank();
            this.cached[index] = new MetaTileEntityMEInputHatch.ExportOnlyAETank();
            line = index / 8;
            this.addWidget(new AEFluidConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
        this.amountSetWidget = new AmountSetSlot(80, -32, this);
        this.addWidget(this.amountSetWidget);
        this.addWidget(this.amountSetWidget.getText());
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
            if (w instanceof AEFluidConfigSlot) {
                ((AEFluidConfigSlot) w).setSelect(false);
            }
        }
        this.disableAmount();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public IConfigurableSlot<IAEFluidStack> getDisplay(int index) {
        return this.displayList[index];
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.changeMap.clear();
        for (int index = 1; index < this.config.length; index ++) {
            IConfigurableSlot<IAEFluidStack> newSlot = this.config[index];
            IConfigurableSlot<IAEFluidStack> oldSlot = this.cached[index];
            IAEFluidStack nConfig = newSlot.getConfig();
            IAEFluidStack nStock = newSlot.getStock();
            IAEFluidStack oConfig = oldSlot.getConfig();
            IAEFluidStack oStock = oldSlot.getStock();
            if (!areAEStackCountEquals(nConfig, oConfig) || !areAEStackCountEquals(nStock, oStock)) {
                this.changeMap.put(index, newSlot.copy());
                this.cached[index] = this.config[index].copy();
            }
        }
        if (!this.changeMap.isEmpty()) {
            this.writeUpdateInfo(UPDATE_ID, buf -> {
                try {
                    buf.writeVarInt(this.changeMap.size());
                    for (int index : this.changeMap.keySet()) {
                        IAEFluidStack sConfig = this.changeMap.get(index).getConfig();
                        IAEFluidStack sStock = this.changeMap.get(index).getStock();
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
                } catch (IOException ignored) {
                }
            });
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == UPDATE_ID) {
            try {
                int size = buffer.readVarInt();
                for (int i = 0; i < size; i ++) {
                    int index = buffer.readVarInt();
                    IConfigurableSlot<IAEFluidStack> slot = this.displayList[index];
                    if (buffer.readBoolean()) {
                        slot.setConfig(AEFluidStack.fromPacket(buffer));
                    } else {
                        slot.setConfig(null);
                    }
                    if (buffer.readBoolean()) {
                        slot.setStock(AEFluidStack.fromPacket(buffer));
                    } else {
                        slot.setStock(null);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private boolean areAEStackCountEquals(IAEFluidStack s1, IAEFluidStack s2) {
        if (s2 == s1) {
            return true;
        }
        if (s1 != null && s2 != null) {
            return s1.getStackSize() == s2.getStackSize() && s1.equals(s2);
        }
        return false;
    }
}
