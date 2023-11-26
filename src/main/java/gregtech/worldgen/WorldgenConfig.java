package gregtech.worldgen;

import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;

@Config.LangKey("gregtech.config.worldgen")
@Config(modid = GTValues.MODID, name = GTValues.MODID + "/worldgen", category = "Worldgen")
public final class WorldgenConfig {

    @Config.Comment({"Chunk-Aligned Worldgen Cache Size.",
            "Speeds up chunk-aligned worldgen at the cost of using more memory.",
            "Increase this limit if worldgen causes performance issues on a server with multiple players.",
            "Should not be necessary to increase this on SinglePlayer.",
            "Default: 512"})
    @Config.RangeInt(min = Byte.MAX_VALUE, max = Short.MAX_VALUE)
    @Config.RequiresMcRestart
    public static int chunkAlignedCacheSize = 512;
}
