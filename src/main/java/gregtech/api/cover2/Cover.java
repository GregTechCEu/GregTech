package gregtech.api.cover2;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface Cover {

    /**
     * @return the CoverableView containing this cover
     */
    @NotNull CoverableView getCoverable();

    @NotNull CoverDefinition2 getDefinition();

    /**
     * @return the World containing this cover
     */
    default @UnknownNullability World getWorld() {
        return getCoverable().getWorld();
    }

    /**
     * @return the pos of this cover
     */
    default @UnknownNullability BlockPos getPos() {
        return getCoverable().getPos();
    }

    /**
     * Mark the CoverableView as needing to be saved to the chunk
     */
    default void markDirty() {
        getCoverable().markDirty();
    }

    /**
     * Notify block updates for the CoverableView
     */
    default void notifyBlockUpdate() {
        getCoverable().notifyBlockUpdate();
    }

    /**
     * Schedule the CoverableView to update rendering
     */
    default void scheduleRenderUpdate() {
        getCoverable().scheduleRenderUpdate();
    }

    /**
     * @return tick timer value with a random offset of [0,20]
     */
    default long getOffsetTimer() {
        return getCoverable().getOffsetTimer();
    }

    default void update() {}

    default boolean isTickable() {
        //noinspection InstanceofThis
        return this instanceof ITickable;
    }

    /**
     * @return the side the cover is attached to
     */
    @NotNull EnumFacing getAttachedSide();

    /**
     * @param coverable the CoverableView to attach to
     * @param side      the side to test
     * @return if the cover can attach to the side
     */
    boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side);


    /**
     * Called when the cover is first attached on the Server Side.
     *
     * @param coverableView the CoverableView this cover is attached to
     * @param side          the side this cover is attached to
     * @param player        the player attaching the cover
     * @param itemStack     the item used to place the cover
     */
    default void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side, @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {}

    /**
     * Called when the cover is removed
     */
    default void onRemoval() {}

    /**
     * @return if the cover interacts with an Output Side of a CoverableView
     */
    default boolean canInteractWithOutputSide() {
        return false;
    }

    /**
     * @return if the pipe this cover is placed on should render a connection to the cover
     */
    default boolean shouldAutoConnectToPipes() {
        return true;
    }

    /**
     * @return if the pipe this cover is placed on and a pipe on the other side should be able to connect
     */
    default boolean canPipePassThrough() {
        return false;
    }

    /**
     * @param player    the player clicking the cover
     * @param hitResult the HitResult of the click
     * @return the action's result
     */
    default boolean onLeftClick(@NotNull EntityPlayer player, @NotNull CuboidRayTraceResult hitResult) {
        return false;
    }

    /**
     * @param player    the player clicking the cover
     * @param hand      the active hand the player is using
     * @param hitResult the HitResult of the click
     * @return the action's result
     */
    default @NotNull EnumActionResult onRightClick(@NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull CuboidRayTraceResult hitResult) {
        return EnumActionResult.PASS;
    }

    /**
     * @param player    the player clicking the cover
     * @param hand      the active hand the player is using
     * @param hitResult the HitResult of the click
     * @return the action's result
     */
    default @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull CuboidRayTraceResult hitResult) {
        return EnumActionResult.PASS;
    }

    /**
     * @param player    the player clicking the cover
     * @param hand      the active hand the player is using
     * @param hitResult the HitResult of the click
     * @return the action's result
     */
    default @NotNull EnumActionResult onSoftMalletClick(@NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull CuboidRayTraceResult hitResult) {
        return EnumActionResult.PASS;
    }

    /**
     * @return a list of ItemStacks to drop when removed
     */
    default @NotNull @Unmodifiable List<@NotNull ItemStack> getDrops() {
        return Collections.singletonList(getPickItem());
    }

    /**
     * @return the ItemStack form of the Cover
     */
    default @NotNull ItemStack getPickItem() {
        return getDefinition().getDropItemStack();
    }

    /**
     * @return if the Cover can connect to redstone
     */
    default boolean canConnectRedstone() {
        return false;
    }

    /**
     * Called when the redstone input signal changes.
     *
     * @param redstone the new signal value
     */
    default void onRedstoneInputSignalChange(int redstone) {}

    /**
     * @return the redstone signal being output from the cover
     */
    default int getRedstoneSignalOutput() {
        return 0;
    }

    /**
     * Called on client side to render this cover on the machine's face
     * It will be automatically translated to prevent Z-fighting with machine faces
     */
    @SideOnly(Side.CLIENT)
    void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, @NotNull IVertexOperation[] pipeline,
                     @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer);

    @SideOnly(Side.CLIENT)
    default boolean canRenderInLayer(@NotNull BlockRenderLayer renderLayer) {
        return renderLayer == BlockRenderLayer.CUTOUT_MIPPED || renderLayer == BloomEffectUtil.getRealBloomLayer();
    }

    @SideOnly(Side.CLIENT)
    void renderCoverPlate(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, @NotNull IVertexOperation[] pipeline,
                          @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer);

    default boolean canRenderBackside() {
        return true;
    }

    /**
     * Will be called for each capability request to the CoverableView
     * Cover can override CoverableView capabilities, modify their values, or deny accessing them
     *
     * @param capability   the requested Capability
     * @param defaultValue value of the capability from CoverableView itself
     * @return the resulting capability the caller will receive
     */
    default <T> @Nullable T getCapability(@NotNull Capability<T> capability, @Nullable T defaultValue) {
        return defaultValue;
    }

    default void writeToNBT(@NotNull NBTTagCompound nbt) {}

    default void readFromNBT(@NotNull NBTTagCompound nbt) {}

    default void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {}

    default void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {}

    default void writeCustomData(int discriminator, @NotNull Consumer<@NotNull PacketBuffer> buf) {
        getCoverable().writeCoverData(this, discriminator, buf);
    }

    default void readCustomData(int discriminator, @NotNull PacketBuffer buf) {}
}
