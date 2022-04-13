package gregtech.common.blocks.transport;

import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.common.metatileentities.transport.LongDistancePipeWalker;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class LongDistancePipelineBlock extends Block {

    public LongDistancePipelineBlock() {
        super(Material.IRON);
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @SuppressWarnings("deprecated")
    @Override
    public float getExplosionResistance(@Nonnull Entity exploder) {
        return 20;
    }

    @Override
    public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity exploder, @Nonnull Explosion explosion) {
        return 20;
    }

    protected abstract LongDistancePipeWalker getPipeWalker(World world, BlockPos pos);

    /**
     * @param block the block to check
     * @return {@code true} if the pipe block is valid for this pipeline, else {@code false}
     */
    protected abstract boolean isPipeBlockValid(IBlockState block);

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (!worldIn.isRemote) {
            LongDistancePipeWalker walker = getPipeWalker(worldIn, pos);
            walker.reset();
            walker.traversePipeNet();
            if (walker.getEndpoint() != null) {
                walker.getEndpoint().onPipeBlockChanged();
            }
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            LongDistancePipeWalker walker = getPipeWalker(worldIn, pos);
            walker.reset();
            walker.traversePipeNet();
            if (walker.getEndpoint() != null) {
                walker.getEndpoint().onPipeBlockChanged();
            }
        }
    }
}
