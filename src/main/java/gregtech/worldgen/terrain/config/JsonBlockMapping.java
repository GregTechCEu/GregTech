package gregtech.worldgen.terrain.config;

import gregtech.api.util.ConfigUtil;
import gregtech.worldgen.WorldgenModule;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Json-equivalent form of a single mapping in a {@link gregtech.worldgen.terrain.BlockMapper}
 */
public class JsonBlockMapping {

    // must be public for Gson
    public String target;
    public String[] replacements;

    /**
     * Convert the JSON representation of a BlockMapping to a Parsed Mapping
     * @param filePath the path to the file, used for logging
     * @return the converted form
     */
    @Nullable
    ParsedBlockMapping toParsed(@Nonnull String filePath) {
        if (target == null) {
            WorldgenModule.logger.error("Unable to parse target BlockState. Skipping file {}", filePath);
            return null;
        }

        if (replacements == null) {
            WorldgenModule.logger.error("Unable to parse replacement BlockStates. Skipping file {}", filePath);
            return null;
        }

        IBlockState targetState = ConfigUtil.getBlockStateFromName(target);
        if (targetState == null) {
            WorldgenModule.logger.error("Unable to parse target BlockState from name {}. Skipping file {}", target, filePath);
            return null;
        }

        List<IBlockState> replacementStates = new ArrayList<>(replacements.length);
        for (String name : replacements) {
            IBlockState blockState = ConfigUtil.getBlockStateFromName(name);
            if (blockState == null) {
                WorldgenModule.logger.error("Unable to parse replacement BlockState from name {} in file {}. Skipping entry...", name, filePath);
            }
            replacementStates.add(blockState);
        }

        return new ParsedBlockMapping(targetState, replacementStates);
    }
}
