package gregtech.modules;

import gregtech.api.modules.IModuleContainer;

public class GregTechModules implements IModuleContainer {

    public static final String MODULE_CORE = "core";
    public static final String MODULE_TOOLS = "tools";
    public static final String MODULE_INTEGRATION = "integration";

    // Integration modules
    public static final String MODULE_JEI = "jei_integration";
    public static final String MODULE_TOP = "top_integration";
    public static final String MODULE_CT = "ct_integration";
    public static final String MODULE_GRS = "grs_integration";
    public static final String MODULE_OC = "oc_integration";
    public static final String MODULE_HWYLA = "hwyla_integration";
    public static final String MODULE_BAUBLES = "baubles_integration";
    public static final String MODULE_FR = "fr_integration";

    @Override
    public String getID() {
        return "gregtech";
    }
}
