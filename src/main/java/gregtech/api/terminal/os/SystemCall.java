package gregtech.api.terminal.os;

import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.util.function.TriConsumer;

public enum SystemCall {
    CALL_MENU("call_menu", 0, (os, side, args)->os.callMenu(side)),
    FULL_SCREEN("full_screen", 1, (os, side, args)->os.maximize(side)),
    MINIMIZE_FOCUS_APP("minimize_focus_app", 2, (os, side, args)->os.minimizeApplication(os.getFocusApp(), side)),
    CLOSE_FOCUS_APP("close_focus_app", 3, (os, side, args)->os.closeApplication(os.getFocusApp(), side)),
    SHUT_DOWN("shutdown", 4, (os, side, args)->os.shutdown(side)),
    OPEN_APP("open_app", 5, (os, side, args)->{
        if (args.length > 0 && args[0] != null) {
            AbstractApplication app = TerminalRegistry.getApplication(args[0]);
            if (app != null) {
                os.openApplication(app, side);
            }
        }
    });


    TriConsumer<TerminalOSWidget, Boolean, String[]> action;
    String name;
    int index;

    SystemCall(String name, int index, TriConsumer<TerminalOSWidget, Boolean, String[]> action){
        this.action = action;
        this.name = name;
        this.index = index;
    }

    public void call(TerminalOSWidget os, boolean isClient, String... args) {
        action.accept(os, isClient, args);

    }

    public String getTranslateKey() {
        return "terminal.system_call." + name;
    }

    public static SystemCall getFromName(String name) {
        for (SystemCall value : SystemCall.values()) {
            if (value.name.toLowerCase().equals(name.toLowerCase())) {
                return value;
            } else if(value.getTranslateKey().equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static SystemCall getFromIndex(int index) {
        for (SystemCall value : SystemCall.values()) {
            if (value.index == index) {
                return value;
            }
        }
        return null;
    }
}
