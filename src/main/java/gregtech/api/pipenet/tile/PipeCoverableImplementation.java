package gregtech.api.pipenet.tile;

import gregtech.api.cover2.Cover;
import gregtech.api.cover2.CoverHolder;
import gregtech.api.cover2.CoverSaveHandler;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public class PipeCoverableImplementation implements CoverHolder {

    private final IPipeTile<?, ?> holder;
    private final EnumMap<EnumFacing, Cover> covers = new EnumMap<>(EnumFacing.class);
    private final int[] sidedRedstoneInput = new int[6];

    public PipeCoverableImplementation(IPipeTile<?, ?> holder) {
        this.holder = holder;
    }

    public void transferDataTo(PipeCoverableImplementation destImpl) {
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = covers.get(side);
            if (cover == null) continue;
            NBTTagCompound tagCompound = new NBTTagCompound();
            cover.writeToNBT(tagCompound);
            Cover newCover = cover.getDefinition().createCover(destImpl, side);
            newCover.readFromNBT(tagCompound);
            destImpl.covers.put(side, newCover);
        }
    }

    @Override
    public final void addCover(@NotNull EnumFacing side, @NotNull Cover cover) {
        if (cover.isTickable() && !holder.supportsTicking()) {
            IPipeTile<?, ?> newHolderTile = holder.setSupportsTicking();
            newHolderTile.getCoverableImplementation().addCover(side, cover);
            return;
        }
        this.covers.put(side, cover);
        CoverSaveHandler.writeCoverPlacement(this, COVER_ATTACHED_PIPE, side, cover);
        if (cover.shouldAutoConnectToPipes()) {
            holder.setConnection(side, true, false);
        }
        holder.notifyBlockUpdate();
        holder.markAsDirty();
    }

    @Override
    public final void removeCover(EnumFacing side) {
        Cover cover = getCoverAtSide(side);
        if (cover == null) return;

        dropCover(side);
        covers.remove(side);
        writeCustomData(COVER_REMOVED_PIPE, buffer -> buffer.writeByte(side.getIndex()));
        if (cover.shouldAutoConnectToPipes()) {
            holder.setConnection(side, false, false);
        }
        holder.notifyBlockUpdate();
        holder.markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public ItemStack getStackForm() {
        BlockPipe pipeBlock = holder.getPipeBlock();
        return pipeBlock.getDropItem(holder);
    }

    public void onLoad() {
        for (EnumFacing side : EnumFacing.VALUES) {
            this.sidedRedstoneInput[side.getIndex()] = GTUtility.getRedstonePower(getWorld(), getPos(), side);
        }
    }

    @Override
    public final int getInputRedstoneSignal(EnumFacing side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; //covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.getIndex()];
    }

    public void updateInputRedstoneSignals() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int redstoneValue = GTUtility.getRedstonePower(getWorld(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.getIndex()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.getIndex()] = redstoneValue;
                Cover cover = getCoverAtSide(side);
                if (cover != null) {
                    cover.onRedstoneInputSignalChange(redstoneValue);
                }
            }
        }
    }

    @Override
    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public double getCoverPlateThickness() {
        float thickness = holder.getPipeType().getThickness();
        // no cover plate for pipes >= 1 block thick
        if (thickness >= 1) return 0;

        // If the available space for the cover is less than the regular cover plate thickness, use that

        // need to divide by 2 because thickness is centered on the block, so the space is half on each side of the pipe
        return Math.min(1.0 / 16.0, (1.0 - thickness) / 2);
    }

    @Override
    public boolean shouldRenderCoverBackSides() {
        return false;
    }

    @Override
    public int getPaintingColorForRendering() {
        return ConfigHolder.client.defaultPaintingColor;
    }

    @Override
    public boolean hasCover(@NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
        return holder.canPlaceCoverOnSide(side);
    }

    public boolean canConnectRedstone(@Nullable EnumFacing side) {
        //so far null side means either upwards or downwards redstone wire connection
        //so check both top cover and bottom cover
        if (side == null) {
            return canConnectRedstone(EnumFacing.UP) ||
                    canConnectRedstone(EnumFacing.DOWN);
        }
        Cover cover = getCoverAtSide(side);
        return cover != null && cover.canConnectRedstone();
    }

    public int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        Cover cover = getCoverAtSide(side);
        return cover == null ? 0 : cover.getRedstoneSignalOutput();
    }

    public int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = getCoverAtSide(side);
            if (cover == null) continue;
            highestSignal = Math.max(highestSignal, cover.getRedstoneSignalOutput());
        }
        return highestSignal;
    }

    public void update() {
        if (!getWorld().isRemote) {
            updateCovers();
        }
    }

    @Override
    public void writeCoverData(@Nonnull Cover cover, int discriminator, @NotNull Consumer<@NotNull PacketBuffer> buf) {
        writeCustomData(UPDATE_COVER_DATA_PIPE, buffer -> {
            buffer.writeByte(cover.getAttachedSide().getIndex());
            buffer.writeVarInt(discriminator);
            buf.accept(buffer);
        });
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        CoverSaveHandler.writeInitialSyncData(buf, this);
    }

    public void readInitialSyncData(PacketBuffer buf) {
        CoverSaveHandler.receiveInitialSyncData(buf, this);
    }

    @Override
    public void writeCustomData(int dataId, @NotNull Consumer<PacketBuffer> writer) {
        holder.writeCoverCustomData(dataId, writer);
    }

    public void readCustomData(int dataId, PacketBuffer buf) {
        if (dataId == COVER_ATTACHED_PIPE) {
            CoverSaveHandler.readCoverPlacement(buf, this);
        } else if (dataId == COVER_REMOVED_PIPE) {
            //cover removed event
            EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
            this.covers.remove(placementSide);
            holder.scheduleChunkForRenderUpdate();
        } else if (dataId == UPDATE_COVER_DATA_PIPE) {
            //cover custom data received
            EnumFacing coverSide = EnumFacing.VALUES[buf.readByte()];
            Cover cover = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            if (cover != null) {
                cover.readCustomData(internalId, buf);
            }
        }
    }

    public void writeToNBT(NBTTagCompound data) {
        CoverSaveHandler.writeCoverNBT(data, this);
    }

    public void readFromNBT(NBTTagCompound data) {
        CoverSaveHandler.readCoverNBT(data, this);
    }

    @Override
    public World getWorld() {
        return holder.getPipeWorld();
    }

    @Override
    public BlockPos getPos() {
        return holder.getPipePos();
    }

    @Override
    public long getOffsetTimer() {
        return holder.getTickTimer();
    }

    @Nullable
    @Override
    public Cover getCoverAtSide(@NotNull EnumFacing side) {
        return covers.get(side);
    }

    @Override
    public void markDirty() {
        holder.markAsDirty();
    }

    @Override
    public boolean isValid() {
        return holder.isValidTile();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return holder.getCapabilityInternal(capability, side);
    }
}
