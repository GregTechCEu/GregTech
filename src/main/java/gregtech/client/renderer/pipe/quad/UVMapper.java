package gregtech.client.renderer.pipe.quad;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

@FunctionalInterface
@SideOnly(Side.CLIENT)
public interface UVMapper {

    Int2ObjectOpenHashMap<UVMapper> CACHE = new Int2ObjectOpenHashMap<>();

    static UVMapper standard(int rot) {
        return CACHE.computeIfAbsent(rot, (r) -> (normal, box) -> {
            Vector3f small = box.getLeft();
            Vector3f large = box.getRight();
            return switch (normal.getAxis()) {
                case X -> new BlockFaceUV(new float[] { small.y, large.z, large.y, small.z }, r);
                case Y -> new BlockFaceUV(new float[] { small.x, large.z, large.x, small.z }, r);
                case Z -> new BlockFaceUV(new float[] { small.x, large.y, large.x, small.y }, r);
            };
        });
    }

    BlockFaceUV map(EnumFacing normal, Pair<Vector3f, Vector3f> box);
}
