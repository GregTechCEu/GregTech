package gregtech.api.pipenet.tile;

import com.google.common.base.Preconditions;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class PipeCoverableImplementation implements ICoverable {

    private final IPipeTile<?, ?> holder;
    private final CoverBehavior[] coverBehaviors = new CoverBehavior[6];
    private int[] sidedRedstoneInput = new int[6];

    public PipeCoverableImplementation(IPipeTile<?, ?> holder) {
        this.holder = holder;
    }

    public void transferDataTo(PipeCoverableImplementation destImpl) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior behavior = coverBehaviors[coverSide.getIndex()];
            if (behavior == null) continue;
            NBTTagCompound tagCompound = new NBTTagCompound();
            behavior.writeToNBT(tagCompound);
            CoverBehavior newBehavior = behavior.getCoverDefinition().createCoverBehavior(destImpl, coverSide);
            newBehavior.readFromNBT(tagCompound);
            destImpl.coverBehaviors[coverSide.getIndex()] = newBehavior;
        }
    }

    public final boolean placeCoverOnSide(EnumFacing side, ItemStack itemStack, CoverDefinition coverDefinition) {
        if (side == null || coverDefinition == null) {
            return false;
        }
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        if (!canPlaceCoverOnSide(side) || !coverBehavior.canAttach()) {
            return false;
        }
        //if cover requires ticking and we're not tickable, update ourselves and redirect call to new tickable tile entity
        boolean requiresTicking = coverBehavior instanceof ITickable;
        if (requiresTicking && !holder.supportsTicking()) {
            IPipeTile<?, ?> newHolderTile = holder.setSupportsTicking();
            return newHolderTile.getCoverableImplementation().placeCoverOnSide(side, itemStack, coverDefinition);
        }
        if (coverBehaviors[side.getIndex()] != null) {
            removeCover(side);
        }
        this.coverBehaviors[side.getIndex()] = coverBehavior;
        coverBehavior.onAttached(itemStack);
        writeCustomData(1, buffer -> {
            buffer.writeByte(side.getIndex());
            buffer.writeVarInt(CoverDefinition.getNetworkIdForCover(coverDefinition));
            coverBehavior.writeInitialSyncData(buffer);
        });
        if (!coverBehavior.canPipePassThrough()) {
            holder.setConnectionBlocked(AttachmentType.COVER, side, true, false);
        }
        holder.notifyBlockUpdate();
        holder.markAsDirty();
        return true;
    }

    @SuppressWarnings("unchecked")
    public final boolean removeCover(EnumFacing side) {
        Preconditions.checkNotNull(side, "side");
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return false;
        }
        List<ItemStack> drops = coverBehavior.getDrops();
        coverBehavior.onRemoved();
        this.coverBehaviors[side.getIndex()] = null;
        for (ItemStack dropStack : drops) {
            Block.spawnAsEntity(getWorld(), getPos(), dropStack);
        }
        writeCustomData(2, buffer -> buffer.writeByte(side.getIndex()));
        BlockPipe pipeBlock = (BlockPipe<?, ?, ?>) getWorld().getBlockState(getPos()).getBlock();
        holder.setConnectionBlocked(AttachmentType.COVER, side, !pipeBlock.canConnect(holder, side), false);
        holder.notifyBlockUpdate();
        holder.markAsDirty();
        return true;
    }

    public final void dropAllCovers() {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = coverBehaviors[coverSide.getIndex()];
            if (coverBehavior == null) continue;
            List<ItemStack> drops = coverBehavior.getDrops();
            coverBehavior.onRemoved();
            for (ItemStack dropStack : drops) {
                Block.spawnAsEntity(getWorld(), getPos(), dropStack);
            }
        }
    }

    @Override
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
        if(!ignoreCover && getCoverAtSide(side) != null) {
            return 0; //covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.getIndex()];
    }

    public void updateInputRedstoneSignals() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int redstoneValue = GTUtility.getRedstonePower(getWorld(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.getIndex()];
            if(redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.getIndex()] = redstoneValue;
                CoverBehavior coverBehavior = getCoverAtSide(side);
                if(coverBehavior != null) {
                    coverBehavior.onRedstoneInputSignalChange(redstoneValue);
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
        holder.markAsDirty();
    }

    @Override
    public double getCoverPlateThickness() {
        return 1.0 / 16.0;
    }

    @Override
    public int getPaintingColor() {
        //todo make insulation colors separate for pipes and cables so cover plates have the correct overlay
        return holder.getInsulationColor() == IPipeTile.DEFAULT_INSULATION_COLOR ? IPipeTile.DEFAULT_INSULATION_COLOR : holder.getInsulationColor();
    }

    @Override
    public boolean shouldRenderBackSide() {
        return false;
    }

    @Override
    public CoverBehavior getCoverAtSide(EnumFacing side) {
        return side == null ? null : coverBehaviors[side.getIndex()];
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
        return holder.canPlaceCoverOnSide(side);
    }

    public boolean canConnectRedstone(@Nullable EnumFacing side) {
        //so far null side means either upwards or downwards redstone wire connection
        //so check both top cover and bottom cover
        if(side == null) {
            return canConnectRedstone(EnumFacing.UP) ||
                canConnectRedstone(EnumFacing.DOWN);
        }
        CoverBehavior behavior = getCoverAtSide(side);
        return behavior != null && behavior.canConnectRedstone();
    }

    public int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        if(side == null) {
            return getHighestOutputRedstoneSignal();
        }
        CoverBehavior behavior = getCoverAtSide(side);
        return behavior == null ? 0 : behavior.getRedstoneSignalOutput();
    }

    public int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for(EnumFacing side : EnumFacing.VALUES) {
            CoverBehavior behavior = getCoverAtSide(side);
            highestSignal = Math.max(highestSignal, behavior.getRedstoneSignalOutput());
        }
        return highestSignal;
    }

    public void update() {
        if (!getWorld().isRemote) {
            for (CoverBehavior coverBehavior : coverBehaviors) {
                if (coverBehavior instanceof ITickable) {
                    ((ITickable) coverBehavior).update();
                }
            }
        }
    }

    @Override
    public void writeCoverData(CoverBehavior behavior, int id, Consumer<PacketBuffer> writer) {
        writeCustomData(0, buffer -> {
            buffer.writeByte(behavior.attachedSide.getIndex());
            buffer.writeVarInt(id);
            writer.accept(buffer);
        });
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = getCoverAtSide(coverSide);
            if (coverBehavior != null) {
                int coverId = CoverDefinition.getNetworkIdForCover(coverBehavior.getCoverDefinition());
                buf.writeVarInt(coverId);
                coverBehavior.writeInitialSyncData(buf);
            } else {
                buf.writeVarInt(-1);
            }
        }
    }

    public void readInitialSyncData(PacketBuffer buf) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            int coverId = buf.readVarInt();
            if (coverId != -1) {
                CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(coverId);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, coverSide);
                coverBehavior.readInitialSyncData(buf);
                this.coverBehaviors[coverSide.getIndex()] = coverBehavior;
            }
        }
    }

    public void writeCustomData(int dataId, Consumer<PacketBuffer> writer) {
        holder.writeCoverCustomData(dataId, writer);
    }

    public void readCustomData(int dataId, PacketBuffer buf) {
        if (dataId == 1) {
            //cover placement event
            EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
            int coverId = buf.readVarInt();
            CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(coverId);
            CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, placementSide);
            this.coverBehaviors[placementSide.getIndex()] = coverBehavior;
            coverBehavior.readInitialSyncData(buf);
            holder.scheduleChunkForRenderUpdate();
        } else if (dataId == 2) {
            //cover removed event
            EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
            this.coverBehaviors[placementSide.getIndex()] = null;
            holder.scheduleChunkForRenderUpdate();
        } else if (dataId == 0) {
            //cover custom data received
            EnumFacing coverSide = EnumFacing.VALUES[buf.readByte()];
            CoverBehavior coverBehavior = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            if (coverBehavior != null) {
                coverBehavior.readUpdateData(internalId, buf);
            }
        }
    }

    public void writeToNBT(NBTTagCompound data) {
        NBTTagList coversList = new NBTTagList();
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = coverBehaviors[coverSide.getIndex()];
            if (coverBehavior != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                ResourceLocation coverId = coverBehavior.getCoverDefinition().getCoverId();
                tagCompound.setString("CoverId", coverId.toString());
                tagCompound.setByte("Side", (byte) coverSide.getIndex());
                coverBehavior.writeToNBT(tagCompound);
                coversList.appendTag(tagCompound);
            }
        }
        data.setTag("Covers", coversList);
    }

    public void readFromNBT(NBTTagCompound data) {
        NBTTagList coversList = data.getTagList("Covers", NBT.TAG_COMPOUND);
        for (int index = 0; index < coversList.tagCount(); index++) {
            NBTTagCompound tagCompound = coversList.getCompoundTagAt(index);
            if (tagCompound.hasKey("CoverId", NBT.TAG_STRING)) {
                EnumFacing coverSide = EnumFacing.VALUES[tagCompound.getByte("Side")];
                ResourceLocation coverId = new ResourceLocation(tagCompound.getString("CoverId"));
                CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverId);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, coverSide);
                coverBehavior.readFromNBT(tagCompound);
                this.coverBehaviors[coverSide.getIndex()] = coverBehavior;
            }
        }
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

    @Override
    public void markDirty() {
        holder.markAsDirty();
    }

    @Override
    public boolean isValid() {
        return !holder.isValidTile();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return holder.getCapabilityInternal(capability, side);
    }
}
