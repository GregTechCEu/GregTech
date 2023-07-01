package gregtech.integration.forestry;

import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

@LangKey("gregtech.config.forestry")
@Config(modid = GTValues.MODID, name = GTValues.MODID + "/forestry", category = "Forestry")
public class ForestryConfig {

    @Comment({"Enable GregTech Electron Tubes.", "Default: true"})
    @RequiresMcRestart
    public static boolean enableGTElectronTubes = true;

    @Comment({"Enable the GregTech Scoop.", "Default: true"})
    @RequiresMcRestart
    public static boolean enableGTScoop = true;

    @Comment({
            "Enable GregTech Bees.",
            "Requirements: Forestry Apiculture module",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean enableGTBees = true;

    @Comment({
            "Whether or not to make GregTech Comb processing recipes harder.",
            "Requirements: Forestry Apiculture module, 'enableGTBees' config option enabled",
            "Primarily affects whether the Centrifuge (GregTech or Forestry) can process GregTech Combs.",
            "Default: false"
    })
    @RequiresMcRestart
    public static boolean harderGTCombRecipes = false;

    @Comment({
            "Enable GregTech Apiary Frames.",
            "Requirements: Forestry Apiculture module",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean enableGTFrames = true;
}
