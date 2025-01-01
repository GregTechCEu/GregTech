package gregtech.api.pattern;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

/**
 * Class allowing access to a block at a certain pos for structure checks and contains structure information.
 */
public class BlockWorldState {

    protected World world;
    protected BlockPos pos;
    protected IBlockState state;
    protected TileEntity tileEntity;
    protected boolean tileEntityInitialized;

    public void update(World worldIn, GreggyBlockPos pos) {
        this.world = worldIn;
        this.pos = pos.immutable();
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
    }

    public void setPos(GreggyBlockPos pos) {
        this.pos = pos.immutable();
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos.toImmutable();
        this.state = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
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
}
