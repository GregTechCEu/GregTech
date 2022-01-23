package gregtech.client.utils;

import codechicken.lib.vec.Cuboid6;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBufferHelper {

    public static void renderCubeFrame(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    public static void renderCubeFace(BufferBuilder buffer, Cuboid6 cuboid, float r, float g, float b, float a) {
        renderCubeFace(buffer, cuboid.min.x, cuboid.min.y, cuboid.min.z, cuboid.max.x, cuboid.max.y, cuboid.max.z, r, g, b, a);
    }

    public static void renderCubeFace(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    public static void renderRing(BufferBuilder buffer, double x, double y, double z, double r, double tubeRadius, int sides,  int segments, float red, float green, float blue, float alpha, EnumFacing.Axis axis) {
        double sideDelta = 2.0 * Math.PI / sides;
        double ringDelta = 2.0 * Math.PI / segments;
        double theta = 0;
        double cosTheta = 1.0;
        double sinTheta = 0.0;

        double phi, sinPhi, cosPhi;
        double dist;

        for (int i = 0; i < segments; i++)
        {
            double theta1 = theta + ringDelta;
            double cosTheta1 = MathHelper.cos((float) theta1);
            double sinTheta1 = MathHelper.sin((float) theta1);

            phi = 0;
            for (int j = 0; j <= sides; j++)
            {
                phi = phi + sideDelta;
                cosPhi = MathHelper.cos((float)phi);
                sinPhi = MathHelper.sin((float)phi);
                dist = r + (tubeRadius * cosPhi);

                switch (axis) {
                    case Y:
                        buffer.pos(x + sinTheta * dist, y + tubeRadius * sinPhi, z + cosTheta * dist).color(red, green, blue, alpha).endVertex();
                        buffer.pos(x + sinTheta1 * dist, y + tubeRadius * sinPhi, z + cosTheta1 * dist).color(red, green, blue, alpha).endVertex();
                        break;
                    case X:
                        buffer.pos(x + tubeRadius * sinPhi, y + sinTheta * dist, z + cosTheta * dist).color(red, green, blue, alpha).endVertex();
                        buffer.pos(x + tubeRadius * sinPhi, y + sinTheta1 * dist, z + cosTheta1 * dist).color(red, green, blue, alpha).endVertex();
                        break;
                    case Z:
                        buffer.pos(x + cosTheta * dist, y + sinTheta * dist, z + tubeRadius * sinPhi).color(red, green, blue, alpha).endVertex();
                        buffer.pos(x + cosTheta1 * dist, y + sinTheta1 * dist, z + tubeRadius * sinPhi).color(red, green, blue, alpha).endVertex();
                        break;
                }

            }
            theta = theta1;
            cosTheta = cosTheta1;
            sinTheta = sinTheta1;

        }
    }


}
