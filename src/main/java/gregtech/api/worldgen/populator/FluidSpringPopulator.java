package gregtech.api.worldgen.populator;

import com.google.gson.JsonObject;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FluidSpringPopulator implements VeinBufferPopulator {

    private IBlockState fluidState;
    private float springGenerationChance;

    public FluidSpringPopulator() {
    }

    public FluidSpringPopulator(IBlockState fluidState, float springGenerationChance) {
        this.fluidState = fluidState;
        this.springGenerationChance = springGenerationChance;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.springGenerationChance = object.get("chance").getAsFloat();
    }

    @Override
    public void initializeForVein(OreDepositDefinition definition) {
        List<IBlockState> possibleStates = definition.getBlockFiller().getAllPossibleStates().stream()
                .flatMap(it -> it.getPossibleResults().stream())
                .collect(Collectors.toList());
        this.fluidState = possibleStates.stream().filter(it -> it.getPropertyKeys().contains(BlockFluidBase.LEVEL)).findFirst().orElse(null);
        if (fluidState == null) {
            String message = "Can't find fluid block for spring in vein %s. Blocks in vein: %s";
            throw new IllegalArgumentException(String.format(message, definition.getDepositName(), possibleStates));
        }
    }

    @Override
    public void populateBlockBuffer(Random random, GridEntryInfo gridEntryInfo, IBlockModifierAccess modifier, OreDepositDefinition depositDefinition) {
        if (random.nextFloat() <= springGenerationChance) {
            int groundLevel = gridEntryInfo.getTerrainHeight();
            int springUndergroundHeight = groundLevel - gridEntryInfo.getCenterPos(depositDefinition).getY();
            int springHeight = springUndergroundHeight + 6 + random.nextInt(3);
            for (int i = 1; i <= springHeight; i++) {
                modifier.setBlock(0, i, 0, 0);
                if (i <= springUndergroundHeight) {
                    modifier.setBlock(1, i, 0, 0);
                    modifier.setBlock(-1, i, 0, 0);
                    modifier.setBlock(0, i, 1, 0);
                    modifier.setBlock(0, i, -1, 0);
                }
            }
        }
    }

    @Override
    public IBlockState getBlockByIndex(World world, BlockPos pos, int index) {
        return fluidState.withProperty(BlockFluidBase.LEVEL, index);
    }

    public IBlockState getFluidState() {
        return fluidState;
    }
}
