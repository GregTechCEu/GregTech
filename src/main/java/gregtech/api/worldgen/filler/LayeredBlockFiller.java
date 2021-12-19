package gregtech.api.worldgen.filler;

import com.google.gson.JsonObject;
import gregtech.api.worldgen.config.FillerConfigUtils;
import gregtech.api.worldgen.config.FillerConfigUtils.LayeredFillerEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LayeredBlockFiller extends BlockFiller {

    private LayeredFillerEntry fillerEntry;

    public LayeredBlockFiller() {
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.fillerEntry = FillerConfigUtils.createLayeredFiller(object);
    }

    @Override
    public IBlockState apply(IBlockState currentState, IBlockAccess blockAccess, BlockPos blockPos, int relativeX, int relativeY, int relativeZ, double density, Random gridRandom, int layer) {
        return fillerEntry.apply(currentState, blockAccess, blockPos, density, gridRandom, layer); // todo need to pass more through here
    }

    @Override
    public List<FillerEntry> getAllPossibleStates() {
        return Collections.singletonList(fillerEntry);
    }
}
