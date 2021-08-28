package gregtech.api.terminal.hardware;

import gregtech.api.capability.impl.ElectricItem;
import net.minecraft.item.ItemStack;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/28
 * @Description:
 */
public class BatteryHardware extends ElectricItem implements IHardware {

    public BatteryHardware() {
        this(null);
    }

    public BatteryHardware(ItemStack itemStack) {
        super(itemStack, 0, 0, true, false);
    }

    public BatteryHardware(int tier, int cost) {
        super(null, cost, tier, false, false);
    }

    @Override
    public boolean isHardwareAdequate(IHardware demand) {
        return demand instanceof BatteryHardware && ((BatteryHardware) demand).tier < this.tier;
    }

    @Override
    public String getRegistryName() {
        return "battery";
    }

    @Override
    public IHardware createHardware(ItemStack itemStack) {
        return new BatteryHardware(itemStack);
    }
}
