package gregtech.api.terminal;

import gregtech.api.terminal.app.*;
import gregtech.api.terminal.app.guide.ItemGuideApp;
import gregtech.api.terminal.app.guide.MultiBlockGuideApp;
import gregtech.api.terminal.app.guide.SimpleMachineGuideApp;
import gregtech.api.terminal.app.guide.TutorialGuideApp;
import gregtech.api.terminal.app.guideeditor.GuideEditorApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalBuilder {
    private static final Map<String, AbstractApplication> appRegister = new HashMap<>();
    private static final List<String> defaultApps = new ArrayList<>();

    public static void init() {
        registerApp(new SimpleMachineGuideApp(), true);
        registerApp(new MultiBlockGuideApp(), true);
        registerApp(new ItemGuideApp(), true);
        registerApp(new TutorialGuideApp(), true);
        registerApp(new GuideEditorApp(), true);
        registerApp(new ThemeSettingApp(), true);
    }

    public static void registerApp(AbstractApplication application, boolean isDefaultApp) {
        appRegister.put(application.getName(), application);
        if (isDefaultApp) {
            defaultApps.add(application.getName());
        }
    }

    public static List<String> getDefaultApps() {
        return defaultApps;
    }

    public static AbstractApplication getApplication(String name) {
        return appRegister.get(name);
    }
}
