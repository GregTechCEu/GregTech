package gregtech.api.worldgen.bedrockFluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

import org.jetbrains.annotations.NotNull;

public class ChunkPosDimension extends ChunkPos {

    public int dimension;

    public ChunkPosDimension(int dimension, int x, int z) {
        super(x, z);
        this.dimension = dimension;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (!(object instanceof ChunkPosDimension))
            return false;

        ChunkPosDimension coordinatePair = (ChunkPosDimension) object;
        return this.dimension == coordinatePair.dimension && this.x == coordinatePair.x && this.z == coordinatePair.z;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ (31 + dimension);
    }

    @NotNull
    @Override
    public String toString() {
        return "[dim:" + this.dimension + "; " + this.x + ", " + this.z + "]";
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", dimension);
        tag.setInteger("x", this.x);
        tag.setInteger("z", this.z);
        return tag;
    }

    public static ChunkPosDimension readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("dimension", 3) && tag.hasKey("x", 3) && tag.hasKey("z", 3))
            return new ChunkPosDimension(tag.getInteger("dimension"), tag.getInteger("x"), tag.getInteger("z"));
        return null;
    }
}
