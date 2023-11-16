package gregtech.client.utils;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.model.miningpipe.MiningPipeModel;
import gregtech.common.entities.MiningPipeEntity;
import gregtech.common.metatileentities.miner.Miner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;

public class MinerRenderHelper {

    private MinerRenderHelper() {}

    public static final ResourceLocation MINER_AREA_PREVIEW_TEXTURE = GTUtility.gregtechId("textures/fx/miner_area_preview.png");

    private static final long TEXTURE_WRAP_INTERVAL_NANOSECONDS = 3_000_000_000L;

    private static final ClippingHelperImpl clippingHelper = new ClippingHelperImpl();

    private static final Vector3f[] nearPlaneVectors = {
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()
    };

    // 3*4 augmented matrix, last 4 elements are for swap buffer (because im a good progranmer)
    private static final float[] mat = new float[16];

    // reusing static field to prevent heap pollution
    private static final SATTestResult sat = new SATTestResult();
    private static final Vector3f vec1 = new Vector3f(), vec2 = new Vector3f();

    private static int prevFrameIndex = -1;

    private static void updateFrustum() {
        int index = Minecraft.getMinecraft().frameTimer.getIndex();
        if (prevFrameIndex == index) return;
        prevFrameIndex = index;
        clippingHelper.init();
    }

    public static <MTE extends MetaTileEntity & Miner> void renderPipe(double x, double y, double z, float partialTicks,
                                                                       @Nonnull MiningPipeEntity<MTE> entity) {
        if (entity.getMTE() == null || entity.length <= 0) return;
        updateFrustum();

        x -= entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        y -= entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        z -= entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        Minecraft mc = Minecraft.getMinecraft();
        BlockPos origin = entity.getOrigin();
        MutableBlockPos mpos = new MutableBlockPos(origin);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        MiningPipeModel model = entity.getMTE().getMiningPipeModel();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.shadeModel(Minecraft.isAmbientOcclusionEnabled() ? GL11.GL_SMOOTH : GL11.GL_FLAT);
        GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
        buffer.setTranslation(x, y, z);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (int i = 0, len = entity.length; i < len; i++) {
            mpos.setY(entity.y - len + i);
            // me epicly dodging AABB allocations by plugging in primitive values directly (very epic)
            if (!clippingHelper.isBoxInFrustum(
                    mpos.getX() + .25 + x, mpos.getY() + y, mpos.getZ() + .25 + z,
                    mpos.getX() + .75 + x, mpos.getY() + 1 + y, mpos.getZ() + .75 + z)) {
                continue;
            }

            mc.blockRenderDispatcher.getBlockModelRenderer().renderModel(entity.world,
                    i == (len - 1) && entity.end ? model.getBottomModel() : model.getBaseModel(),
                    entity.world.getBlockState(mpos), mpos, buffer, false);
        }
        buffer.setTranslation(0, 0, 0);
        t.draw();
        RenderHelper.enableStandardItemLighting();
    }

    /**
     * Draw an area preview.
     *
     * @param box area
     * @param pos block position
     * @param x   X position; generally {@code cameraX + blockX}
     * @param y   Y position; generally {@code cameraY + blockY}
     * @param z   Z position; generally {@code cameraZ + blockZ}
     */
    public static void renderAreaPreview(@Nonnull AxisAlignedBB box, @Nonnull BlockPos pos,
                                         double x, double y, double z) {
        // skull emoji

        // positions
        double minX = box.minX + x - pos.getX(), maxX = box.maxX + x - pos.getX();
        double minY = Math.max(0, box.minY) + y - pos.getY(), maxY = box.maxY + y - pos.getY();
        double minZ = box.minZ + z - pos.getZ(), maxZ = box.maxZ + z - pos.getZ();

        boolean isBoxClippingThroughCamera = isBoxClippingThroughCamera(minX, maxX, minY, maxY, minZ, maxZ);

        // texture UVs
        double texOffset = (System.nanoTime() % TEXTURE_WRAP_INTERVAL_NANOSECONDS) / (double) (TEXTURE_WRAP_INTERVAL_NANOSECONDS);

        double dx = (box.maxX - box.minX);
        double dy = (box.maxY - Math.max(0, box.minY));
        double dz = (box.maxZ - box.minZ);

        double uMax = texOffset + box.maxX - Math.floor(box.maxX);
        double uMax2 = uMax - dy;
        double uMin = uMax - dx;
        double uMin2 = uMin - dy;
        double vMax = texOffset + box.maxZ - Math.floor(box.maxZ);
        double vMax2 = vMax - dy;
        double vMin = vMax - dz;
        double vMin2 = vMin - dy;
        double vMin3 = vMin2 + dz;

        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        Minecraft.getMinecraft().getTextureManager().bindTexture(MINER_AREA_PREVIEW_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (boolean looped = false; ; looped = true) {
            int alpha = looped ? 70 : 200;

            if (looped) {
                GlStateManager.disableDepth();
                // only draw inner parts of the border when camera is inside it
                if (isBoxClippingThroughCamera) {
                    GlStateManager.disableCull();
                }
            }

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

            // UP
            v(buffer, minX, maxY, maxZ, uMin, vMax, alpha);
            v(buffer, maxX, maxY, maxZ, uMax, vMax, alpha);
            v(buffer, maxX, maxY, minZ, uMax, vMin, alpha);
            v(buffer, minX, maxY, minZ, uMin, vMin, alpha);

            // NORTH
            v(buffer, minX, maxY, minZ, uMin, vMin, alpha);
            v(buffer, maxX, maxY, minZ, uMax, vMin, alpha);
            v(buffer, maxX, minY, minZ, uMax, vMin2, alpha);
            v(buffer, minX, minY, minZ, uMin, vMin2, alpha);

            // SOUTH
            v(buffer, minX, minY, maxZ, uMin, vMax2, alpha);
            v(buffer, maxX, minY, maxZ, uMax, vMax2, alpha);
            v(buffer, maxX, maxY, maxZ, uMax, vMax, alpha);
            v(buffer, minX, maxY, maxZ, uMin, vMax, alpha);

            // WEST
            v(buffer, minX, minY, maxZ, uMin2, vMax, alpha);
            v(buffer, minX, maxY, maxZ, uMin, vMax, alpha);
            v(buffer, minX, maxY, minZ, uMin, vMin, alpha);
            v(buffer, minX, minY, minZ, uMin2, vMin, alpha);

            // EAST
            v(buffer, maxX, minY, minZ, uMax2, vMin, alpha);
            v(buffer, maxX, maxY, minZ, uMax, vMin, alpha);
            v(buffer, maxX, maxY, maxZ, uMax, vMax, alpha);
            v(buffer, maxX, minY, maxZ, uMax2, vMax, alpha);

            // DOWN
            v(buffer, minX, minY, minZ, uMin, vMin2, alpha);
            v(buffer, maxX, minY, minZ, uMax, vMin2, alpha);
            v(buffer, maxX, minY, maxZ, uMax, vMin3, alpha);
            v(buffer, minX, minY, maxZ, uMin, vMin3, alpha);

            tessellator.draw();

            if (looped) {
                GlStateManager.enableDepth();
                if (isBoxClippingThroughCamera) {
                    GlStateManager.enableCull();
                }
                break;
            }
        }

        GlStateManager.enableLighting();
    }

    private static void v(BufferBuilder buffer, double x, double y, double z, double u, double v, int alpha) {
        buffer.pos(x, y, z).tex(u, v).lightmap(240, 240).color(255, 255, 255, alpha).endVertex();
    }

    /**
     * Check if given AABB is clipping through camera.
     */
    public static boolean isBoxClippingThroughCamera(double minX, double maxX,
                                                     double minY, double maxY,
                                                     double minZ, double maxZ) {
        updateFrustum();

        // obtain 4 vertices of near plane rectangle
        // just halt and return false as a fallback, theoretically possible if view matrix got somehow borked
        if (!calculateIntersectingPoint(0, 0, 2)) return false;
        if (!calculateIntersectingPoint(1, 1, 2)) return false;
        if (!calculateIntersectingPoint(2, 1, 3)) return false;
        if (!calculateIntersectingPoint(3, 0, 3)) return false;

        // divide the near plane rectangle to 2 triangles, then do some intersection tests
        float minXf = (float) minX, maxXf = (float) maxX,
                minYf = (float) minY, maxYf = (float) maxY,
                minZf = (float) minZ, maxZf = (float) maxZ;
        return intersects(0, 1, 2, minXf, maxXf, minYf, maxYf, minZf, maxZf) ||
                intersects(0, 2, 3, minXf, maxXf, minYf, maxYf, minZf, maxZf);
    }

    /**
     * Tries to calculate an intersecting point between 2 specified planes and the near plane. If an intersecting point
     * can be derived, {@code true} will be returned and the value will be stored in {@code vectorIndex}-th element of
     * {@link #nearPlaneVectors}. If planes have no single intersecting point (either because they don't intersect, or
     * have infinitely many solutions), {@code false} will be returned.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean calculateIntersectingPoint(int vectorIndex, int plane1, int plane2) {
        System.arraycopy(clippingHelper.frustum[5], 0, mat, 0, 4); // near plane
        System.arraycopy(clippingHelper.frustum[plane1], 0, mat, 4, 4);
        System.arraycopy(clippingHelper.frustum[plane2], 0, mat, 8, 4);

        // shoutouts to wikipedia and gauss and row-echelon or sth idk
        for (int i = 0; i < 3; i++) {
            // find the k-th pivot
            int maxRow = 0;
            float maxValue = -1;
            for (int r = i; r < 3; r++) {
                float abs = Math.abs(getMatrixValue(i, r));
                if (maxValue < abs) {
                    maxRow = r;
                    maxValue = abs;
                }
            }

            // no pivot in this column, this means the system has no one unique solution
            // since we don't care about calculating intersections in a form other than a point, we can just halt here
            if (maxValue == 0) return false;

            swapRow(i, maxRow);
            // do for all rows below pivot
            for (int r = i + 1; r < 3; r++) {
                float f = getMatrixValue(i, r) / getMatrixValue(i, i);
                // fill with zeros the lower part of pivot column
                setMatrixValue(i, r, 0);
                for (int c = i + 1; c < 4; c++) {
                    setMatrixValue(c, r, getMatrixValue(c, r) - getMatrixValue(c, i) * f);
                }
            }
        }

        // back substitution???? i guess??????
        float z = getMatrixValue(3, 2) / getMatrixValue(2, 2);
        float y = (getMatrixValue(3, 1) - z * getMatrixValue(2, 1)) / getMatrixValue(1, 1);
        float x = (getMatrixValue(3, 0) - z * getMatrixValue(2, 0) - y * getMatrixValue(1, 0)) / getMatrixValue(0, 0);

        // idk
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) return false;
        nearPlaneVectors[vectorIndex].set(-x, -y, -z);
        return true;
    }

    /**
     * Check if a triangle made of {@code p1}, {@code p2}, and {@code p3}-th element of {@link #nearPlaneVectors}
     * intersects with given AABB. (<a href="https://stackoverflow.com/questions/17458562/">Source</a>)
     */
    @SuppressWarnings("SameParameterValue")
    private static boolean intersects(int p1, int p2, int p3,
                                      float minX, float maxX,
                                      float minY, float maxY,
                                      float minZ, float maxZ) {
        // Test the box normals (x-, y- and z-axes)
        for (int i = 0; i < 3; i++) {
            sat.projectTriangle(p1, p2, p3, i == 0 ? 1 : 0, i == 1 ? 1 : 0, i == 2 ? 1 : 0);
            if (sat.max < switch (i) {
                case 0 -> minX;
                case 1 -> minY;
                default -> minZ;
            } || sat.min > switch (i) {
                case 0 -> maxX;
                case 1 -> maxY;
                default -> maxZ;
            }) return false;
        }

        // Test the triangle normal
        vec1.sub(nearPlaneVectors[p2], nearPlaneVectors[p1]);
        vec2.sub(nearPlaneVectors[p3], nearPlaneVectors[p1]);
        vec1.cross(vec1, vec2);
        vec1.normalize();
        double triangleOffset = vec1.dot(nearPlaneVectors[p1]);
        sat.projectAABB(minX, maxX, minY, maxY, minZ, maxZ,
                vec1.x, vec1.y, vec1.z);
        if (sat.max < triangleOffset || sat.min > triangleOffset) return false;

        // Test the nine edge cross-products
        for (int i = 0; i < 3; i++) {
            // The box normals are the same as it's edge tangents
            vec1.sub(nearPlaneVectors[(i) % 3], nearPlaneVectors[(i + 1) % 3]);
            for (int j = 0; j < 3; j++) {
                vec2.set(j == 0 ? 1 : 0, j == 1 ? 1 : 0, j == 2 ? 1 : 0);
                vec2.cross(vec1, vec2);

                sat.projectAABB(minX, maxX, minY, maxY, minZ, maxZ, vec2.x, vec2.y, vec2.z);
                float boxMin = sat.min, boxMax = sat.max;
                sat.projectTriangle(p1, p2, p3, vec2.x, vec2.y, vec2.z);
                if (boxMax <= sat.min || boxMin >= sat.max) return false;
            }
        }

        // No separating axis found.
        return true;
    }

    private static float getMatrixValue(int col, int row) {
        if (col < 0 || col >= 4) throw new IndexOutOfBoundsException("col == " + col);
        if (row < 0 || row >= 3) throw new IndexOutOfBoundsException("row == " + row);
        return mat[row * 4 + col];
    }

    private static void setMatrixValue(int col, int row, float val) {
        if (col < 0 || col >= 4) throw new IndexOutOfBoundsException("col == " + col);
        if (row < 0 || row >= 3) throw new IndexOutOfBoundsException("row == " + row);
        mat[row * 4 + col] = val;
    }

    private static void swapRow(int r1, int r2) {
        if (r1 < 0 || r1 >= 3) throw new IndexOutOfBoundsException("r1 == " + r1);
        if (r2 < 0 || r2 >= 3) throw new IndexOutOfBoundsException("r2 == " + r2);
        if (r1 == r2) return;
        System.arraycopy(mat, r1 * 4, mat, 12, 4);
        System.arraycopy(mat, r2 * 4, mat, r1 * 4, 4);
        System.arraycopy(mat, 12, mat, r2 * 4, 4);
    }

    /* for debug
    private static String matrixToString() {
        StringBuilder stb = new StringBuilder();
        for (int r = 0; r < 3; r++) {
            if (r != 0) stb.append('\n');
            for (int c = 0; c < 4; c++) {
                if (c != 0) stb.append('\t');
                float v = getMatrixValue(c, r);
                if (v >= 0) stb.append(" ");
                stb.append(String.format("%.4f", v));
            }
        }
        return stb.toString();
    }
    */

    private static final class SATTestResult {

        private float min, max;

        void projectTriangle(int p1, int p2, int p3,
                             float axisX, float axisY, float axisZ) {
            this.min = Float.POSITIVE_INFINITY;
            this.max = Float.NEGATIVE_INFINITY;

            Vector3f v = nearPlaneVectors[p1];
            project(v.x, v.y, v.z, axisX, axisY, axisZ);
            v = nearPlaneVectors[p2];
            project(v.x, v.y, v.z, axisX, axisY, axisZ);
            v = nearPlaneVectors[p3];
            project(v.x, v.y, v.z, axisX, axisY, axisZ);
        }

        void projectAABB(float minX, float maxX,
                         float minY, float maxY,
                         float minZ, float maxZ,
                         float axisX, float axisY, float axisZ) {
            this.min = Float.POSITIVE_INFINITY;
            this.max = Float.NEGATIVE_INFINITY;

            for (int i = 0; i <= 0b111; i++) {
                project((i & 0b001) != 0 ? maxX : minX,
                        (i & 0b010) != 0 ? maxY : minY,
                        (i & 0b100) != 0 ? maxZ : minZ,
                        axisX, axisY, axisZ);
            }
        }

        private void project(float x, float y, float z,
                             float axisX, float axisY, float axisZ) {
            float dot = x * axisX + y * axisY + z * axisZ;
            if (dot < this.min) this.min = dot;
            if (dot > this.max) this.max = dot;
        }
    }
}
