package gregtech.api.net;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CPacketRecoverMTE implements NetworkHandler.Packet {
    public World world;
    public BlockPos pos;


    public CPacketRecoverMTE(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public static void registerPacket(int packetId) {
        NetworkHandler.registerPacket(packetId, CPacketRecoverMTE.class, new NetworkHandler.PacketCodec<>(
                (packet, buf) -> {
                    buf.writeVarInt(packet.world.provider.getDimension());
                    buf.writeBlockPos(packet.pos);
                },
                (buf) -> new CPacketRecoverMTE(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(buf.readVarInt()), buf.readBlockPos())
        ));
    }

    public static void registerExecutor() {
        NetworkHandler.registerServerExecutor(CPacketRecoverMTE.class, (packet, handler) -> {
            TileEntity te = packet.world.getTileEntity(packet.pos);
            if (te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).isValid()) {
                ((MetaTileEntityHolder) te).sendInitialSyncData();
            }
        });
    }

}
