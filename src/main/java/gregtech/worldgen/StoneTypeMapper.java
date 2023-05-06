package gregtech.worldgen;

import com.google.gson.JsonObject;
import gregtech.api.util.BlockStateHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Maps a single state to multiple candidates.
 */
public class StoneTypeMapper {

    private final Map<IBlockState, List<IBlockState>> map = new Object2ObjectOpenCustomHashMap<>(BlockStateHashStrategy.STRATEGY);

    public StoneTypeMapper() {

    }

    public void initializeFromJson(@Nonnull JsonObject json) {
        //TODO impl
    }

    @Nullable
    public List<IBlockState> getCandidates(@Nonnull IBlockState state) {
        return map.get(state);
    }

    @Override
    public String toString() {
        return "StoneTypeMapper{" + map + '}';
    }
}
