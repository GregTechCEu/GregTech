package gregtech.api.net;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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

    public static FMLProxyPacket packet2proxy(IPacket packet) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(NetworkHandler.packetMap.getId(packet.getClass()));
        packet.encode(buf);
        return new FMLProxyPacket(buf, GTValues.MODID);
    }

    public static IPacket proxy2packet(FMLProxyPacket proxyPacket) throws Exception {
        PacketBuffer payload = (PacketBuffer) proxyPacket.payload();
        IPacket packet = NetworkHandler.packetMap.get(payload.readVarInt()).newInstance();
        packet.decode(payload);
        return packet;
    }

    public static NetworkRegistry.TargetPoint blockPoint(World world, BlockPos blockPos) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 128.0);
    }

    protected static List<Class<?>> getPacketClasses(ASMDataTable table) {
        List<Class<?>> packetClasses = new ArrayList<>();
        for (ASMDataTable.ASMData data : table.getAll(IPacket.Packet.class.getName())) {
            String className = data.getClassName();
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                GTLog.logger.error("Failed to load network packet class {}, skipping...", className);
                continue;
            }

            // Ensure class extends IPacket
            if (!IPacket.class.isAssignableFrom(clazz)) {
                GTLog.logger.error("Could not register packet class {} as it does not extend IPacket, skipping...", className);
                continue;
            }

            // Ensure class is not abstract
            if (Modifier.isAbstract(clazz.getModifiers())) {
                GTLog.logger.error("Could not register packet class {} as it is abstract, skipping...", className);
                continue;
            }

            // Ensure class has no-args constructor
            try {
                clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                GTLog.logger.error("Could not register packet class {} as it is missing a no-args constructor, skipping...", className);
                continue;
            }

            // Ensure class extends either IServerExecutor or IClientExecutor, but not both
            if (IServerExecutor.class.isAssignableFrom(clazz) || IClientExecutor.class.isAssignableFrom(clazz)) {
                if (IServerExecutor.class.isAssignableFrom(clazz) && IClientExecutor.class.isAssignableFrom(clazz)) {
                    GTLog.logger.error("Could not register packet class {} as it implements both IServerExecutor and IClientExecutor (only one allowed), skipping...", className);
                }
            } else {
                GTLog.logger.error("Could not register packet class {} as it does not implement IServerExecutor or IClientExecutor (must implement one), skipping...", className);
            }

            packetClasses.add(clazz);
        }
        return packetClasses;
    }
}
