package gregtech.api.terminal.os;

import gregtech.api.util.function.TriConsumer;

public enum SystemCall {
    CALL_MENU("call_menu", (os, side, args)->os.callMenu(side)),
    FULL_SCREEN("full_screen", (os, side, args)->os.maximize(side)),
    MINIMIZE_FOCUS_APP("minimize_focus_app", (os, side, args)->os.minimizeApplication(os.getFocusApp(), side)),
    CLOSE_FOCUS_APP("close_focus_app", (os, side, args)->os.closeApplication(os.getFocusApp(), side)),
    SHUT_DOWN("shutdown", (os, side, args)->os.shutdown(side)),
    OPEN_APP("shutdown", (os, side, args)->os.shutdown(side));


    TriConsumer<TerminalOSWidget, Boolean, Object[]> action;
    String name;

    SystemCall(String name,  TriConsumer<TerminalOSWidget, Boolean, Object[]> action){
        this.action = action;
        this.name = name;
    }

    public void call(TerminalOSWidget os, boolean isClient, Object... args) {
        action.accept(os, isClient, args);

    }
}
