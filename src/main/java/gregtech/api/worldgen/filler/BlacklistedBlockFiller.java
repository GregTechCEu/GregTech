package gregtech.api.worldgen.filler;

import gregtech.api.worldgen.config.FillerConfigUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlacklistedBlockFiller extends BlockFiller {

    private FillerEntry blockStateFiller;
    private final List<IBlockState> blacklist;

    public BlacklistedBlockFiller(List<IBlockState> blacklist) {
        this.blacklist = blacklist;
    }

    public BlacklistedBlockFiller(List<IBlockState> blacklist, FillerEntry blockStateFiller) {
        this.blacklist = blacklist;
        this.blockStateFiller = blockStateFiller;
    }

    public List<IBlockState> getBlacklist() {
        return blacklist;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.blockStateFiller = FillerConfigUtils.createBlockStateFiller(object.get("value"));
    }

    @Override
    public IBlockState apply(IBlockState currentState, IBlockAccess blockAccess, BlockPos blockPos, int relativeX,
                             int relativeY, int relativeZ, double density, Random gridRandom, int layer) {
        for (IBlockState blockState : blacklist) {
            if (blockState == currentState) {
                return currentState;
            }
        }
        return blockStateFiller.apply(currentState, blockAccess, blockPos);
    }

    @Override
    public List<FillerEntry> getAllPossibleStates() {
        return Collections.singletonList(blockStateFiller);
    }
}
