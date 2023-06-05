package gregtech.integration.exnihilo;

import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;

import static net.minecraftforge.common.config.Config.Comment;
import static net.minecraftforge.common.config.Config.RequiresMcRestart;
import static net.minecraftforge.common.config.Config.LangKey;

@LangKey("gregtech.config.ex_nihilo")
@Config(modid = GTValues.MODID, name = GTValues.MODID + "/ex_nihilo", category = "Ex Nihilo")
public class ExNihiloConfig {

    @Config.Comment({
            "Override all Sifting Tables to drop excess outputs once GT drops have been added.",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean overrideAllSiftDrops = true;

    @Comment({
            "Replace Ex Nihilo Mesh recipes with GT-style recipes",
            "Default: true"
    })
    @RequiresMcRestart
    public static boolean harderMeshes = true;
}
