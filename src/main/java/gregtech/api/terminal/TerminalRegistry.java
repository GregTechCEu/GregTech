package gregtech.api.terminal;

import gregtech.api.GTValues;
import gregtech.api.terminal.app.*;
import gregtech.api.terminal.util.GuideJsonLoader;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import gregtech.common.terminal.app.ThemeSettingApp;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class TerminalRegistry {
    private static final Map<String, AbstractApplication> APP_REGISTER = new HashMap<>();
    private static final List<String> DEFAULT_APPS = new ArrayList<>();
    @SideOnly(Side.CLIENT)
    public static final File TERMINAL_PATH = new File(Loader.instance().getConfigDir().getParentFile(), "terminal");

    public static void init() {
        registerApp(new SimpleMachineGuideApp(), true);
        registerApp(new MultiBlockGuideApp(), true);
        registerApp(new ItemGuideApp(), true);
        registerApp(new TutorialGuideApp(), true);
        registerApp(new GuideEditorApp(), true);
        registerApp(new ThemeSettingApp(), true);
        registerApp(new OreProspectorApp(), true);
        if (GTValues.isModLoaded(GTValues.MODID_JEI)) {
            registerApp(new RecipeChartApp(), true);
        }
        registerApp(new ConsoleApp(), true);

        initTerminalFiles();
    }

    public static void initTerminalFiles() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            try {
                FileUtility.extractJarFiles(Paths.get(TerminalRegistry.class.getResource(String.format("/assets/%s/%s", GTValues.MODID, "terminal")).toURI()),
                        TERMINAL_PATH.toPath(),
                        false);
            } catch (IOException | URISyntaxException e) {
                GTLog.logger.error("error while init terminal resources.", e);
            }
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new GuideJsonLoader());
        }
    }

    public static void registerApp(AbstractApplication application, boolean isDefaultApp) {
        APP_REGISTER.put(application.getRegistryName(), application);
        if (isDefaultApp) {
            DEFAULT_APPS.add(application.getRegistryName());
        }
    }

    public static List<String> getDefaultApps() {
        return DEFAULT_APPS;
    }

    public static List<String> getAllApps() {
        return new ArrayList<>(APP_REGISTER.keySet());
    }

    public static AbstractApplication getApplication(String name) {
        return APP_REGISTER.get(name);
    }
}
