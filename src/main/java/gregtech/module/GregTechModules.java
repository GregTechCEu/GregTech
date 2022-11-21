package gregtech.module;

import gregtech.api.module.IModuleContainer;

public class GregTechModules implements IModuleContainer {

    public static final String MODULE_CORE = "core";
    public static final String MODULE_MACHINE = "machine";
    public static final String MODULE_TOOL = "tool";

    // Integration modules
    public static final String MODULE_JEI = "jei_integration";
    public static final String MODULE_TOP = "top_integration";

    @Override
    public String getID() {
        return "gregtech";
    }
}
