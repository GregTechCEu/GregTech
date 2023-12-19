package gregtech.api.pattern;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BlockWorldState {

    protected World world;
    protected BlockPos pos;
    protected IBlockState state;
    protected TileEntity tileEntity;
    protected boolean tileEntityInitialized;
    protected PatternMatchContext matchContext;
    protected Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount;
    protected Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount;
    protected TraceabilityPredicate predicate;
    protected PatternError error;

    public void update(World worldIn, BlockPos posIn, PatternMatchContext matchContext,
                       Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount,
                       Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount,
                       TraceabilityPredicate predicate) {
        this.world = worldIn;
        this.pos = posIn;
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
        this.matchContext = matchContext;
        this.globalCount = globalCount;
        this.layerCount = layerCount;
        this.predicate = predicate;
        this.error = null;
    }

    public boolean hasError() {
        return error != null;
    }

    public void setError(PatternError error) {
        this.error = error;
        if (error != null) {
            error.setWorldState(this);
        }
    }

    public PatternMatchContext getMatchContext() {
        return matchContext;
    }

    public IBlockState getBlockState() {
        if (this.state == null) {
            this.state = this.world.getBlockState(this.pos);
        }

        return this.state;
    }

    @Nullable
    public TileEntity getTileEntity() {
        if (this.tileEntity == null && !this.tileEntityInitialized) {
            this.tileEntity = this.world.getTileEntity(this.pos);
            this.tileEntityInitialized = true;
        }

        return this.tileEntity;
    }

    public BlockPos getPos() {
        return this.pos.toImmutable();
    }

    public IBlockState getOffsetState(EnumFacing face) {
        if (pos instanceof MutableBlockPos) {
            ((MutableBlockPos) pos).move(face);
            IBlockState blockState = world.getBlockState(pos);
            ((MutableBlockPos) pos).move(face.getOpposite());
            return blockState;
        }
        return world.getBlockState(this.pos.offset(face));
    }

    public World getWorld() {
        return world;
    }
}
