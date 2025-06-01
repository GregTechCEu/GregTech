package gtqt.api.util.wireless;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class WorldBlockPos {
    private final int dimension;
    private final BlockPos pos;

    public WorldBlockPos(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    // 从NBT反序列化
    public static WorldBlockPos fromNBT(NBTTagCompound nbt) {
        return new WorldBlockPos(
                nbt.getInteger("dim"),
                new BlockPos(
                        nbt.getInteger("x"),
                        nbt.getInteger("y"),
                        nbt.getInteger("z")
                )
        );
    }

    // 序列化到NBT
    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("dim", dimension);
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        return nbt;
    }

    // Getter 方法
    public int getDimension() { return dimension; }
    public BlockPos getPos() { return pos; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldBlockPos that = (WorldBlockPos) o;
        return dimension == that.dimension &&
                pos.getX()==that.pos.getX()&&
                pos.getY()==that.pos.getY()&&
                pos.getZ()==that.pos.getZ();
    }

    @Override
    public int hashCode() {
        return 31 * dimension + pos.hashCode();
    }
}
