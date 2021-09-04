package gregtech.api.terminal;

import gregtech.api.GTValues;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.hardware.BatteryHardware;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.util.GuideJsonLoader;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.terminal.app.ThemeSettingApp;
import gregtech.common.terminal.app.batterymanager.BatteryManagerApp;
import gregtech.common.terminal.app.console.ConsoleApp;
import gregtech.common.terminal.app.guide.ItemGuideApp;
import gregtech.common.terminal.app.guide.MultiBlockGuideApp;
import gregtech.common.terminal.app.guide.SimpleMachineGuideApp;
import gregtech.common.terminal.app.guide.TutorialGuideApp;
import gregtech.common.terminal.app.guideeditor.GuideEditorApp;
import gregtech.common.terminal.app.prospector.OreProspectorApp;
import gregtech.common.terminal.app.recipechart.RecipeChartApp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.registry.RegistryDefaulted;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TerminalRegistry {
    public static final RegistrySimple<String, AbstractApplication> APP_REGISTER = new RegistrySimple<>();
    public static final RegistrySimple<String, Hardware> HW_REGISTER = new RegistrySimple<>();
    public static final RegistrySimple<String, List<Hardware>> APP_HW_DEMAND = new RegistryDefaulted<>(Collections.emptyList());
    public static final RegistrySimple<String, List<List<Object>>> APP_UPGRADE_CONDITIONS = new RegistryDefaulted<>(Collections.emptyList());
    public static final List<String> DEFAULT_APPS = new ArrayList<>();
    @SideOnly(Side.CLIENT)
    public static final File TERMINAL_PATH = new File(Loader.instance().getConfigDir(), ConfigHolder.U.clientConfig.terminalRootPath);

    public static void init() {
        // register hardware
        registerHardware(new BatteryHardware());
        // register applications
        AppBuilder.create(new SimpleMachineGuideApp()).defaultApp(true).build();
        AppBuilder.create(new MultiBlockGuideApp()).defaultApp(true).build();
        AppBuilder.create(new ItemGuideApp()).defaultApp(true).build();
        AppBuilder.create(new TutorialGuideApp()).defaultApp(true).build();
        AppBuilder.create(new GuideEditorApp()).defaultApp(true).build();
        AppBuilder.create(new ThemeSettingApp()).defaultApp(true).build();
        AppBuilder.create(new OreProspectorApp()).defaultApp(false).battery(2, 1000).build();
        if (GTValues.isModLoaded(GTValues.MODID_JEI)) {
            AppBuilder.create(new RecipeChartApp()).defaultApp(false).battery(1, 100).build();
        }
        AppBuilder.create(new ConsoleApp()).defaultApp(false).battery(1, 500).build();
        AppBuilder.create(new BatteryManagerApp()).battery(0, 10).defaultApp(true).build();
    }

    @SideOnly(Side.CLIENT)
    public static void initTerminalFiles() {
        FileUtility.extractJarFiles(String.format("/assets/%s/%s", GTValues.MODID, "terminal"), TERMINAL_PATH, false);
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new GuideJsonLoader());
    }

    public static void registerApp(AbstractApplication application) {
        String name = application.getRegistryName();
        if (APP_REGISTER.containsKey(name)) {
            GTLog.logger.warn("Duplicate APP registry names exist: {}", name);
            return;
        }
        APP_REGISTER.putObject(name, application);
    }

    public static void registerHardware(Hardware hardware) {
        String name = hardware.getRegistryName();
        if (APP_REGISTER.containsKey(name)) {
            GTLog.logger.warn("Duplicate APP registry names exist: {}", name);
            return;
        }
        HW_REGISTER.putObject(name, hardware);
    }

    public static void registerHardwareDemand(String name, boolean isDefaultApp, @Nullable List<Hardware> hardware, @Nullable List<List<Object>> upgrade) {
        if (name != null && APP_REGISTER.containsKey(name)) {
            if (isDefaultApp) {
                DEFAULT_APPS.add(name);
            }
            if (hardware != null && !hardware.isEmpty()) {
                APP_HW_DEMAND.putObject(name, hardware);
            }
            if (upgrade != null && !upgrade.isEmpty()) {
                APP_UPGRADE_CONDITIONS.putObject(name, upgrade);
            }
        } else {
            GTLog.logger.error("Not found the app {}", name);
        }
    }

    public static List<AbstractApplication> getDefaultApps() {
        return DEFAULT_APPS.stream().map(APP_REGISTER::getObject).collect(Collectors.toList());
    }

    public static List<AbstractApplication> getAllApps() {
        return APP_REGISTER.getKeys().stream().map(APP_REGISTER::getObject).collect(Collectors.toList());
    }

    public static AbstractApplication getApplication(String name) {
        return APP_REGISTER.getObject(name);
    }

    public static List<Hardware> getAllHardware() {
        return HW_REGISTER.getKeys().stream().map(HW_REGISTER::getObject).collect(Collectors.toList());
    }

    public static Hardware getHardware(String name) {
        return HW_REGISTER.getObject(name);
    }

    public static List<Hardware> getAppHardwareDemand(String name) {
        return APP_HW_DEMAND.getObject(name);
    }

    public static List<List<Object>> getAppHardwareUpgradeConditions(String name) {
        return APP_UPGRADE_CONDITIONS.getObject(name);
    }

    private static class AppBuilder {
        AbstractApplication app;
        boolean isDefaultApp;
        List<Hardware> hardware;
        List<List<Object>> upgrade;

        public static AppBuilder create(AbstractApplication app){
            AppBuilder builder = new AppBuilder();
            builder.app = app;
            builder.hardware = new ArrayList<>();
            builder.upgrade = new ArrayList<>(Collections.nCopies(app.getMaxTier(), null));
            return builder;
        }

        public AppBuilder defaultApp(boolean isDefaultApp){
            this.isDefaultApp = isDefaultApp;
            return this;
        }

        public AppBuilder battery(int tier, long cost) {
            this.hardware.add(new BatteryHardware.BatteryDemand(tier, cost));
            return this;
        }

        public AppBuilder hardware(Hardware... hardware) {
            this.hardware.addAll(Arrays.asList(hardware));
            return this;
        }

        public AppBuilder upgrade(int tier, List<Object> objects) {
            if (tier < upgrade.size()) {
                upgrade.set(tier, objects);
            }
            return this;
        }

        public void build() {
            TerminalRegistry.registerApp(app);
            TerminalRegistry.registerHardwareDemand(app.getRegistryName(), isDefaultApp, hardware, upgrade);
        }
    }
}
