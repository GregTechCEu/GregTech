package gregtech.api.terminal;

import gregtech.api.terminal.app.*;
import gregtech.api.terminal.app.guide.ItemGuideApp;
import gregtech.api.terminal.app.guide.MultiBlockGuideApp;
import gregtech.api.terminal.app.guide.SimpleMachineGuideApp;
import gregtech.api.terminal.app.guide.TutorialGuideApp;

import java.util.ArrayList;
import java.util.List;

public class TerminalBuilder {
    private static final List<AbstractApplication> appRegister = new ArrayList<>();

    public static void init() {
        appRegister.add(new SimpleMachineGuideApp());
        appRegister.add(new MultiBlockGuideApp());
        appRegister.add(new ItemGuideApp());
        appRegister.add(new TutorialGuideApp());
        appRegister.add(new GuideEditorApp());
    }

    public static List<AbstractApplication> getApplications() {
        return appRegister;
    }
}
