package gregtech.integration.forestry;

import gregtech.api.GTValues;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

@LangKey("gregtech.config.forestry")
@Config(modid = GTValues.MODID, name = GTValues.MODID + "/forestry_integration", category = "Forestry")
public class ForestryConfig {

    @Comment({ "Enable GregTech Electron Tubes.", "Default: true" })
    @RequiresMcRestart
    public static boolean enableGTElectronTubes = true;

    @Comment({ "Enable the GregTech Scoop.", "Default: true" })
    @RequiresMcRestart
    public static boolean enableGTScoop = true;

    @Comment({
            "Enable GregTech Bees.",
            "Requirements: Forestry Apiculture module",
            "Recommended: ExtraBees and MagicBees, as some GT bees are only added, and others have more interesting mutations, if those mods are present",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean enableGTBees = true;

    @Comment({
            "Enable GregTech Apiary Frames.",
            "Requirements: Forestry Apiculture module",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean enableGTFrames = true;

    @Comment({
            "Whether to remove some Forestry Crafting Table recipes (such as Fertilizer, Compost, etc) in favor of GT recipes.",
            "Default: false"
    })
    @RequiresMcRestart
    public static boolean harderForestryRecipes = false;

    @Comment({
            "Whether to disable some bees from other Forestry addons, such as ExtraBees and MagicBees, in favor of GTs.",
            "Heavily recommended, as the removed bees have no purpose with GT Bees other than JEI and breed tree clutter.",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean disableConflictingBees = true;
}
