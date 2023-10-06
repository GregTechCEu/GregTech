package gregtech.common.metatileentities.miner;

import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple implementation of {@link IMiningArea}. Defines cube-shaped mining area with simple iteration logic.
 */
public class SimpleMiningArea implements IMiningArea {

    public final int startX;
    public final int startY;
    public final int startZ;
    public final int endX;
    public final int endY;
    public final int endZ;

    /**
     * Index for current block position. This implementation of {@link IMiningArea} operates by first mapping each block
     * in given area to non-negative long indices, then processing it by incrementing internal counter starting from 0.
     * <br/>
     * The area iterates through X plane first, then Z plane, before moving down one Y block.
     */
    private long currentBlock;

    @Nullable
    private AxisAlignedBB boundingBoxCache;

    /**
     * @param startX Min X position, inclusive
     * @param startY <b>Max</b> Y position, inclusive
     * @param startZ Min Z position, inclusive
     * @param endX   Max X position, inclusive
     * @param endY   <b>Min</b> Y position, inclusive
     * @param endZ   Max Z position, inclusive
     */
    public SimpleMiningArea(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    @Nonnull
    public static SimpleMiningArea readPreview(@Nonnull PacketBuffer buffer) {
        return new SimpleMiningArea(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public boolean getCurrentBlockPos(@Nonnull MutableBlockPos mpos) {
        long index = this.currentBlock;
        if (index < 0) return false;
        int sizeX = this.endX - this.startX;
        int sizeZ = this.endZ - this.startZ;
        if (sizeX <= 0 || sizeZ <= 0) return false;

        int x = this.startX + (int) (index % sizeX);
        index /= sizeX;
        int z = this.startZ + (int) (index % sizeZ);
        int y = this.startY - (int) (index / sizeZ);

        if (y < this.endY) return false;

        mpos.setPos(x, y, z);
        return true;
    }

    @Override
    public void nextBlock() {
        this.currentBlock++;
    }

    @Override
    public void reset() {
        this.currentBlock = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(@Nonnull MetaTileEntity mte, double x, double y, double z, float partialTicks) {
        // skull emoji

        float texOffset = (System.nanoTime() % 1_000_000_000) / 2_000_000_000f;
        // texOffset = (int) (texOffset * 16) * (1 / 16f);

        float t = texOffset * 0.5f;
        float ti = (1 - texOffset) * 0.5f;

        BlockPos pos = mte.getPos();
        double minX = this.startX + x - pos.getX() - (1 / 16.0), maxX = this.endX + x - pos.getX() + (1 / 16.0);
        double minY = this.endY + y - pos.getY() - (1 / 16.0), maxY = this.startY + y - pos.getY() + (1 / 16.0);
        double minZ = this.startZ + z - pos.getZ() - (1 / 16.0), maxZ = this.endZ + z - pos.getZ() + (1 / 16.0);

        double mx = (this.endX - this.startX) * .5f;
        double my = (this.startY - this.endY) * .5f;
        double mz = (this.endZ - this.startZ) * .5f;

        Minecraft.getMinecraft().getTextureManager().bindTexture(MinerUtil.MINER_AREA_PREVIEW_TEXTURE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1, 1, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        // NORTH
        v(buffer, minX, maxY, minZ, t, t);
        v(buffer, maxX, maxY, minZ, t + mx, t);
        v(buffer, maxX, minY, minZ, t + mx, t - my);
        v(buffer, minX, minY, minZ, t, t - my);

        // SOUTH
        v(buffer, minX, minY, maxZ, t, t);
        v(buffer, maxX, minY, maxZ, t + mx, t);
        v(buffer, maxX, maxY, maxZ, t + mx, t + my);
        v(buffer, minX, maxY, maxZ, t, t + my);

        // DOWN
        v(buffer, minX, minY, minZ, t, t);
        v(buffer, maxX, minY, minZ, t + mx, t);
        v(buffer, maxX, minY, maxZ, t + mx, t + mz);
        v(buffer, minX, minY, maxZ, t, t + mz);

        // UP
        v(buffer, minX, maxY, maxZ, t, t);
        v(buffer, maxX, maxY, maxZ, t + mx, t);
        v(buffer, maxX, maxY, minZ, t + mx, t + mz);
        v(buffer, minX, maxY, minZ, t, t + mz);

        // WEST
        v(buffer, minX, minY, maxZ, t, t);
        v(buffer, minX, maxY, maxZ, t + my, t);
        v(buffer, minX, maxY, minZ, t + my, t - mz);
        v(buffer, minX, minY, minZ, t + my, t - mz);

        // EAST
        v(buffer, maxX, minY, minZ, t, t);
        v(buffer, maxX, maxY, minZ, t + my, t);
        v(buffer, maxX, maxY, maxZ, t + my, t + mz);
        v(buffer, maxX, minY, maxZ, t , t + mz);

        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.enableCull();
    }

    private static void v(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z).tex(u, v).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == IFastRenderMetaTileEntity.RENDER_PASS_TRANSLUCENT;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.boundingBoxCache == null) {
            return this.boundingBoxCache = new AxisAlignedBB(
                    startX - (1 / 16.0), endY - (1 / 16.0), startZ - (1 / 16.0),
                    endX + 1 + (1 / 16.0), startY + 1 + (1 / 16.0), endZ + 1 + (1 / 16.0));
        }
        return this.boundingBoxCache;
    }

    @Override
    public void write(@Nonnull NBTTagCompound data) {
        data.setLong("i", this.currentBlock);
    }

    @Override
    public void read(@Nonnull NBTTagCompound data) {
        this.currentBlock = Math.max(0, data.getLong("i"));
    }

    @Override
    public void writePreviewPacket(@Nonnull PacketBuffer buffer) {
        buffer.writeInt(startX);
        buffer.writeInt(startY);
        buffer.writeInt(startZ);
        buffer.writeInt(endX);
        buffer.writeInt(endY);
        buffer.writeInt(endZ);
    }

    @Override
    public String toString() {
        return "SimpleMiningArea{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", startZ=" + startZ +
                ", endX=" + endX +
                ", endZ=" + endZ +
                ", currentBlock=" + currentBlock +
                '}';
    }
}
