package gregtech.api.worldgen.populator;

import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FluidBallPopulator implements VeinBufferPopulator {

    private IBlockState fluidState;
    private float springGenerationChance;

    public FluidBallPopulator() {}

    public FluidBallPopulator(IBlockState fluidState, float springGenerationChance) {
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
        this.fluidState = possibleStates.stream().filter(it -> it.getPropertyKeys().contains(BlockFluidBase.LEVEL))
                .findFirst().orElse(null);
        if (fluidState == null) {
            String message = "Can't find fluid block for spring in vein %s. Blocks in vein: %s";
            throw new IllegalArgumentException(String.format(message, definition.getDepositName(), possibleStates));
        }
    }

    @Override
    public void populateBlockBuffer(Random random, GridEntryInfo gridEntryInfo, IBlockModifierAccess modifier,
                                    OreDepositDefinition depositDefinition) {
        if (random.nextFloat() <= springGenerationChance) {
            // 球体半径，随机在3-5格之间
            int radius = 3 + random.nextInt(3);
            int centerY = gridEntryInfo.getCenterPos(depositDefinition).getY();

            // 生成球体
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        // 计算距离中心的平方距离
                        double distanceSq = x * x + y * y + z * z;
                        // 判断是否在球体范围内
                        if (distanceSq <= radius * radius) {
                            // 设置流体方块，索引为0表示满流体
                            modifier.setBlock(x, centerY + y, z, 0);
                        }
                    }
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
