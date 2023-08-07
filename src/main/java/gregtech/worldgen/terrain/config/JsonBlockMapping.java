package gregtech.worldgen.terrain.config;

import gregtech.api.util.ConfigUtil;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Json-equivalent form of a single mapping in a {@link gregtech.worldgen.terrain.IBlockMapper}
 */
public class JsonBlockMapping {

    // must be public for Gson
    public String target;
    public String[] replacements;

    /**
     * Needed for Gson to parse json into an instance of this class
     */
    @SuppressWarnings("unused")
    private JsonBlockMapping() {}

    public JsonBlockMapping(@Nonnull IBlockState target, @Nonnull IBlockState... replacements) {
        this.target = ConfigUtil.getBlockStateName(target);
        this.replacements = Arrays.stream(replacements)
                .map(ConfigUtil::getBlockStateName)
                .toArray(String[]::new);
    }
}
