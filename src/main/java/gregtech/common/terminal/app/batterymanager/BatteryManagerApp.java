package gregtech.common.terminal.app.batterymanager;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.items.MetaItems;
import gregtech.common.terminal.hardware.BatteryHardware;
import net.minecraft.client.resources.I18n;

import java.util.concurrent.atomic.AtomicInteger;

public class BatteryManagerApp extends AbstractApplication {
    public BatteryManagerApp() {
        super("battery");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(MetaItems.BATTERY_HV_SODIUM.getStackForm());
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    @Override
    public AbstractApplication initApp() {
        if (isClient) {
            this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            this.addWidget(new ImageWidget(170, 15, 1, 232 - 30, TerminalTheme.COLOR_7));
            this.addWidget(new BatteryWidget(10, 10 + (212 - 150) / 2, 150, 150, getOs()));
            addBatteryApps();
        }
        return this;
    }

    private void addBatteryApps() {
        AtomicInteger index = new AtomicInteger();
        for (AbstractApplication installed : getOs().installedApps) {
            TerminalRegistry.getAppHardwareDemand(installed.getRegistryName(), getOs().tabletNBT.getCompoundTag(installed.getRegistryName()).getInteger("_tier")).stream()
                    .filter(i->i instanceof BatteryHardware).findFirst()
                    .ifPresent(battery-> {
                        long charge = ((BatteryHardware)battery).getCharge();
                        this.addWidget(new RectButtonWidget(180 + (index.get() % 5) * 30, 15 + (index.get() / 5) * 30, 20, 20, 2)
                                .setIcon(installed.getIcon())
                                // warn unsafe call the I18n here.
                                .setHoverText(I18n.format("terminal.battery.hover", I18n.format(installed.getUnlocalizedName()), charge))
                                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0));
                        index.getAndIncrement();
                    });
        }
    }
}
