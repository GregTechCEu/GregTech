package gregtech.api.pattern;

import gregtech.api.util.GTLog;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

/**
 * Class allowing access to a block at a certain pos for structure checks and contains structure information for legacy
 */
public class BlockWorldState {
    protected static boolean warned = false;
    protected World world;
    protected BlockPos pos;
    protected IBlockState state;
    protected TileEntity tileEntity;
    protected boolean tileEntityInitialized;
    protected final StructureInfo info;

    public BlockWorldState(StructureInfo info) {
        this.info = info;
    }

    public void update(World worldIn, GreggyBlockPos pos) {
        this.world = worldIn;
        this.pos = pos.immutable();
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
    }

    public void setPos(GreggyBlockPos pos) {
        setPos(pos.immutable());
    }

    public void setPos(BlockPos pos) {
        this.pos = pos.toImmutable();
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
    }

    @Deprecated
    public boolean hasError() {
        warn("hasError()");
        return info.getError() != null;
    }

    @Deprecated
    public void setError(PatternError error) {
        warn("setError(PatternError)");
        info.setError(error);
    }

    @Deprecated
    public PatternMatchContext getMatchContext() {
        warn("getMatchContext()");
        return info.getContext();
    }

    public StructureInfo getStructureInfo() {
        return this.info;
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

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    protected void warn(String name) {
        if (warned) return;

        GTLog.logger.warn("Calling " + name + " on BlockWorldState is deprecated! Use the method on StructureInfo, obtained via BlockWorldState#getStructureInfo() !");
        warned = true;
    }
}
