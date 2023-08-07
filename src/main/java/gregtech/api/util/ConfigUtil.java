package gregtech.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ConfigUtil {

    private static final Pattern COLON_PATTERN = Pattern.compile(":");

    private ConfigUtil() {}

    /**
     * Get an IBlockState from a block's name.
     * Formats: {@code modid:unlocalized_name} or {@code modid:unlocalized_name:metadata}
     *
     * @param name the name of the block with optional metadata
     * @return the BlockState
     */
    @Nullable
    public static IBlockState getBlockStateFromName(@Nonnull String name) {
        String[] blockDescription = COLON_PATTERN.split(name);
        Block replacementBlock;

        if (blockDescription.length == 2) {
            replacementBlock = Block.getBlockFromName(name);
        } else {
            replacementBlock = Block.getBlockFromName(blockDescription[0] + ":" + blockDescription[1]);
        }

        if (replacementBlock == null) return null;

        // check for meta
        if (blockDescription.length > 2 && !blockDescription[2].isEmpty()) {
            //noinspection deprecation
            return replacementBlock.getDefaultState().getBlock().getStateFromMeta(Integer.parseInt(blockDescription[2]));
        }

        return replacementBlock.getDefaultState();
    }

    @Nonnull
    public static String getBlockStateName(@Nonnull IBlockState state) {
        Block block = state.getBlock();
        int metadata = block.getMetaFromState(state);
        String name = Objects.requireNonNull(block.getRegistryName()).toString();
        if (metadata != 0) name += ":" + metadata;
        return name;
    }
}
