package gregtech.common.metatileentities.miner;

import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

/**
 * Object representing operation area of {@link MinerLogic}.
 */
public interface MiningArea {

    /**
     * Get current block position for processing. If this method returns {@code true}, {@code mpos} argument should be
     * modified to the block position. Return value of {@code false} indicates there aren't any block left to process.
     * <br/>
     * Calling this method does not affect the state. Use {@link #nextBlock()} for advancing to next block.
     *
     * @param mpos Mutable block position
     * @return {@code true} if {@code mpos} is set to current block position for processing, {@code false} otherwise
     */
    boolean getCurrentBlockPos(@NotNull MutableBlockPos mpos);

    /**
     * Move on to next block for processing, if it exists. Does nothing if there aren't any block left to process.
     *
     * @see #getCurrentBlockPos(MutableBlockPos)
     */
    void nextBlock();

    /**
     * Reset the cursor to starting point (i.e. re-start iteration from start)
     */
    void reset();

    @SideOnly(Side.CLIENT)
    default void renderMetaTileEntityFast(@NotNull MetaTileEntity mte, @NotNull CCRenderState renderState,
                                          @NotNull Matrix4 translation, float partialTicks) {}

    @SideOnly(Side.CLIENT)
    default void renderMetaTileEntity(@NotNull MetaTileEntity mte, double x, double y, double z, float partialTicks) {}

    @NotNull
    AxisAlignedBB getRenderBoundingBox();

    default boolean shouldRenderInPass(int pass) {
        return pass == IFastRenderMetaTileEntity.RENDER_PASS_TRANSLUCENT;
    }

    /**
     * Write any persistent data here.
     *
     * @param data NBT data
     */
    void write(@NotNull NBTTagCompound data);

    /**
     * Read any persistent data here.
     *
     * @param data NBT data
     */
    void read(@NotNull NBTTagCompound data);

    /**
     * Write data for area preview.
     *
     * @param buffer Packet buffer
     */
    void writePreviewPacket(@NotNull PacketBuffer buffer);
}
