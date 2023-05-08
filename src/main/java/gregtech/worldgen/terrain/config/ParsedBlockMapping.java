package gregtech.worldgen.terrain.config;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.util.List;

class ParsedBlockMapping {

    @Nonnull
    IBlockState target;
    @Nonnull
    List<IBlockState> replacements;

    ParsedBlockMapping(@Nonnull IBlockState target, @Nonnull List<IBlockState> replacements) {
        this.target = target;
        this.replacements = replacements;
    }
}
