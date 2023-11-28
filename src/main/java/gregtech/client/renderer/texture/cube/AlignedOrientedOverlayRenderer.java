package gregtech.client.renderer.texture.cube;

import net.minecraft.util.EnumFacing;

import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import org.jetbrains.annotations.NotNull;

public class AlignedOrientedOverlayRenderer extends OrientedOverlayRenderer {

    private static final Rotation DEF_ROT = new Rotation(0, 0, 0, 0);

    public AlignedOrientedOverlayRenderer(@NotNull String basePath) {
        super(basePath);
    }

    @Override
    public Rotation getRotation(Matrix4 transformation, EnumFacing renderSide, EnumFacing frontFacing) {
        if (renderSide == EnumFacing.EAST) {
            if (frontFacing == EnumFacing.NORTH) {
                transformation.translate(0, 1, 1);
                return new Rotation(Math.PI, 1, 0, 0);
            }
            if (frontFacing == EnumFacing.UP) {
                transformation.translate(0, 0, 1);
                return new Rotation(-Math.PI / 2, 1, 0, 0);
            }
            if (frontFacing == EnumFacing.DOWN) {
                transformation.translate(0, 1, 0);
                return new Rotation(Math.PI / 2, 1, 0, 0);
            }
            return DEF_ROT;
        }
        if (renderSide == EnumFacing.WEST) {
            if (frontFacing == EnumFacing.SOUTH) {
                transformation.translate(0, 1, 1);
                return new Rotation(Math.PI, 1, 0, 0);
            }
            if (frontFacing == EnumFacing.UP) {
                transformation.translate(0, 1, 0);
                return new Rotation(Math.PI / 2, 1, 0, 0);
            }
            if (frontFacing == EnumFacing.DOWN) {
                transformation.translate(0, 0, 1);
                return new Rotation(-Math.PI / 2, 1, 0, 0);
            }
            return DEF_ROT;
        }
        if (renderSide == EnumFacing.NORTH) {
            if (frontFacing == EnumFacing.WEST) {
                transformation.translate(1, 1, 0);
                return new Rotation(Math.PI, 0, 0, 1);
            }
            if (frontFacing == EnumFacing.UP) {
                transformation.translate(1, 0, 0);
                return new Rotation(Math.PI / 2, 0, 0, 1);
            }
            if (frontFacing == EnumFacing.DOWN) {
                transformation.translate(0, 1, 0);
                return new Rotation(-Math.PI / 2, 0, 0, 1);
            }
            return DEF_ROT;
        }
        if (renderSide == EnumFacing.SOUTH) {
            if (frontFacing == EnumFacing.EAST) {
                transformation.translate(1, 1, 0);
                return new Rotation(Math.PI, 0, 0, 1);
            }
            if (frontFacing == EnumFacing.UP) {
                transformation.translate(0, 1, 0);
                return new Rotation(-Math.PI / 2, 0, 0, 1);
            }
            if (frontFacing == EnumFacing.DOWN) {
                transformation.translate(1, 0, 0);
                return new Rotation(Math.PI / 2, 0, 0, 1);
            }
            return DEF_ROT;
        }
        return super.getRotation(transformation, renderSide, frontFacing);
    }
}
