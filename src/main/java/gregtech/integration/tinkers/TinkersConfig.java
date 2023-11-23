package gregtech.integration.tinkers;

import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

@LangKey("gregtech.config.tinkers")
@Config(modid = GTValues.MODID, name = GTValues.MODID + "/tinkers_integration", category = "Tinkers' Construct")
public class TinkersConfig {

    @Comment({"Enable GregTech alloys in the Smeltery.", "Default: true"})
    @RequiresMcRestart
    public static boolean enableGTSmelteryAlloys = true;

    @Comment({"Enable GregTech metal tools with Tinkers' Construct.", "Default: true"})
    @RequiresMcRestart
    public static boolean enableMetalTools = true;

    @Comment({"Enable GregTech gem tools with Tinkers' Construct.", "Default: true"})
    @RequiresMcRestart
    public static boolean enableGemTools = true;

    @Name("Tool Stats Options")
    @Comment("Config options for general tinker stats modifiers for GT parts")
    @RequiresMcRestart
    public static ToolStats toolStats = new ToolStats();

    public static class ToolStats {

        @RangeDouble(min = 0.01, max = 1.0)
        public double durabilityModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double miningSpeedModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double attackDamageModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double handleModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double bowDrawSpeedModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double bowFlightSpeedModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double arrowMassModifier = 1.0;

        @RangeDouble(min = 0.01, max = 1.0)
        public double arrowAmmoModifier = 1.0;
    }
}
