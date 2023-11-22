package gregtech.integration.tinkers;

import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

@LangKey("gregtech.config.tinkers")
@Config(modid = GTValues.MODID, name = GTValues.MODID + "/tinkers_integration", category = "Tinkers' Construct")
public class TinkersConfig {

    @Comment({"Enable GregTech alloys in the Smeltery.", "Default: true"})
    @RequiresMcRestart
    public static boolean enableGTSmeltryAlloys = true;
}
