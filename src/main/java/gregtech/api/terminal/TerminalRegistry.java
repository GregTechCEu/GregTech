package gregtech.api.terminal;

import gregtech.api.GTValues;
import gregtech.api.terminal.app.*;
import gregtech.common.terminal.app.ThemeSettingApp;
import gregtech.common.terminal.app.guide.ItemGuideApp;
import gregtech.common.terminal.app.guide.MultiBlockGuideApp;
import gregtech.common.terminal.app.guide.SimpleMachineGuideApp;
import gregtech.common.terminal.app.guide.TutorialGuideApp;
import gregtech.common.terminal.app.guideeditor.GuideEditorApp;
import gregtech.common.terminal.app.prospector.OreProspectorApp;
import gregtech.common.terminal.app.recipechart.RecipeChartApp;

import java.util.*;

public class TerminalRegistry {
    private static final Map<String, AbstractApplication> appRegister = new HashMap<>();
    private static final List<String> defaultApps = new ArrayList<>();

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
    }

    public static void registerApp(AbstractApplication application, boolean isDefaultApp) {
        appRegister.put(application.getRegistryName(), application);
        if (isDefaultApp) {
            defaultApps.add(application.getRegistryName());
        }
    }

    public static List<String> getDefaultApps() {
        return defaultApps;
    }

    public static List<String> getAllApps() {
        return new ArrayList<>(appRegister.keySet());
    }

    public static AbstractApplication getApplication(String name) {
        return appRegister.get(name);
    }
}
