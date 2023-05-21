package gregtech.worldgen.terrain.internal;

import gregtech.worldgen.terrain.IBlockMapper;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Maps BlockStates to multiple candidates.
 */
public class BlockMapper implements IBlockMapper {

    private final Map<IBlockState, List<IBlockState>> map;

    /**
     * @param map the map
     */
    public BlockMapper(@Nonnull Map<IBlockState, List<IBlockState>> map) {
        this.map = map;
    }

    @Override
    @Nullable
    public List<IBlockState> getCandidates(@Nonnull IBlockState state) {
        return map.get(state);
    }

    @Override
    public String toString() {
        return "BlockMapper{" + map + '}';
    }
}
