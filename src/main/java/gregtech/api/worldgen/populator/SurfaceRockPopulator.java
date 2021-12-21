package gregtech.api.worldgen.populator;

import com.google.gson.JsonObject;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.config.OreConfigUtils;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;
import gregtech.common.MetaFluids;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SurfaceRockPopulator implements VeinChunkPopulator {

    private Material material;
    private int failedGenerationCounter = 0;

    public SurfaceRockPopulator() {
    }

    public SurfaceRockPopulator(Material material) {
        this.material = material;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.material = OreConfigUtils.getMaterialByName(object.get("material").getAsString());
    }

    @Override
    public void initializeForVein(OreDepositDefinition definition) {
    }

    private Set<Material> findUndergroundMaterials(Collection<IBlockState> generatedBlocks) {
        HashSet<Material> result = new HashSet<>();
        for (IBlockState blockState : generatedBlocks) {
            Material resultMaterial;
            if (blockState.getBlock() instanceof IFluidBlock || blockState.getBlock() instanceof BlockLiquid) {
                Fluid fluid = FluidRegistry.lookupFluidForBlock(blockState.getBlock());
                resultMaterial = fluid == null ? null : MetaFluids.getMaterialFromFluid(fluid);
            } else {
                ItemStack itemStack = new ItemStack(blockState.getBlock(), 1, blockState.getBlock().damageDropped(blockState));
                UnificationEntry entry = OreDictUnifier.getUnificationEntry(itemStack);
                if (entry != null && entry.material != null && entry.material.hasProperty(PropertyKey.ORE))
                    resultMaterial = entry.material;
                else
                    resultMaterial = null;
            }
            if (resultMaterial != null) {
                result.add(resultMaterial);
            }
        }
        return result;
    }

    private void setStoneBlock(World world, BlockPos blockPos, Collection<Material> undergroundMaterials) {
        for (Material material : undergroundMaterials) {
            boolean surfaceRockPlaced = world.setBlockState(blockPos, MetaBlocks.SURFACE_ROCK.get(material).getDefaultState());
            if (!surfaceRockPlaced) {
                failedGenerationCounter++;
            }
        }
    }

    /**
     * Generates the Surface Rock for an underground vein. Replaces the applicable topmost block in the chunk with a
     * Surface Rock, at a random position in the chunk. Does not run on a Flat world type
     *
     * @param world         - The Minecraft world. Used for finding the top most block and its state
     * @param chunkX        - The X chunk coordinate
     * @param chunkZ        - The Z chunk coordinate
     * @param random        - A Random parameter. Used for determining the number of spawned Surface Blocks and their position
     * @param definition    - The Ore Vein definition
     * @param gridEntryInfo - Information about the ore generation grid for the current generation section
     */
    @Override
    public void populateChunk(World world, int chunkX, int chunkZ, Random random, OreDepositDefinition definition, GridEntryInfo gridEntryInfo) {
        int stonesCount = random.nextInt(2);
        if (stonesCount > 0 && world.getWorldType() != WorldType.FLAT) {
            Set<Material> undergroundMaterials = findUndergroundMaterials(gridEntryInfo.getGeneratedBlocks(definition, chunkX, chunkZ));
            if (undergroundMaterials.isEmpty())
                return;
            for (int i = 0; i < stonesCount; i++) {
                int randomX = chunkX * 16 + 8 + random.nextInt(16);
                int randomZ = chunkZ * 16 + 8 + random.nextInt(16);
                BlockPos topBlockPos = world.getTopSolidOrLiquidBlock(new BlockPos(randomX, 0, randomZ));
                Block blockAtPos = world.getBlockState(topBlockPos).getBlock();

                //Checks if the block is a replaceable feature like grass, snow layers, or Air. Liquids are replaceable, so
                // exclude one deep liquid blocks, for looks
                if (!blockAtPos.isReplaceable(world, topBlockPos) || world.getBlockState(topBlockPos).getMaterial().isLiquid()) {
                    continue;
                }

                //Checks if the block below has a solid top. This method is also used to check what blocks redstone can
                //be placed on.
                if (!world.isSideSolid(topBlockPos.down(), EnumFacing.UP)) {
                    continue;
                }

                setStoneBlock(world, topBlockPos, undergroundMaterials);
            }
        }

        //Log if all Surface Rock generation attempts were failed
        if (failedGenerationCounter == stonesCount && stonesCount > 0 && world.getWorldType() != WorldType.FLAT) {
            GTLog.logger.debug("Failed to generate surface rocks for vein {} at chunk with position: x: {}, z: {}", definition.getDepositName(), chunkX, chunkZ);
        }
    }

    public Material getMaterial() {
        return material;
    }
}
