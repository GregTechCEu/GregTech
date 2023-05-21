package gregtech.worldgen.terrain.config.internal;

import gregtech.api.util.ConfigUtil;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.terrain.config.JsonBlockMapping;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ParsedBlockMapping {

    @Nonnull
    IBlockState target;
    @Nonnull
    List<IBlockState> replacements;

    private ParsedBlockMapping(@Nonnull IBlockState target, @Nonnull List<IBlockState> replacements) {
        this.target = target;
        this.replacements = replacements;
    }

    /**
     * Convert the JSON representation of a BlockMapping to a Parsed Mapping
     *
     * @param filePath the path to the file, used for logging
     * @return the converted form
     */
    @Nullable
    public static ParsedBlockMapping fromJson(@Nonnull JsonBlockMapping mapping, @Nonnull String filePath) {
        if (mapping.target == null) {
            WorldgenModule.logger.error("Unable to parse target BlockState. Skipping file {}", filePath);
            return null;
        }

        if (mapping.replacements == null) {
            WorldgenModule.logger.error("Unable to parse replacement BlockStates. Skipping file {}", filePath);
            return null;
        }

        IBlockState targetState = ConfigUtil.getBlockStateFromName(mapping.target);
        if (targetState == null) {
            WorldgenModule.logger.error("Unable to parse target BlockState from name {}. Skipping file {}", mapping.target, filePath);
            return null;
        }

        List<IBlockState> replacementStates = new ArrayList<>(mapping.replacements.length);
        for (String name : mapping.replacements) {
            IBlockState blockState = ConfigUtil.getBlockStateFromName(name);
            if (blockState == null) {
                WorldgenModule.logger.error("Unable to parse replacement BlockState from name {} in file {}. Skipping entry...", name, filePath);
            }
            replacementStates.add(blockState);
        }

        return new ParsedBlockMapping(targetState, replacementStates);
    }
}
