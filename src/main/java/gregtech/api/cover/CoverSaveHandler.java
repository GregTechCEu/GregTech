package gregtech.api.cover;

import gregtech.api.util.GTLog;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public final class CoverSaveHandler {

    public static final int NO_COVER_ID = -1;

    private CoverSaveHandler() {}

    /**
     * Write sync data for a CoverableView's covers
     *
     * @param buf           the buf to write to
     * @param coverableView the CoverableView containing the covers
     */
    public static void writeInitialSyncData(@NotNull PacketBuffer buf, @NotNull CoverableView coverableView) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            Cover cover = coverableView.getCoverAtSide(coverSide);
            if (cover != null) {
                buf.writeVarInt(CoverDefinition.getNetworkIdForCover(cover.getDefinition()));
                cover.writeInitialSyncData(buf);
            } else {
                // cover was not attached
                buf.writeVarInt(NO_COVER_ID);
            }
        }
    }

    /**
     * Receive sync data for a CoverHolder's covers
     *
     * @param buf         the buf to read from
     * @param coverHolder the CoverHolder containing the covers
     */
    public static void receiveInitialSyncData(@NotNull PacketBuffer buf, @NotNull CoverHolder coverHolder) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            int id = buf.readVarInt();
            if (id != NO_COVER_ID) {
                CoverDefinition definition = CoverDefinition.getCoverByNetworkId(id);
                if (definition == null) {
                    GTLog.logger.warn("Unable to find CoverDefinition for Network ID {} at position {}", id, coverHolder.getPos());
                } else {
                    Cover cover = definition.createCover(coverHolder, coverSide);
                    cover.readInitialSyncData(buf);
                    coverHolder.addCover(coverSide, cover);
                }
            }
        }
    }

    /**
     * Write a cover's placement customData
     *
     * @param coverHolder   the CoverHolder to write cover placement data to
     * @param discriminator the discriminator the CoverableView uses for the operation
     * @param side          the side the cover is attached to
     * @param cover         the cover
     */
    public static void writeCoverPlacement(@NotNull CoverHolder coverHolder, int discriminator,
                                           @NotNull EnumFacing side, @NotNull Cover cover) {
        coverHolder.writeCustomData(discriminator, buf -> {
            buf.writeByte(side.getIndex());
            buf.writeVarInt(CoverDefinition.getNetworkIdForCover(cover.getDefinition()));
            cover.writeInitialSyncData(buf);
        });
    }

    /**
     * Read a cover's placement customData
     *
     * @param buf         the buffer to read from
     * @param coverHolder the CoverHolder the cover is placed on
     */
    public static void readCoverPlacement(@NotNull PacketBuffer buf, @NotNull CoverHolder coverHolder) {
        //cover placement event
        EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
        int id = buf.readVarInt();
        CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(id);
        if (coverDefinition == null) {
            GTLog.logger.warn("Unable to find CoverDefinition for Network ID {} at position {}", id, coverHolder.getPos());
        } else {
            Cover cover = coverDefinition.createCover(coverHolder, placementSide);
            coverHolder.addCover(placementSide, cover);

            cover.readInitialSyncData(buf);
        }
        coverHolder.scheduleRenderUpdate();
    }

    /**
     * Writes a CoverableView's covers to NBT
     *
     * @param tagCompound   the tag compound to write to
     * @param coverableView the CoverableView containing the cover
     */
    public static void writeCoverNBT(@NotNull NBTTagCompound tagCompound, @NotNull CoverableView coverableView) {
        NBTTagList coversList = new NBTTagList();
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            Cover cover = coverableView.getCoverAtSide(coverSide);
            if (cover != null) {
                NBTTagCompound tag = new NBTTagCompound();
                ResourceLocation coverId = cover.getDefinition().getCoverId();
                tag.setString("CoverId", coverId.toString());
                tag.setByte("Side", (byte) coverSide.getIndex());
                cover.writeToNBT(tag);
                coversList.appendTag(tag);
            }
        }
        tagCompound.setTag("Covers", coversList);
    }

    /**
     * Reads a CoverableView's covers from NBT
     *
     * @param tagCompound        the tag compound to read from
     * @param coverHolder        the CoverHolder to store the covers in
     * @param coverStoreFunction a function to directly store the cover field
     */
    public static void readCoverNBT(@NotNull NBTTagCompound tagCompound, @NotNull CoverHolder coverHolder,
                                    @NotNull BiConsumer<EnumFacing, Cover> coverStoreFunction) {
        NBTTagList coversList = tagCompound.getTagList("Covers", Constants.NBT.TAG_COMPOUND);
        for (int index = 0; index < coversList.tagCount(); index++) {
            NBTTagCompound tag = coversList.getCompoundTagAt(index);
            if (tag.hasKey("CoverId", Constants.NBT.TAG_STRING)) {
                EnumFacing coverSide = EnumFacing.VALUES[tag.getByte("Side")];
                ResourceLocation coverLocation = new ResourceLocation(tag.getString("CoverId"));
                CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverLocation);
                if (coverDefinition == null) {
                    GTLog.logger.warn("Unable to find CoverDefinition for ResourceLocation {} at position {}",
                            coverLocation, coverHolder.getPos());
                } else {
                    Cover cover = coverDefinition.createCover(coverHolder, coverSide);
                    cover.readFromNBT(tag);
                    coverStoreFunction.accept(coverSide, cover);
                }
            }
        }
    }
}
