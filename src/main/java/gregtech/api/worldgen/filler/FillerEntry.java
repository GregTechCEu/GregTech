package gregtech.api.worldgen.filler;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface FillerEntry {

    IBlockState apply(IBlockState source, IBlockAccess blockAccess, BlockPos blockPos);

    Collection<IBlockState> getPossibleResults();

    default List<Pair<Integer, FillerEntry>> getEntries() {
        return new ArrayList<>();
    }

    static FillerEntry createSimpleFiller(IBlockState blockState) {
        List<IBlockState> possibleResults = ImmutableList.of(blockState);
        return new FillerEntry() {
            @Override
            public IBlockState apply(IBlockState source, IBlockAccess blockAccess, BlockPos blockPos) {
                return blockState;
            }

            @Override
            public List<IBlockState> getPossibleResults() {
                return possibleResults;
            }
        };
    }
}
