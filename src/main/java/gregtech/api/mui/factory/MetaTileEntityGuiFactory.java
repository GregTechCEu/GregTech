package gregtech.api.mui.factory;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MetaTileEntityGuiFactory extends AbstractUIFactory<PosGuiData> {

    public static final MetaTileEntityGuiFactory INSTANCE = new MetaTileEntityGuiFactory();

    private static final Long2ObjectMap<Set<UUID>> openedUIs = new Long2ObjectArrayMap<>();

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
        Set<UUID> players = openedUIs.computeIfAbsent(pos.toLong(), key -> new ObjectOpenHashSet<>());
        if (players.add(player.getUniqueID())) {
            PosGuiData data = new PosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
            GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
        }
    }

    public static void close(BlockPos pos, World world) {
        if (world.isRemote) {
            return;
        }
        Iterator<UUID> iterator = openedUIs.get(pos.toLong()).iterator();
        while (iterator.hasNext()) {
            var p = world.getPlayerEntityByUUID(iterator.next());
            if (p != null) {
                p.closeScreen();
                iterator.remove();
            }
        }
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
