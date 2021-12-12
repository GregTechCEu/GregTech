package gregtech.common.worldgen;

import gregtech.api.GTValues;
import gregtech.common.ConfigHolder;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenAbandonedBase implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (ConfigHolder.worldgen.abandonedBaseRarity == 0 ||
                world.getWorldType() == WorldType.FLAT ||
                world.provider.getDimensionType() != DimensionType.OVERWORLD ||
                !world.getWorldInfo().isMapFeaturesEnabled()) {
            return; //do not generate in flat worlds, or in non-surface worlds
        }
        BlockPos randomPos = new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8);

        if (random.nextInt(ConfigHolder.worldgen.abandonedBaseRarity) == 0) {
            int variantNumber = random.nextInt(3);
            Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            ResourceLocation templateId = new ResourceLocation(GTValues.MODID, "abandoned_base/abandoned_base_1_" + variantNumber);
            Template template = TemplateManager.getBuiltinTemplate(world, templateId);
            BlockPos originPos = template.getZeroPositionWithTransform(randomPos, Mirror.NONE, rotation);
            originPos = TemplateManager.calculateAverageGroundLevel(world, originPos, template.getSize());
            template.addBlocksToWorld(world, originPos, new PlacementSettings().setRotation(rotation));
        }
    }
}
