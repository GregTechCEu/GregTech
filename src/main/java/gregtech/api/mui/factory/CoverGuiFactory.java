package gregtech.api.mui.factory;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.cover.CoverWithUI;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CoverGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final CoverGuiFactory INSTANCE = new CoverGuiFactory();

    private CoverGuiFactory() {
        super("gregtech:cover");
    }

    public static <T extends Cover & IGuiHolder<SidedPosGuiData>> void open(EntityPlayer player, T cover) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(cover);
        if (!cover.getCoverableView().isValid()) {
            throw new IllegalArgumentException("Can't open Cover GUI on invalid cover holder!");
        }
        if (player.world != cover.getWorld()) {
            throw new IllegalArgumentException("Cover must be in same dimension as the player!");
        }
        BlockPos pos = cover.getPos();
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), cover.getAttachedSide());
        GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
    }

    @Override
    public @NotNull IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        TileEntity te = data.getTileEntity();
        if (te == null) {
            throw new IllegalStateException("Could not get gui for null TileEntity!");
        }
        CoverHolder coverHolder = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, data.getSide());
        if (coverHolder == null) {
            throw new IllegalStateException("Could not get CoverHolder for found TileEntity!");
        }
        Cover cover = coverHolder.getCoverAtSide(data.getSide());
        if (cover == null) {
            throw new IllegalStateException("Could not find cover at side " + data.getSide() +
                    " for found CoverHolder!");
        }
        if (!(cover instanceof CoverWithUI coverWithUI)) {
            throw new IllegalStateException("Cover at side " + data.getSide() + " is not a gui holder!");
        }
        return coverWithUI;
    }

    @Override
    public void writeGuiData(SidedPosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
        buffer.writeByte(guiData.getSide().getIndex());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new SidedPosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                EnumFacing.VALUES[buffer.readByte()]);
    }
}
