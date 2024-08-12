package gregtech.client.renderer.pipe.quad;

import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

@FunctionalInterface
@SideOnly(Side.CLIENT)
public interface UVMapper {

    static UVMapper standard(int rot) {
        return (normal, box) -> {
            Vector3f small = box.getLeft();
            Vector3f large = box.getRight();
            return switch (normal.getAxis()) {
                case X -> new BlockFaceUV(new float[] { small.y, large.z, large.y, small.z }, rot);
                case Y -> new BlockFaceUV(new float[] { small.x, large.z, large.x, small.z }, rot);
                case Z -> new BlockFaceUV(new float[] { small.x, large.y, large.x, small.y }, rot);
            };
        };
    }

    BlockFaceUV map(EnumFacing normal, Pair<Vector3f, Vector3f> box);
}
