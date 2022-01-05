package gregtech.api.worldgen.filler;

import com.google.gson.JsonObject;
import gregtech.api.worldgen.config.FillerConfigUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimpleBlockFiller extends BlockFiller {

    private FillerEntry fillerEntry;

    public SimpleBlockFiller() {
    }

    public SimpleBlockFiller(FillerEntry blockStateFiller) {
        this.fillerEntry = blockStateFiller;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.fillerEntry = FillerConfigUtils.createBlockStateFiller(object.get("value"));
    }

    @Override
    public IBlockState apply(IBlockState currentState, IBlockAccess blockAccess, BlockPos blockPos, int relativeX, int relativeY, int relativeZ, double density, Random gridRandom, int layer) {
        return fillerEntry.apply(currentState, blockAccess, blockPos);
    }

    @Override
    public List<FillerEntry> getAllPossibleStates() {
        return Collections.singletonList(fillerEntry);
    }
}
