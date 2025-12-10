package gregtech.api.mui.factory;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MetaTileEntityGuiFactory extends AbstractUIFactory<MetaTileEntityGuiData> {

    public static final MetaTileEntityGuiFactory INSTANCE = new MetaTileEntityGuiFactory();

    private MetaTileEntityGuiFactory() {
        super("gregtech:mte");
    }

    public static <T extends MetaTileEntity & IMetaTileEntityGuiHolder> void open(EntityPlayerMP player, T mte) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(mte);
        if (!mte.isValid()) {
            throw new IllegalArgumentException("Can't open invalid MetaTileEntity GUI!");
        }
        if (player.world != mte.getWorld()) {
            throw new IllegalArgumentException("MetaTileEntity must be in same dimension as the player!");
        }
        BlockPos pos = mte.getPos();
        MetaTileEntityGuiData data = new MetaTileEntityGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        mte.writeExtraGuiData(data.getBufferInternal());
        GuiManager.open(INSTANCE, data, player);
    }

    @Override
    public @NotNull IGuiHolder<MetaTileEntityGuiData> getGuiHolder(MetaTileEntityGuiData data) {
        MetaTileEntity mte = data.getMetaTileEntity();
        if (mte != null) {
            return Objects.requireNonNull(castGuiHolder(mte), "Found MetaTileEntity is not a gui holder!");
        }
        throw new IllegalStateException("Found TileEntity is not a MetaTileEntity!");
    }

    @Override
    public void writeGuiData(MetaTileEntityGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());

        PacketBuffer guiDataBuffer = guiData.getBufferInternal();
        int length = guiDataBuffer.writerIndex();
        buffer.writeVarInt(length);
        buffer.writeBytes(guiDataBuffer, 0, length);
    }

    @Override
    public @NotNull MetaTileEntityGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        MetaTileEntityGuiData guiData = new MetaTileEntityGuiData(player, buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt());
        buffer.readBytes(guiData.getBufferInternal(), buffer.readVarInt());

        return guiData;
    }
}
