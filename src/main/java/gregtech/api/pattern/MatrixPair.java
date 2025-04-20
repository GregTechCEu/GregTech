package gregtech.api.pattern;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.util.vector.Matrix4f;

public class MatrixPair {
    public final Matrix4f mat = new Matrix4f(), inv = new Matrix4f();
    public final GreggyBlockPos dummy = new GreggyBlockPos();

    public MatrixPair translate(float x, float y, float z) {
        translate(x, y, z, mat, mat);
        translate(-x, -y, -z, inv, inv);
        return this;
    }

    /**
     *  Counterclockwise when looking negative direction from vector into origin.
     */
    public MatrixPair rotate(float a, float x, float y, float z) {
        rotate(a, x, y, z, mat, mat);
        rotate(-a, x, y, z, inv, inv);
        return this;
    }

    /**
     * Reflect across ax + by + cz + d = 0
     */
    public MatrixPair reflect(float a, float b, float c, float d) {
        reflect(a, b, c, d, mat, mat);
        reflect(a, b, c, d, inv, inv);
        return this;
    }

    public MatrixPair identity() {
        mat.setIdentity();
        inv.setIdentity();
        return this;
    }

    public GreggyBlockPos apply(GreggyBlockPos o) {
        return apply(mat, o);
    }

    public GreggyBlockPos unapply(GreggyBlockPos o) {
        return apply(inv, o);
    }

    public GreggyBlockPos apply(BlockPos o) {
        return apply(mat, dummy.from(o));
    }

    public GreggyBlockPos unapply(BlockPos o) {
        return apply(inv, dummy.from(o));
    }

    public static GreggyBlockPos apply(Matrix4f src, GreggyBlockPos dest) {
        float x = dest.x() * src.m00 + dest.y() * src.m10 + dest.z() * src.m20 + src.m30;
        float y = dest.x() * src.m01 + dest.y() * src.m11 + dest.z() * src.m21 + src.m31;
        float z = dest.x() * src.m02 + dest.y() * src.m12 + dest.z() * src.m22 + src.m32;
        dest.x(MathHelper.floor(x + 0.5f));
        dest.y(MathHelper.floor(y + 0.5f));
        dest.z(MathHelper.floor(z + 0.5f));
        return dest;
    }

    public static void serialize(Matrix4f src, PacketBuffer buf) {
        buf.writeFloat(src.m00)
                .writeFloat(src.m01)
                .writeFloat(src.m02)
                .writeFloat(src.m03)
                .writeFloat(src.m10)
                .writeFloat(src.m11)
                .writeFloat(src.m12)
                .writeFloat(src.m13)
                .writeFloat(src.m20)
                .writeFloat(src.m21)
                .writeFloat(src.m22)
                .writeFloat(src.m23)
                .writeFloat(src.m30)
                .writeFloat(src.m31)
                .writeFloat(src.m32)
                .writeFloat(src.m33);
    }

    public static void deserialize(Matrix4f dest, PacketBuffer buf) {
        dest.m00 = buf.readFloat();
        dest.m01 = buf.readFloat();
        dest.m02 = buf.readFloat();
        dest.m03 = buf.readFloat();
        dest.m10 = buf.readFloat();
        dest.m11 = buf.readFloat();
        dest.m12 = buf.readFloat();
        dest.m13 = buf.readFloat();
        dest.m20 = buf.readFloat();
        dest.m21 = buf.readFloat();
        dest.m22 = buf.readFloat();
        dest.m23 = buf.readFloat();
        dest.m30 = buf.readFloat();
        dest.m31 = buf.readFloat();
        dest.m32 = buf.readFloat();
        dest.m33 = buf.readFloat();
    }

    // all below stolen from JOML, shoutout to MIT
    public static Matrix4f reflect(float a, float b, float c, float d, Matrix4f src, Matrix4f dest) {
        float da = a + a, db = b + b, dc = c + c, dd = d + d;
        float rm00 = 1.0f - da * a;
        float rm01 = -da * b;
        float rm02 = -da * c;
        float rm10 = -db * a;
        float rm11 = 1.0f - db * b;
        float rm12 = -db * c;
        float rm20 = -dc * a;
        float rm21 = -dc * b;
        float rm22 = 1.0f - dc * c;
        float rm30 = -dd * a;
        float rm31 = -dd * b;
        float rm32 = -dd * c;
        // matrix multiplication
        dest.m30 = src.m00 * rm30 + src.m10 * rm31 + src.m20 * rm32 + src.m30;
        dest.m31 = src.m01 * rm30 + src.m11 * rm31 + src.m21 * rm32 + src.m31;
        dest.m32 = src.m02 * rm30 + src.m12 * rm31 + src.m22 * rm32 + src.m32;
        dest.m33 = src.m03 * rm30 + src.m13 * rm31 + src.m23 * rm32 + src.m33;
        float nm00 = src.m00 * rm00 + src.m10 * rm01 + src.m20 * rm02;
        float nm01 = src.m01 * rm00 + src.m11 * rm01 + src.m21 * rm02;
        float nm02 = src.m02 * rm00 + src.m12 * rm01 + src.m22 * rm02;
        float nm03 = src.m03 * rm00 + src.m13 * rm01 + src.m23 * rm02;
        float nm10 = src.m00 * rm10 + src.m10 * rm11 + src.m20 * rm12;
        float nm11 = src.m01 * rm10 + src.m11 * rm11 + src.m21 * rm12;
        float nm12 = src.m02 * rm10 + src.m12 * rm11 + src.m22 * rm12;
        float nm13 = src.m03 * rm10 + src.m13 * rm11 + src.m23 * rm12;
        dest.m20 = src.m00 * rm20 + src.m10 * rm21 + src.m20 * rm22;
        dest.m21 = src.m01 * rm20 + src.m11 * rm21 + src.m21 * rm22;
        dest.m22 = src.m02 * rm20 + src.m12 * rm21 + src.m22 * rm22;
        dest.m23 = src.m03 * rm20 + src.m13 * rm21 + src.m23 * rm22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m03 = nm03;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m13 = nm13;
        return dest;
    }

    public static Matrix4f reflect(float nx, float ny, float nz, float px, float py, float pz, Matrix4f src,
                                   Matrix4f dest) {
        float invLength = 1 / MathHelper.sqrt(nx * nx + ny * ny + nz * nz);
        float nnx = nx * invLength;
        float nny = ny * invLength;
        float nnz = nz * invLength;
        /* See: http://mathworld.wolfram.com/Plane.html */
        return reflect(nnx, nny, nnz, -nnx * px - nny * py - nnz * pz, src, dest);
    }

    public static Matrix4f rotate(float ang, float x, float y, float z, Matrix4f src, Matrix4f dest) {
        float s = MathHelper.sin(ang);
        float c = MathHelper.cos(ang);
        float C = 1.0f - c;
        float xx = x * x, xy = x * y, xz = x * z;
        float yy = y * y, yz = y * z;
        float zz = z * z;
        float rm00 = xx * C + c;
        float rm01 = xy * C + z * s;
        float rm02 = xz * C - y * s;
        float rm10 = xy * C - z * s;
        float rm11 = yy * C + c;
        float rm12 = yz * C + x * s;
        float rm20 = xz * C + y * s;
        float rm21 = yz * C - x * s;
        float rm22 = zz * C + c;
        float nm00 = src.m00 * rm00 + src.m10 * rm01 + src.m20 * rm02;
        float nm01 = src.m01 * rm00 + src.m11 * rm01 + src.m21 * rm02;
        float nm02 = src.m02 * rm00 + src.m12 * rm01 + src.m22 * rm02;
        float nm03 = src.m03 * rm00 + src.m13 * rm01 + src.m23 * rm02;
        float nm10 = src.m00 * rm10 + src.m10 * rm11 + src.m20 * rm12;
        float nm11 = src.m01 * rm10 + src.m11 * rm11 + src.m21 * rm12;
        float nm12 = src.m02 * rm10 + src.m12 * rm11 + src.m22 * rm12;
        float nm13 = src.m03 * rm10 + src.m13 * rm11 + src.m23 * rm12;
        dest.m20 = src.m00 * rm20 + src.m10 * rm21 + src.m20 * rm22;
        dest.m21 = src.m01 * rm20 + src.m11 * rm21 + src.m21 * rm22;
        dest.m22 = src.m02 * rm20 + src.m12 * rm21 + src.m22 * rm22;
        dest.m23 = src.m03 * rm20 + src.m13 * rm21 + src.m23 * rm22;
        dest.m00 = nm00;
        dest.m01 = nm01;
        dest.m02 = nm02;
        dest.m03 = nm03;
        dest.m10 = nm10;
        dest.m11 = nm11;
        dest.m12 = nm12;
        dest.m13 = nm13;
        dest.m30 = src.m30;
        dest.m31 = src.m31;
        dest.m32 = src.m32;
        dest.m33 = src.m33;
        return dest;
    }

    public static Matrix4f translate(float x, float y, float z, Matrix4f src, Matrix4f dest) {
        dest.load(src);
        dest.m30 = src.m00 * x + src.m10 * y + src.m20 * z + src.m30;
        dest.m31 = src.m01 * x + src.m11 * y + src.m21 * z + src.m31;
        dest.m32 = src.m02 * x + src.m12 * y + src.m22 * z + src.m32;
        dest.m33 = src.m03 * x + src.m13 * y + src.m23 * z + src.m33;
        return dest;
    }
}
