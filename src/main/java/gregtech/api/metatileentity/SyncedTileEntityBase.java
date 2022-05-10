package gregtech.api.metatileentity;

import gregtech.api.block.BlockStateTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SyncedTileEntityBase extends BlockStateTileEntity {

    public abstract void writeInitialSyncData(PacketBuffer buf);

    public abstract void receiveInitialSyncData(PacketBuffer buf);

    public abstract void receiveCustomData(int discriminator, PacketBuffer buf);

    protected final Int2ObjectMap<byte[]> updates = new Int2ObjectArrayMap<>(5);

    public void writeCustomData(int discriminator, Consumer<PacketBuffer> dataWriter) {
        ByteBuf backedBuffer = Unpooled.buffer();
        dataWriter.accept(new PacketBuffer(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updates.put(discriminator, updateData);
        @SuppressWarnings("deprecation")
        IBlockState blockState = getBlockType().getStateFromMeta(getBlockMetadata());
        world.notifyBlockUpdate(getPos(), blockState, blockState, 0);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        if (this.updates.isEmpty()) {
            return null;
        }
        NBTTagCompound updateTag = new NBTTagCompound();
        NBTTagList listTag = new NBTTagList();
        for (Int2ObjectMap.Entry<byte[]> entry : updates.int2ObjectEntrySet()) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setByteArray(Integer.toString(entry.getIntKey()), entry.getValue());
            listTag.appendTag(entryTag);
        }
        updateTag.setTag("d", listTag);
        this.updates.clear();
        return new SPacketUpdateTileEntity(getPos(), 0, updateTag);
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound updateTag = pkt.getNbtCompound();
        NBTTagList listTag = updateTag.getTagList("d", Constants.NBT.TAG_COMPOUND);
        for (NBTBase entryBase : listTag) {
            NBTTagCompound entryTag = (NBTTagCompound) entryBase;
            for (String discriminatorKey : entryTag.getKeySet()) {
                ByteBuf backedBuffer = Unpooled.copiedBuffer(entryTag.getByteArray(discriminatorKey));
                receiveCustomData(Integer.parseInt(discriminatorKey), new PacketBuffer(backedBuffer));
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        ByteBuf backedBuffer = Unpooled.buffer();
        writeInitialSyncData(new PacketBuffer(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateTag.setByteArray("d", updateData);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag); // deserializes Forge data and capabilities
        byte[] updateData = tag.getByteArray("d");
        ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
        receiveInitialSyncData(new PacketBuffer(backedBuffer));
    }

}
