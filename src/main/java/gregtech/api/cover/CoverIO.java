package gregtech.api.cover;

import gregtech.api.util.GTLog;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Contains default implementations for common networking and data storage involving covers
 */
public final class CoverIO {

    public static final int NO_COVER_ID = -1;

    private CoverIO() {/**/}

    /**
     * Write sync data for a coverable's covers
     *
     * @param buf       the buf to write to
     * @param coverable the coverable containing the covers
     */
    public static void writeCoverSyncData(@Nonnull PacketBuffer buf, @Nonnull ICoverable coverable) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = coverable.getCoverAtSide(coverSide);
            if (coverBehavior != null) {
                buf.writeVarInt(CoverDefinition.getNetworkIdForCover(coverBehavior.getCoverDefinition()));
                coverBehavior.writeInitialSyncData(buf);
            } else {
                // cover was not attached
                buf.writeVarInt(NO_COVER_ID);
            }
        }
    }

    /**
     * Receive sync data for a coverable's covers
     *
     * @param buf         the buf to read from
     * @param coverable   the coverable containing the covers
     * @param coverWriter the operation the coverable uses to store a cover
     */
    public static void receiveCoverSyncData(@Nonnull PacketBuffer buf, @Nonnull ICoverable coverable,
                                            @Nonnull BiConsumer<EnumFacing, CoverBehavior> coverWriter) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            int id = buf.readVarInt();
            if (id != NO_COVER_ID) {
                CoverDefinition definition = CoverDefinition.getCoverByNetworkId(id);
                if (definition == null) {
                    GTLog.logger.warn("Unable to find CoverDefinition for Network ID {} at position {}", id, coverable.getPos());
                } else {
                    CoverBehavior coverBehavior = definition.createCoverBehavior(coverable, coverSide);
                    coverBehavior.readInitialSyncData(buf);
                    coverWriter.accept(coverSide, coverBehavior);
                }
            }
        }
    }

    /**
     * @param side     the side the cover is attached to
     * @param behavior the cover's behavior
     * @return a writer for a cover's attachment data
     */
    @Nonnull
    public static Consumer<PacketBuffer> getCoverPlacementCustomDataWriter(@Nonnull EnumFacing side, @Nonnull CoverBehavior behavior) {
        return buffer -> {
            buffer.writeByte(side.getIndex());
            buffer.writeVarInt(CoverDefinition.getNetworkIdForCover(behavior.getCoverDefinition()));
            behavior.writeInitialSyncData(buffer);
        };
    }

    /**
     * Read a cover's placement customData
     *
     * @param buf           the buffer to read from
     * @param coverable     the coverable the cover is placed on
     * @param coverWriter   the operation the coverable uses to store a cover
     * @param renderUpdater the operation the coverable uses to schedule render updates
     */
    public static void readCoverPlacement(@Nonnull PacketBuffer buf, @Nonnull ICoverable coverable,
                                          @Nonnull BiConsumer<EnumFacing, CoverBehavior> coverWriter,
                                          @Nonnull Runnable renderUpdater) {
        //cover placement event
        EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
        int id = buf.readVarInt();
        CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(id);
        if (coverDefinition == null) {
            GTLog.logger.warn("Unable to find CoverDefinition for Network ID {} at position {}", id, coverable.getPos());
        } else {
            CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(coverable, placementSide);
            coverWriter.accept(placementSide, coverBehavior);

            coverBehavior.readInitialSyncData(buf);
        }
        renderUpdater.run();
    }

    /**
     * Writes a coverable's covers to NBT
     *
     * @param tagCompound the tag compound to write to
     * @param coverReader the operation the coverable uses to retrieve a stored cover
     */
    public static void writeCoverNBT(@Nonnull NBTTagCompound tagCompound, @Nonnull Function<EnumFacing, CoverBehavior> coverReader) {
        NBTTagList coversList = new NBTTagList();
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = coverReader.apply(coverSide);
            if (coverBehavior != null) {
                NBTTagCompound tag = new NBTTagCompound();
                ResourceLocation coverId = coverBehavior.getCoverDefinition().getCoverId();
                tag.setString("CoverId", coverId.toString());
                tag.setByte("Side", (byte) coverSide.getIndex());
                coverBehavior.writeToNBT(tag);
                coversList.appendTag(tag);
            }
        }
        tagCompound.setTag("Covers", coversList);
    }

    /**
     * Reads a coverable's covers from NBT
     *
     * @param tagCompound the tag compound to read from
     * @param coverable   the cover to store the covers in
     * @param coverWriter the operation the coverable uses to write a stored cover
     */
    public static void readCoverNBT(@Nonnull NBTTagCompound tagCompound, @Nonnull ICoverable coverable,
                                    @Nonnull BiConsumer<EnumFacing, CoverBehavior> coverWriter) {
        NBTTagList coversList = tagCompound.getTagList("Covers", Constants.NBT.TAG_COMPOUND);
        for (int index = 0; index < coversList.tagCount(); index++) {
            NBTTagCompound tag = coversList.getCompoundTagAt(index);
            if (tag.hasKey("CoverId", Constants.NBT.TAG_STRING)) {
                EnumFacing coverSide = EnumFacing.VALUES[tag.getByte("Side")];
                ResourceLocation coverLocation = new ResourceLocation(tag.getString("CoverId"));
                CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverLocation);
                if (coverDefinition == null) {
                    GTLog.logger.warn("Unable to find CoverDefinition for ResourceLocation {} at position {}",
                            coverLocation, coverable.getPos());
                } else {
                    CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(coverable, coverSide);
                    coverBehavior.readFromNBT(tag);
                    coverWriter.accept(coverSide, coverBehavior);
                }
            }
        }
    }
}
