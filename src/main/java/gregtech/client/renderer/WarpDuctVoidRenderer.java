package gregtech.client.renderer;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.QuadHelper;
import gregtech.common.pipelike.block.warpduct.WarpDuctTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.EnumSet;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class WarpDuctVoidRenderer extends TileEntitySpecialRenderer<WarpDuctTileEntity> {

    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    public void render(@NotNull WarpDuctTileEntity te, double x, double y, double z, float partialTicks,
                       int destroyStage, float alpha) {
        GlStateManager.disableLighting();
        RANDOM.setSeed(31100L);
        GlStateManager.getFloat(2982, MODELVIEW);
        GlStateManager.getFloat(2983, PROJECTION);
        double d0 = x * x + y * y + z * z;
        int i = this.getPasses(d0);
        float thickness = te.getStructure().getRenderThickness() - 0.01f;
        float offset = (1 - thickness) / 2;
        float offsetLarge = 1 - offset;
        EnumSet<EnumFacing> inhabitedFacings = GTUtility.maskToSet(EnumFacing.class,
                te.getCoverAdjustedConnectionMask());

        boolean flag = false;

        for (int j = 0; j < i; ++j) {
            GlStateManager.pushMatrix();
            float f1 = 2.0F / (float) (18 - j);

            if (j == 0) {
                this.bindTexture(END_SKY_TEXTURE);
                f1 = 0.15F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if (j >= 1) {
                this.bindTexture(END_PORTAL_TEXTURE);
                flag = true;
                Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            }

            if (j == 1) {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 1.0F);
            float f2 = (float) (j + 1);
            GlStateManager.translate(17.0F / f2,
                    (2.0F + f2 / 1.5F) * ((float) Minecraft.getSystemTime() % 800000.0F / 800000.0F), 0.0F);
            GlStateManager.rotate((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            float f3 = (RANDOM.nextFloat() * 0.5F + 0.1F) * f1;
            float f4 = (RANDOM.nextFloat() * 0.5F + 0.4F) * f1;
            float f5 = (RANDOM.nextFloat() * 0.5F + 0.5F) * f1;

            Pair<Vector3f, Vector3f> core = QuadHelper.toPair(offset, offset, offset, offsetLarge, offsetLarge,
                    offsetLarge);

            if (inhabitedFacings.contains(EnumFacing.SOUTH)) {
                Pair<Vector3f, Vector3f> box = QuadHelper.toPair(offset, offset, offsetLarge, offsetLarge, offsetLarge,
                        0.99f);
                if (te.shouldRenderFace(EnumFacing.SOUTH)) {
                    visitQuad(bufferbuilder, box, EnumFacing.SOUTH, f3, f4, f5, x, y, z);
                }
                visitQuad(bufferbuilder, box, EnumFacing.UP, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.DOWN, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.EAST, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.WEST, f3, f4, f5, x, y, z);
            } else {
                visitQuad(bufferbuilder, core, EnumFacing.SOUTH, f3, f4, f5, x, y, z);
            }

            if (inhabitedFacings.contains(EnumFacing.NORTH)) {
                Pair<Vector3f, Vector3f> box = QuadHelper.toPair(offset, offset, 0.01f, offsetLarge, offsetLarge,
                        offset);
                if (te.shouldRenderFace(EnumFacing.NORTH)) {
                    visitQuad(bufferbuilder, box, EnumFacing.NORTH, f3, f4, f5, x, y, z);
                }
                visitQuad(bufferbuilder, box, EnumFacing.UP, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.DOWN, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.EAST, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.WEST, f3, f4, f5, x, y, z);
            } else {
                visitQuad(bufferbuilder, core, EnumFacing.NORTH, f3, f4, f5, x, y, z);
            }

            if (inhabitedFacings.contains(EnumFacing.EAST)) {
                Pair<Vector3f, Vector3f> box = QuadHelper.toPair(offsetLarge, offset, offset, 0.99f, offsetLarge,
                        offsetLarge);
                if (te.shouldRenderFace(EnumFacing.EAST)) {
                    visitQuad(bufferbuilder, box, EnumFacing.EAST, f3, f4, f5, x, y, z);
                }
                visitQuad(bufferbuilder, box, EnumFacing.UP, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.DOWN, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.NORTH, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.SOUTH, f3, f4, f5, x, y, z);
            } else {
                visitQuad(bufferbuilder, core, EnumFacing.EAST, f3, f4, f5, x, y, z);
            }

            if (inhabitedFacings.contains(EnumFacing.WEST)) {
                Pair<Vector3f, Vector3f> box = QuadHelper.toPair(0.01f, offset, offset, offset, offsetLarge,
                        offsetLarge);
                if (te.shouldRenderFace(EnumFacing.WEST)) {
                    visitQuad(bufferbuilder, box, EnumFacing.WEST, f3, f4, f5, x, y, z);
                }
                visitQuad(bufferbuilder, box, EnumFacing.UP, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.DOWN, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.NORTH, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.SOUTH, f3, f4, f5, x, y, z);
            } else {
                visitQuad(bufferbuilder, core, EnumFacing.WEST, f3, f4, f5, x, y, z);
            }

            if (inhabitedFacings.contains(EnumFacing.UP)) {
                Pair<Vector3f, Vector3f> box = QuadHelper.toPair(offset, offsetLarge, offset, offsetLarge, 0.99f,
                        offsetLarge);
                if (te.shouldRenderFace(EnumFacing.UP)) {
                    visitQuad(bufferbuilder, box, EnumFacing.UP, f3, f4, f5, x, y, z);
                }
                visitQuad(bufferbuilder, box, EnumFacing.EAST, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.WEST, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.NORTH, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.SOUTH, f3, f4, f5, x, y, z);
            } else {
                visitQuad(bufferbuilder, core, EnumFacing.UP, f3, f4, f5, x, y, z);
            }

            if (inhabitedFacings.contains(EnumFacing.DOWN)) {
                Pair<Vector3f, Vector3f> box = QuadHelper.toPair(offset, 0.01f, offset, offsetLarge, offset,
                        offsetLarge);
                if (te.shouldRenderFace(EnumFacing.DOWN)) {
                    visitQuad(bufferbuilder, box, EnumFacing.DOWN, f3, f4, f5, x, y, z);
                }
                visitQuad(bufferbuilder, box, EnumFacing.EAST, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.WEST, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.NORTH, f3, f4, f5, x, y, z);
                visitQuad(bufferbuilder, box, EnumFacing.SOUTH, f3, f4, f5, x, y, z);
            } else {
                visitQuad(bufferbuilder, core, EnumFacing.DOWN, f3, f4, f5, x, y, z);
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();

        if (flag) {
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        }
    }

    protected void visitQuad(BufferBuilder bufferbuilder, Pair<Vector3f, Vector3f> box, EnumFacing facing, float f3,
                             float f4, float f5, double x, double y, double z) {
        switch (facing) {
            case UP, DOWN -> {
                float yBox = facing == EnumFacing.UP ? box.getRight().y : box.getLeft().y;
                // order determines normal, for some reason
                if (facing == EnumFacing.UP) {
                    bufferbuilder.pos(x + box.getLeft().x, y + yBox, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getLeft().x, y + yBox, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + yBox, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + yBox, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                } else {
                    bufferbuilder.pos(x + box.getLeft().x, y + yBox, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + yBox, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + yBox, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getLeft().x, y + yBox, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                }
            }
            case NORTH, SOUTH -> {
                float zBox = facing == EnumFacing.SOUTH ? box.getRight().z : box.getLeft().z;
                if (facing == EnumFacing.NORTH) {
                    bufferbuilder.pos(x + box.getLeft().x, y + box.getLeft().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getLeft().x, y + box.getRight().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + box.getRight().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + box.getLeft().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                } else {
                    bufferbuilder.pos(x + box.getLeft().x, y + box.getLeft().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + box.getLeft().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getRight().x, y + box.getRight().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + box.getLeft().x, y + box.getRight().y, z + zBox).color(f3, f4, f5, 1.0F)
                            .endVertex();
                }
            }
            case EAST, WEST -> {
                float xBox = facing == EnumFacing.EAST ? box.getRight().x : box.getLeft().x;
                if (facing == EnumFacing.WEST) {
                    bufferbuilder.pos(x + xBox, y + box.getLeft().y, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + xBox, y + box.getLeft().y, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + xBox, y + box.getRight().y, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + xBox, y + box.getRight().y, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                } else {
                    bufferbuilder.pos(x + xBox, y + box.getLeft().y, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + xBox, y + box.getRight().y, z + box.getLeft().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + xBox, y + box.getRight().y, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                    bufferbuilder.pos(x + xBox, y + box.getLeft().y, z + box.getRight().z).color(f3, f4, f5, 1.0F)
                            .endVertex();
                }
            }
        }
    }

    protected int getPasses(double p_191286_1_) {
        int i;

        if (p_191286_1_ > 36864.0D) {
            i = 1;
        } else if (p_191286_1_ > 25600.0D) {
            i = 3;
        } else if (p_191286_1_ > 16384.0D) {
            i = 5;
        } else if (p_191286_1_ > 9216.0D) {
            i = 7;
        } else if (p_191286_1_ > 4096.0D) {
            i = 9;
        } else if (p_191286_1_ > 1024.0D) {
            i = 11;
        } else if (p_191286_1_ > 576.0D) {
            i = 13;
        } else if (p_191286_1_ > 256.0D) {
            i = 14;
        } else {
            i = 15;
        }

        return i;
    }

    private FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_) {
        this.buffer.clear();
        this.buffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(p_147525_4_);
        this.buffer.flip();
        return this.buffer;
    }
}
