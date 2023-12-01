package gregtech.common.metatileentities.miner;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.utils.MinerRenderHelper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.metatileentity.IFastRenderMetaTileEntity.RENDER_PASS_TRANSLUCENT;

/**
 * Simple implementation of {@link MiningArea}. Defines cube-shaped mining area with simple iteration logic.
 */
public class SimpleMiningArea implements MiningArea {

    private static final double PREVIEW_OFFSET = 1 / 16.0;

    public final int startX;
    public final int startY;
    public final int startZ;
    public final int endX;
    public final int endY;
    public final int endZ;

    /**
     * Index for current block position. This implementation of {@link MiningArea} operates by first mapping each block
     * in given area to non-negative long indices, then processing it by incrementing internal counter starting from 0.
     * <br/> The area iterates through X plane first, then Z plane, before moving down one Y block.
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

    @NotNull
    public static SimpleMiningArea readPreview(@NotNull PacketBuffer buffer) {
        return new SimpleMiningArea(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt());
    }

    @Override
    public boolean getCurrentBlockPos(@NotNull MutableBlockPos mpos) {
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
    public void renderMetaTileEntity(@NotNull MetaTileEntity mte, double x, double y, double z, float partialTicks) {
        if (MinecraftForgeClient.getRenderPass() == RENDER_PASS_TRANSLUCENT) {
            MinerRenderHelper.renderAreaPreview(this.getRenderBoundingBox(), mte.getPos(), x, y, z);
        }
    }

    @NotNull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.boundingBoxCache == null) {
            return this.boundingBoxCache = new AxisAlignedBB(
                    startX + PREVIEW_OFFSET,
                    endY == Integer.MIN_VALUE ? Double.NEGATIVE_INFINITY : endY + PREVIEW_OFFSET,
                    startZ + PREVIEW_OFFSET,
                    endX - PREVIEW_OFFSET, startY + 1 - PREVIEW_OFFSET, endZ - PREVIEW_OFFSET);
        }
        return this.boundingBoxCache;
    }

    @Override
    public void write(@NotNull NBTTagCompound data) {
        data.setLong("i", this.currentBlock);
    }

    @Override
    public void read(@NotNull NBTTagCompound data) {
        this.currentBlock = Math.max(0, data.getLong("i"));
    }

    @Override
    public void writePreviewPacket(@NotNull PacketBuffer buffer) {
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
