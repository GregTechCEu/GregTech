package gregtech.client.renderer.pipe.quad;

import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

@FunctionalInterface
public interface OverlayLayerDefinition {

    ImmutablePair<Vector3f, Vector3f> computeBox(@Nullable EnumFacing facing, float x1, float y1, float z1, float x2, float y2, float z2);
}
