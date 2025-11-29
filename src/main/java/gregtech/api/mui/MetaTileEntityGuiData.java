package gregtech.api.mui;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.factory.PosGuiData;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class MetaTileEntityGuiData extends PosGuiData {

    private final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

    public MetaTileEntityGuiData(@NotNull EntityPlayer player, int x, int y, int z) {
        super(player, x, y, z);
    }

    @UnknownNullability
    public MetaTileEntity getMetaTileEntity() {
        return getTileEntity() instanceof IGregTechTileEntity igtte ? igtte.getMetaTileEntity() : null;
    }

    /**
     * Directly get the stored {@link PacketBuffer}. Should not be used outside of
     * {@link MetaTileEntityGuiFactory} as data should only be written in
     * {@link IMetaTileEntityGuiHolder#writeExtraGuiData(PacketBuffer)}.
     */
    @ApiStatus.Internal
    @NotNull
    public PacketBuffer getBufferInternal() {
        return buffer;
    }

    /**
     * Get the byte buffer with data written server-side in
     * {@link IMetaTileEntityGuiHolder#writeExtraGuiData(PacketBuffer)}.
     */
    @NotNull
    public PacketBuffer getBuffer() {
        return new PacketBuffer(buffer.asReadOnly());
    }
}
