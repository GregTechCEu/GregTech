package gregtech.modules;

import gregtech.api.modules.IModuleContainer;

public class GregTechModules implements IModuleContainer {

    public static final String MODULE_CORE = "core";

    // Integration modules
    public static final String MODULE_JEI = "jei_integration";
    public static final String MODULE_TOP = "top_integration";

    @Override
    public String getID() {
        return "gregtech";
    }
}
