package gregtech.api.terminal;


import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.util.GTLog;
import gregtech.common.terminal.hardware.BatteryHardware;
import gregtech.common.terminal.hardware.DeviceHardware;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@ZenClass("mods.gregtech.TerminalRegistry")
@ZenRegister
@SuppressWarnings("unused")
public class CTTerminalRegistry {
    private static List<CTAppRegistryBuilder> ctAppRegistryBuilders = new LinkedList<>();

    @ZenMethod
    public static CTAppRegistryBuilder createAppRegistryBuilder(String appName) {
        return new CTAppRegistryBuilder(appName);
    }

    @ZenMethod
    public static void registerDevice(IItemStack device, String name) {
        ItemStack itemStack = CraftTweakerMC.getItemStack(device).copy();
        if (!itemStack.isEmpty()) {
            itemStack.setCount(1);
            EnumHelper.addEnum(DeviceHardware.DEVICE.class, name.toUpperCase(), new Class[]{ItemStack.class, String.class}, itemStack, name.toLowerCase());
        }
    }

    public static void registerAPP(CTAppRegistryBuilder builder) {
        if (TerminalRegistry.APP_REGISTER.containsKey(builder.appName)) {
            if (builder.isDefaultApp != null) {
                TerminalRegistry.DEFAULT_APPS.remove(builder.appName);
                if (builder.isDefaultApp) {
                    TerminalRegistry.DEFAULT_APPS.add(builder.appName);
                }
            }
            if (!builder.hardware.isEmpty()) {
                int maxTier = TerminalRegistry.APP_HW_DEMAND.get(builder.appName).length;
                List<Hardware>[] hardware = new List[maxTier];
                for (int i = 0; i < maxTier; i++) {
                    List<Hardware> list = new LinkedList<>();
                    if (builder.battery.containsKey(i)) {
                        list.add(builder.battery.get(i));
                    } else if (builder.battery.containsKey(-1)) {
                        list.add(builder.battery.get(-1));
                    }
                    if (builder.hardware.containsKey(i)) {
                        list.addAll(builder.hardware.get(i));
                    } else if (builder.hardware.containsKey(-1)) {
                        list.addAll(builder.hardware.get(-1));
                    }
                    if (list.size() > 0) {
                        hardware[i] = list;
                    }
                }
                TerminalRegistry.APP_HW_DEMAND.put(builder.appName, hardware);
            }
            if (!builder.upgrade.isEmpty()) {
                int maxTier = TerminalRegistry.APP_UPGRADE_CONDITIONS.get(builder.appName).length;
                List<ItemStack>[] upgrade = new List[maxTier];
                if (builder.upgrade.containsKey(-1)) { // register for all tier
                    Arrays.fill(upgrade, builder.upgrade.get(-1));
                }
                builder.upgrade.forEach((tier, listBuilder) -> { // register for specific tier
                    if (tier != -1 && tier < maxTier) {
                        upgrade[tier] = listBuilder;
                    }
                });
                TerminalRegistry.APP_UPGRADE_CONDITIONS.put(builder.appName, upgrade);
            }
        } else {
            GTLog.logger.error("Not found the app {}, while load the CT script", builder.appName);
        }
    }

    public static void register() {
        ctAppRegistryBuilders.forEach(CTTerminalRegistry::registerAPP);
        ctAppRegistryBuilders = null;
    }

    @ZenClass("mods.gregtech.AppRegistryBuilder")
    @ZenRegister
    public static class CTAppRegistryBuilder {
        final String appName;
        Boolean isDefaultApp;
        Map<Integer, BatteryHardware> battery;
        Map<Integer, List<Hardware>> hardware;
        Map<Integer, List<ItemStack>> upgrade;

        public CTAppRegistryBuilder(String appName) {
            this.appName = appName;
            this.battery = new HashMap<>();
            this.hardware = new HashMap<>();
            this.upgrade = new HashMap<>();
        }

        @ZenMethod
        public CTAppRegistryBuilder isDefaultApp(boolean isDefaultApp) {
            this.isDefaultApp = isDefaultApp;
            return this;
        }

        @ZenMethod
        public CTAppRegistryBuilder battery(int batteryTier, long cost) {
            battery.put(-1, new BatteryHardware.BatteryDemand(batteryTier, cost));
            return this;
        }

        @ZenMethod
        public CTAppRegistryBuilder battery(int tier, int batteryTier, long cost) {
            battery.put(tier, new BatteryHardware.BatteryDemand(batteryTier, cost));
            return this;
        }

        @ZenMethod
        public CTAppRegistryBuilder device(String... device) {
            Hardware[] hw = Arrays.stream(device).map(DeviceHardware.DeviceDemand::new).filter(deviceDemand -> deviceDemand.getDevice() != null).toArray(Hardware[]::new);
            hardware(hw);
            return this;
        }

        @ZenMethod
        public CTAppRegistryBuilder device(int tier, String... device) {
            this.hardware(tier, Arrays.stream(device).map(DeviceHardware.DeviceDemand::new).filter(deviceDemand -> deviceDemand.getDevice() != null).toArray(Hardware[]::new));
            return this;
        }

        private void hardware(Hardware... hardware) {
            hardware(-1, hardware);
        }

        private void hardware(int tier, Hardware... hardware) {
            this.hardware.put(tier, new LinkedList<>());
            for (Hardware hw : hardware) {
                this.hardware.get(tier).add(hw);
            }
        }

        @ZenMethod
        public CTAppRegistryBuilder upgrade(IItemStack... upgrades) {
            upgrade(-1, upgrades);
            return this;
        }

        @ZenMethod
        public CTAppRegistryBuilder upgrade(int tier, IItemStack... upgrades) {
            this.upgrade.put(tier, new LinkedList<>());
            for (ItemStack up : Arrays.stream(upgrades).map(CraftTweakerMC::getItemStack).filter(itemStack -> !itemStack.isEmpty()).toArray(ItemStack[]::new)) {
                this.upgrade.get(tier).add(up);
            }
            return this;
        }

        @ZenMethod
        public void build() {
            if (ctAppRegistryBuilders == null) {
                registerAPP(this);
            } else {
                ctAppRegistryBuilders.add(this);
            }
        }
    }
}
