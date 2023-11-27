package gregtech.client.utils;

import gregtech.api.pipenet.IBlockAppearance;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import static net.minecraft.util.EnumFacing.*;

/**
 * Mostly based on and (copied from) ThermalDynamics with minor tweaks
 * https://github.com/CoFH/ThermalDynamics/
 */
@SideOnly(Side.CLIENT)
public class FacadeBlockAccess implements IBlockAccess {

    public final IBlockAccess world;
    public final BlockPos pos;
    public final EnumFacing side;
    public final IBlockState state;

    public FacadeBlockAccess(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state) {
        this.world = world;
        this.pos = pos;
        this.side = side;
        this.state = state;
    }

    public enum Result {
        ORIGINAL,
        AIR,
        BASE,
        BEDROCK,
        COVER
    }

    public Result getAction(BlockPos pos) {
        if (this.pos == pos) {
            return Result.BASE;
        }
        if (pos == this.pos.offset(side)) {
            return Result.ORIGINAL;
        }
        IBlockState worldState = world.getBlockState(pos);
        Block worldBlock = worldState.getBlock();

        if (worldBlock instanceof IBlockAppearance) {
            IBlockAppearance blockAppearance = (IBlockAppearance) worldBlock;
            if (blockAppearance.supportsVisualConnections()) {
                return Result.COVER;
            }

            if (blockAppearance.getVisualState(world, pos, side).equals(state)) {
                return state.isNormalCube() ? Result.BEDROCK : Result.AIR;
            } else {
                return Result.COVER;
            }
        } else {
            return Result.ORIGINAL;
        }
    }

    @NotNull
    @Override
    public IBlockState getBlockState(@NotNull BlockPos pos) {
        IBlockState ret;
        Result action = getAction(pos);
        if (action == Result.ORIGINAL) {
            ret = world.getBlockState(pos);
        } else if (action == Result.AIR) {
            ret = Blocks.AIR.getDefaultState();
        } else if (action == Result.BEDROCK) {
            ret = Blocks.BEDROCK.getDefaultState();
        } else if (action == Result.COVER) {
            ret = ((IBlockAppearance) world.getBlockState(pos).getBlock()).getVisualState(world, pos, side);
        } else {
            ret = state;
        }
        return ret;
    }

    @Override
    public TileEntity getTileEntity(@NotNull BlockPos pos) {
        return getAction(pos) == Result.ORIGINAL ? world.getTileEntity(pos) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(@NotNull BlockPos pos, int t) {
        if (((side == DOWN && pos.getY() > this.pos.getY()) ||
                (side == UP && pos.getY() < this.pos.getY()) ||
                (side == NORTH && pos.getZ() > this.pos.getZ()) ||
                (side == SOUTH && pos.getZ() < this.pos.getZ()) ||
                (side == WEST && pos.getX() > this.pos.getX()) ||
                (side == EAST && pos.getX() < this.pos.getX()))) {
            return world.getCombinedLight(this.pos, t);
        }
        return world.getCombinedLight(pos, t);
    }

    @Override
    public int getStrongPower(@NotNull BlockPos pos, @NotNull EnumFacing side) {
        return world.getStrongPower(pos, side);
    }

    @NotNull
    @Override
    public WorldType getWorldType() {
        return world.getWorldType();
    }

    @Override
    public boolean isAirBlock(@NotNull BlockPos pos) {
        Result action = getAction(pos);
        return action == Result.AIR ||
                (action == Result.ORIGINAL && world.isAirBlock(pos)) ||
                (action == Result.COVER && getBlockState(pos).getBlock().isAir(getBlockState(pos), this, pos));
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public Biome getBiome(@NotNull BlockPos pos) {
        return world.getBiome(pos);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, @NotNull EnumFacing side, boolean _default) {
        if (pos.getX() < -30000000 ||
                pos.getZ() < -30000000 ||
                pos.getX() >= 30000000 ||
                pos.getZ() >= 30000000) {
            return _default;
        } else {
            return getBlockState(pos).isSideSolid(this, pos, side);
        }
    }
}
