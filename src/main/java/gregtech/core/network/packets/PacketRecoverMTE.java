package gregtech.core.network.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

public class PacketRecoverMTE implements IPacket, IServerExecutor {

    private int dimension;
    private BlockPos pos;

    @SuppressWarnings("unused")
    public PacketRecoverMTE() {}

    public PacketRecoverMTE(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(dimension);
        buf.writeBlockPos(pos);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.dimension = buf.readVarInt();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).isValid()) {
            IGregTechTileEntity holder = (IGregTechTileEntity) te;
            holder.writeCustomData(INITIALIZE_MTE, buffer -> {
                buffer.writeVarInt(
                        GregTechAPI.MTE_REGISTRY.getIdByObjectName(holder.getMetaTileEntity().metaTileEntityId));
                holder.getMetaTileEntity().writeInitialSyncData(buffer);
            });
        } else if (!(world.getBlockState(pos).getBlock() instanceof BlockMachine)) {
            handler.player.connection.sendPacket(new SPacketBlockChange(world, pos));
        }
    }
}
