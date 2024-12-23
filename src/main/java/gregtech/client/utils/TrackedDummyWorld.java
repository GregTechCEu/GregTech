package gregtech.client.utils;

import gregtech.api.util.world.DummyWorld;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/25
 * @Description: TrackedDummyWorld. Used to build a Fake World.
 */
@SideOnly(Side.CLIENT)
public class TrackedDummyWorld extends DummyWorld {

    public final Set<BlockPos> renderedBlocks = new HashSet<>();
    private Predicate<BlockPos> renderFilter;
    private final World proxyWorld;

    private final Vector3f minPos = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final Vector3f maxPos = new Vector3f(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public void setRenderFilter(Predicate<BlockPos> renderFilter) {
        this.renderFilter = renderFilter;
    }

    public TrackedDummyWorld() {
        proxyWorld = null;
    }

    public TrackedDummyWorld(World world) {
        proxyWorld = world;
    }

    @Override
    public TileEntity getTileEntity(@NotNull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos))
            return null;
        return proxyWorld != null ? proxyWorld.getTileEntity(pos) : super.getTileEntity(pos);
    }

    @NotNull
    @Override
    public IBlockState getBlockState(@NotNull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos))
            return Blocks.AIR.getDefaultState(); // return air if not rendering this block
        return proxyWorld != null ? proxyWorld.getBlockState(pos) : super.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(@NotNull BlockPos pos, @NotNull IBlockState newState, int flags) {
        minPos.setX(Math.min(minPos.getX(), pos.getX()));
        minPos.setY(Math.min(minPos.getY(), pos.getY()));
        minPos.setZ(Math.min(minPos.getZ(), pos.getZ()));
        maxPos.setX(Math.max(maxPos.getX(), pos.getX()));
        maxPos.setY(Math.max(maxPos.getY(), pos.getY()));
        maxPos.setZ(Math.max(maxPos.getZ(), pos.getZ()));
        renderedBlocks.add(pos);
        return super.setBlockState(pos, newState, flags);
    }

    public Vector3f getSize() {
        Vector3f result = new Vector3f();
        result.setX(maxPos.getX() - minPos.getX() + 1);
        result.setY(maxPos.getY() - minPos.getY() + 1);
        result.setZ(maxPos.getZ() - minPos.getZ() + 1);
        return result;
    }

    public Vector3f getMinPos() {
        return minPos;
    }

    public Vector3f getMaxPos() {
        return maxPos;
    }
}
