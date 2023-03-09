package gregtech.api.cover;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contains default implementations for common networking involving covers
 */
public final class CoverNetworking {

    private CoverNetworking() {/**/}

    /**
     * Write sync data for a coverable's covers
     * @param buf the buf to write to
     * @param coverable the coverable containing the covers
     */
    public static void writeCoverSyncData(@Nonnull PacketBuffer buf, @Nonnull ICoverable coverable) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            CoverBehavior coverBehavior = coverable.getCoverAtSide(coverSide);
            if (coverBehavior != null) {
                buf.writeBoolean(true);
                String name = coverBehavior.getCoverDefinition().getCoverId().toString();
                buf.writeString(name);
                coverBehavior.writeInitialSyncData(buf);
            } else {
                // cover was not attached
                buf.writeBoolean(false);
            }
        }
    }

    /**
     * Receive sync data for a coverable's covers
     * @param buf the buf to read from
     * @param coverable the coverable containing the covers
     * @param coverWriter the operation the coverable uses to store a cover
     */
    public static void receiveCoverSyncData(@Nonnull PacketBuffer buf, @Nonnull ICoverable coverable,
                                            @Nonnull BiConsumer<EnumFacing, CoverBehavior> coverWriter) {
        for (EnumFacing coverSide : EnumFacing.VALUES) {
            if (buf.readBoolean()) {
                ResourceLocation coverLocation = new ResourceLocation(buf.readString(Short.MAX_VALUE));
                CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverLocation);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(coverable, coverSide);
                coverBehavior.readInitialSyncData(buf);
                coverWriter.accept(coverSide, coverBehavior);
            }
        }
    }

    /**
     * @param side the side the cover is attached to
     * @param behavior the cover's behavior
     * @return a writer for a cover's attachment data
     */
    @Nonnull
    public static Consumer<PacketBuffer> getCoverPlacementCustomDataWriter(@Nonnull EnumFacing side, @Nonnull CoverBehavior behavior) {
        return buffer -> {
            buffer.writeByte(side.getIndex());
            buffer.writeString(behavior.getCoverDefinition().getCoverId().toString());
            behavior.writeInitialSyncData(buffer);
        };
    }

    /**
     * Read a cover's placement customData
     * @param buf the buffer to read from
     * @param coverable the coverable the cover is placed on
     * @param coverWriter the operation the coverable uses to store a cover
     * @param renderUpdater the operation the coverable uses to schedule render updates
     */
    public static void readCoverPlacement(@Nonnull PacketBuffer buf, @Nonnull ICoverable coverable,
                                          @Nonnull BiConsumer<EnumFacing, CoverBehavior> coverWriter,
                                          @Nonnull Runnable renderUpdater) {
        //cover placement event
        EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
        ResourceLocation coverLocation = new ResourceLocation(buf.readString(Short.MAX_VALUE));
        CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverLocation);
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(coverable, placementSide);
        coverWriter.accept(placementSide, coverBehavior);

        coverBehavior.readInitialSyncData(buf);
        renderUpdater.run();
    }
}
