package gregtech.core.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NetworkUtils {

    public static void writePacketBuffer(PacketBuffer writeTo, PacketBuffer writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new PacketBuffer(copiedDataBuffer);
    }

    public static TileEntity getTileEntityServer(int dimension, BlockPos pos) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension).getTileEntity(pos);
    }

    public static IBlockState getIBlockStateServer(int dimension, BlockPos pos) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension).getBlockState(pos);
    }

    public static NetworkRegistry.TargetPoint blockPoint(World world, BlockPos blockPos) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), blockPos.getX() + 0.5,
                blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 128.0);
    }
}
