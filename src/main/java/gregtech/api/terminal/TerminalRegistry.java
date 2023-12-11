package gregtech.api.terminal;

import gregtech.api.GTValues;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.terminal.app.VirtualTankApp;
import gregtech.common.terminal.app.appstore.AppStoreApp;
import gregtech.common.terminal.app.batterymanager.BatteryManagerApp;
import gregtech.common.terminal.app.capeselector.CapeSelectorApp;
import gregtech.common.terminal.app.console.ConsoleApp;
import gregtech.common.terminal.app.game.maze.MazeApp;
import gregtech.common.terminal.app.game.minesweeper.MinesweeperApp;
import gregtech.common.terminal.app.game.pong.PongApp;
import gregtech.common.terminal.app.guide.ItemGuideApp;
import gregtech.common.terminal.app.guide.MultiBlockGuideApp;
import gregtech.common.terminal.app.guide.SimpleMachineGuideApp;
import gregtech.common.terminal.app.guide.TutorialGuideApp;
import gregtech.common.terminal.app.guideeditor.GuideEditorApp;
import gregtech.common.terminal.app.hardwaremanager.HardwareManagerApp;
import gregtech.common.terminal.app.multiblockhelper.MultiBlockPreviewARApp;
import gregtech.common.terminal.app.prospector.ProspectorApp;
import gregtech.common.terminal.app.prospector.ProspectorMode;
import gregtech.common.terminal.app.recipechart.RecipeChartApp;
import gregtech.common.terminal.app.settings.SettingsApp;
import gregtech.common.terminal.app.teleport.TeleportApp;
import gregtech.common.terminal.app.worldprospector.WorldProspectorARApp;
import gregtech.common.terminal.hardware.BatteryHardware;
import gregtech.common.terminal.hardware.DeviceHardware;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TerminalRegistry {

    public static final Map<String, AbstractApplication> APP_REGISTER = new LinkedHashMap<>();
    public static final Map<String, Hardware> HW_REGISTER = new LinkedHashMap<>();
    public static final Map<String, List<Hardware>[]> APP_HW_DEMAND = new HashMap<>();
    public static final Map<String, List<ItemStack>[]> APP_UPGRADE_CONDITIONS = new HashMap<>();
    public static final List<String> DEFAULT_APPS = new ArrayList<>();
    @SideOnly(Side.CLIENT)
    public static File TERMINAL_PATH;

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            TERMINAL_PATH = new File(Loader.instance().getConfigDir(), ConfigHolder.client.terminalRootPath);
        }
    }

    public static void init() {
        // register hardware
        registerHardware(new BatteryHardware());
        int deviceSize = DeviceHardware.DEVICE.values().length;
        for (int i = 1; i < deviceSize; i++) {
            registerHardware(new DeviceHardware(i));
        }
        // register applications
        AppRegistryBuilder.create(new SimpleMachineGuideApp()).defaultApp().build();
        AppRegistryBuilder.create(new MultiBlockGuideApp()).defaultApp().build();
        AppRegistryBuilder.create(new ItemGuideApp()).defaultApp().build();
        AppRegistryBuilder.create(new TutorialGuideApp()).defaultApp().build();
        AppRegistryBuilder.create(new GuideEditorApp()).defaultApp().build();
        AppRegistryBuilder.create(new SettingsApp()).defaultApp().build();
        AppRegistryBuilder.create(new CapeSelectorApp()).defaultApp().build();

        AppRegistryBuilder.create(new TeleportApp())
                .battery(GTValues.ZPM, 10000)
                .device(DeviceHardware.DEVICE.FIELD_GENERATOR_UV)
                .build();

        AppRegistryBuilder.create(new PongApp())
                .battery(GTValues.LV, 75)
                .build();
        AppRegistryBuilder.create(new MazeApp())
                .battery(GTValues.LV, 150)
                .build();
        AppRegistryBuilder.create(new MinesweeperApp())
                .battery(GTValues.LV, 150)
                .build();

        AppRegistryBuilder.create(new ProspectorApp(ProspectorMode.ORE))
                .battery(0, GTValues.LV, 640)
                .battery(1, GTValues.LV, 640)
                .battery(2, GTValues.MV, 1000)
                .battery(3, GTValues.HV, 1500)
                .battery(4, GTValues.HV, 1500)
                .upgrade(0, MetaItems.SENSOR_LV.getStackForm(1))
                .upgrade(1, MetaItems.SENSOR_HV.getStackForm(1))
                .upgrade(2, MetaItems.SENSOR_EV.getStackForm(1))
                .upgrade(3, MetaItems.SENSOR_IV.getStackForm(1))
                .upgrade(4, MetaItems.SENSOR_LuV.getStackForm(1))
                .device(0, DeviceHardware.DEVICE.PROSPECTOR_LV)
                .device(1, DeviceHardware.DEVICE.PROSPECTOR_LV)
                .device(2, DeviceHardware.DEVICE.PROSPECTOR_LV)
                .device(3, DeviceHardware.DEVICE.PROSPECTOR_HV)
                .device(4, DeviceHardware.DEVICE.PROSPECTOR_HV)
                .build();

        AppRegistryBuilder.create(new ProspectorApp(ProspectorMode.FLUID))
                .battery(0, GTValues.MV, 1000)
                .battery(1, GTValues.MV, 1000)
                .battery(2, GTValues.HV, 1500)
                .battery(3, GTValues.HV, 1500)
                .battery(4, GTValues.HV, 1500)
                .upgrade(0, MetaItems.SENSOR_HV.getStackForm(1))
                .upgrade(1, MetaItems.SENSOR_HV.getStackForm(3))
                .upgrade(2, MetaItems.SENSOR_EV.getStackForm(1))
                .upgrade(3, MetaItems.SENSOR_IV.getStackForm(1))
                .upgrade(4, MetaItems.SENSOR_LuV.getStackForm(1))
                .device(DeviceHardware.DEVICE.PROSPECTOR_HV)
                .build();
        AppRegistryBuilder.create(new MultiBlockPreviewARApp())
                .battery(GTValues.LV, 128)
                .device(DeviceHardware.DEVICE.CAMERA)
                .upgrade(1, MetaItems.EMITTER_HV.getStackForm(4), MetaItems.WORKSTATION_EV.getStackForm(2))
                .defaultApp()
                .build();
        if (Loader.isModLoaded(GTValues.MODID_JEI)) {
            AppRegistryBuilder.create(new RecipeChartApp())
                    .battery(GTValues.LV, 160)
                    .upgrade(0, new ItemStack(Items.PAPER, 32))
                    .upgrade(1, new ItemStack(Items.PAPER, 64))
                    .upgrade(2, MetaItems.RANDOM_ACCESS_MEMORY.getStackForm(16))
                    .upgrade(3, MetaItems.RANDOM_ACCESS_MEMORY.getStackForm(32))
                    .build();
        }
        AppRegistryBuilder.create(new ConsoleApp())
                .battery(GTValues.LV, 500)
                .device(DeviceHardware.DEVICE.WIRELESS)
                .build();
        AppRegistryBuilder.create(new BatteryManagerApp()).defaultApp()
                .battery(GTValues.ULV, 0)
                .build();
        AppRegistryBuilder.create(new HardwareManagerApp()).defaultApp().build();
        AppRegistryBuilder.create(new AppStoreApp()).defaultApp().build();
        AppRegistryBuilder.create(new WorldProspectorARApp())
                .battery(GTValues.LV, 320)
                .upgrade(0, MetaItems.EMITTER_LV.getStackForm(2))
                .upgrade(1, MetaItems.EMITTER_MV.getStackForm(2))
                .upgrade(2, MetaItems.EMITTER_HV.getStackForm(2))
                .device(DeviceHardware.DEVICE.CAMERA)
                .build();
        AppRegistryBuilder.create(new VirtualTankApp())
                .battery(GTValues.MV, 500)
                .device(DeviceHardware.DEVICE.WIRELESS)
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initTerminalFiles() {
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(TerminalRegistry::onResourceManagerReload);
    }

    @SideOnly(Side.CLIENT)
    public static void onResourceManagerReload(IResourceManager resourceManager) {
        FileUtility.extractJarFiles(String.format("/assets/%s/%s", GTValues.MODID, "terminal"), TERMINAL_PATH, false);
    }

    public static void registerApp(AbstractApplication application) {
        String name = application.getRegistryName();
        if (APP_REGISTER.containsKey(name)) {
            GTLog.logger.warn("Duplicate APP registry names exist: {}", name);
            return;
        }
        APP_REGISTER.put(name, application);
    }

    public static void registerHardware(Hardware hardware) {
        String name = hardware.getRegistryName();
        if (APP_REGISTER.containsKey(name)) {
            GTLog.logger.warn("Duplicate APP registry names exist: {}", name);
            return;
        }
        HW_REGISTER.put(name, hardware);
    }

    public static void registerHardwareDemand(String name, boolean isDefaultApp, @NotNull List<Hardware>[] hardware,
                                              @NotNull List<ItemStack>[] upgrade) {
        if (name != null && APP_REGISTER.containsKey(name)) {
            if (isDefaultApp) {
                DEFAULT_APPS.add(name);
            }
            APP_HW_DEMAND.put(name, hardware);
            APP_UPGRADE_CONDITIONS.put(name, upgrade);
        } else {
            GTLog.logger.error("Not found the app {}", name);
        }
    }

    public static List<AbstractApplication> getDefaultApps() {
        return DEFAULT_APPS.stream().map(APP_REGISTER::get).collect(Collectors.toList());
    }

    public static Collection<AbstractApplication> getAllApps() {
        return APP_REGISTER.values();
    }

    public static AbstractApplication getApplication(String name) {
        return APP_REGISTER.get(name);
    }

    public static Collection<Hardware> getAllHardware() {
        return HW_REGISTER.values();
    }

    public static Hardware getHardware(String name) {
        return HW_REGISTER.get(name);
    }

    public static List<Hardware> getAppHardwareDemand(String name, int tier) {
        return APP_HW_DEMAND.get(name)[tier] != null ? APP_HW_DEMAND.get(name)[tier] : Collections.emptyList();
    }

    public static List<ItemStack> getAppHardwareUpgradeConditions(String name, int tier) {
        return APP_UPGRADE_CONDITIONS.get(name)[tier] != null ? APP_UPGRADE_CONDITIONS.get(name)[tier] :
                Collections.emptyList();
    }

    private static class AppRegistryBuilder {

        AbstractApplication app;
        boolean isDefaultApp;
        BatteryHardware[] battery;
        List<Hardware>[] hardware;
        List<ItemStack>[] upgrade;

        public static AppRegistryBuilder create(AbstractApplication app) {
            AppRegistryBuilder builder = new AppRegistryBuilder();
            builder.app = app;
            builder.battery = new BatteryHardware[app.getMaxTier() + 1];
            builder.hardware = new List[app.getMaxTier() + 1];
            builder.upgrade = new List[app.getMaxTier() + 1];
            return builder;
        }

        public AppRegistryBuilder defaultApp() {
            this.isDefaultApp = true;
            return this;
        }

        public AppRegistryBuilder battery(int batteryTier, long cost) {
            BatteryHardware hw = new BatteryHardware.BatteryDemand(batteryTier, cost);
            for (int i = 0; i <= app.getMaxTier(); i++) {
                battery[i] = hw;
            }
            return this;
        }

        public AppRegistryBuilder battery(int tier, int batteryTier, long cost) {
            if (tier < battery.length) {
                battery[tier] = new BatteryHardware.BatteryDemand(batteryTier, cost);
            }
            return this;
        }

        public AppRegistryBuilder device(DeviceHardware.DEVICE... device) {
            Hardware[] hw = Arrays.stream(device).map(DeviceHardware.DeviceDemand::new).toArray(Hardware[]::new);
            for (int i = 0; i <= app.getMaxTier(); i++) {
                this.hardware(i, hw);
            }
            return this;
        }

        public AppRegistryBuilder device(int tier, DeviceHardware.DEVICE... device) {
            this.hardware(tier, Arrays.stream(device).map(DeviceHardware.DeviceDemand::new).toArray(Hardware[]::new));
            return this;
        }

        public AppRegistryBuilder hardware(Hardware... hardware) {
            for (int i = 0; i <= app.getMaxTier(); i++) {
                this.hardware(i, hardware);
            }
            return this;
        }

        public AppRegistryBuilder hardware(int tier, Hardware... hardware) {
            if (tier < this.hardware.length) {
                this.hardware[tier] = new LinkedList<>();
                for (Hardware hw : hardware) {
                    this.hardware[tier].add(hw);
                }
            }
            return this;
        }

        public AppRegistryBuilder appendHardware(int tier, Hardware... hardware) {
            if (tier < this.hardware.length) {
                if (this.hardware[tier] == null) {
                    this.hardware[tier] = new LinkedList<>();
                }
                for (Hardware hw : hardware) {
                    this.hardware[tier].add(hw);
                }
            }
            return this;
        }

        public AppRegistryBuilder upgrade(ItemStack... upgrade) {
            ItemStack[] up = Arrays.stream(upgrade).toArray(ItemStack[]::new);
            for (int i = 0; i <= app.getMaxTier(); i++) {
                this.upgrade(i, up);
            }
            return this;
        }

        public AppRegistryBuilder upgrade(int tier, ItemStack... upgrade) {
            if (tier < this.upgrade.length) {
                this.upgrade[tier] = new LinkedList<>();
                for (ItemStack up : upgrade) {
                    this.upgrade[tier].add(up);
                }
            }
            return this;
        }

        public void build() {
            TerminalRegistry.registerApp(app);
            for (int i = 0; i < hardware.length; i++) {
                if (battery[i] != null) {
                    if (hardware[i] == null) {
                        hardware[i] = new LinkedList<>();
                    }
                    hardware[i].add(battery[i]);
                }
            }
            TerminalRegistry.registerHardwareDemand(app.getRegistryName(), isDefaultApp, hardware, upgrade);
        }
    }
}
