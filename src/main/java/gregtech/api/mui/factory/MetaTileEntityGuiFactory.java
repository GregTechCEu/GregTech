package gregtech.api.mui.factory;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MetaTileEntityGuiFactory extends AbstractUIFactory<PosGuiData> {

    public static final MetaTileEntityGuiFactory INSTANCE = new MetaTileEntityGuiFactory();

    private MetaTileEntityGuiFactory() {
        super("gregtech:mte");
    }

    public static <T extends MetaTileEntity & IGuiHolder<PosGuiData>> void open(EntityPlayer player, T mte) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(mte);
        if (!mte.isValid()) {
            throw new IllegalArgumentException("Can't open invalid MetaTileEntity GUI!");
        }
        if (player.world != mte.getWorld()) {
            throw new IllegalArgumentException("MetaTileEntity must be in same dimension as the player!");
        }
        BlockPos pos = mte.getPos();
        PosGuiData data = new PosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
    }

    @Override
    public @NotNull IGuiHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        TileEntity te = data.getTileEntity();
        if (te instanceof IGregTechTileEntity gtte) {
            MetaTileEntity mte = gtte.getMetaTileEntity();
            return Objects.requireNonNull(castGuiHolder(mte), "Found MetaTileEntity is not a gui holder!");
        }
        throw new IllegalStateException("Found TileEntity is not a MetaTileEntity!");
    }

    @Override
    public void writeGuiData(PosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
    }

    @Override
    public @NotNull PosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }
}
