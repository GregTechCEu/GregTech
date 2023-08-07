package gregtech.worldgen.terrain;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Mapper providing candidates to replace a block with in terrain generation
 */
@FunctionalInterface
public interface IBlockMapper {

    /**
     * @param state the state to get candidates for
     * @return the candidates to replace the state with
     */
    @Nullable
    List<IBlockState> getCandidates(@Nonnull IBlockState state);
}
