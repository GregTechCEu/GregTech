package gregtech.api.mui;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.factory.PosGuiData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class MetaTileEntityGuiData extends PosGuiData {

    private final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

    public MetaTileEntityGuiData(@NotNull EntityPlayer player, int x, int y, int z, @NotNull ByteBuf srcBuffer) {
        super(player, x, y, z);
        buffer.writeBytes(srcBuffer);
    }

    public MetaTileEntityGuiData(@NotNull EntityPlayer player, int x, int y, int z, @NotNull MetaTileEntity mte) {
        super(player, x, y, z);
        mte.writeExtraGuiData(buffer);
    }

    @UnknownNullability
    public MetaTileEntity getMetaTileEntity() {
        return getTileEntity() instanceof IGregTechTileEntity igtte ? igtte.getMetaTileEntity() : null;
    }

    @NotNull
    public PacketBuffer getBuffer() {
        return buffer;
    }
}
