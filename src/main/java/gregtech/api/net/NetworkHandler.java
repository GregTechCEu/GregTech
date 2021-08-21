package gregtech.api.net;

import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import gregtech.api.block.ICustomParticleBlock;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.GTLog;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;

public class NetworkHandler {

    public interface Packet {

        default FMLProxyPacket toFMLPacket() {
            return packet2proxy(this);
        }
    }

    @FunctionalInterface
    public interface PacketEncoder<T extends Packet> {
        void encode(T packet, PacketBuffer byteBuf);
    }

    @FunctionalInterface
    public interface PacketDecoder<T extends Packet> {
        T decode(PacketBuffer byteBuf);
    }

    @FunctionalInterface
    public interface PacketExecutor<T, R extends INetHandler> {
        void execute(T packet, R handler);
    }

    public static final class PacketCodec<T extends Packet> {

        public final PacketEncoder<T> encoder;
        public final PacketDecoder<T> decoder;

        public PacketCodec(PacketEncoder<T> encoder, PacketDecoder<T> decoder) {
            this.encoder = encoder;
            this.decoder = decoder;
        }
    }

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("Gregtech");
    private static final HashMap<Class<? extends Packet>, PacketCodec<? extends Packet>> codecMap = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private static HashMap<Class<? extends Packet>, PacketExecutor<? extends Packet, NetHandlerPlayClient>> clientExecutors;
    private static final HashMap<Class<? extends Packet>, PacketExecutor<? extends Packet, NetHandlerPlayServer>> serverExecutors = new HashMap<>();
    private static final IntIdentityHashBiMap<Class<? extends Packet>> packetMap = new IntIdentityHashBiMap<>(10);

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            clientExecutors = new HashMap<>();
        }
    }

    public static FMLEventChannel channel;

    private NetworkHandler() {
    }

    public static void init() {
        INSTANCE.registerMessage(KeysUpdateHandler.class, KeysPacket.class, 0, Side.SERVER);
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(GTValues.MODID);
        channel.register(new NetworkHandler());

        PacketEncoder<PacketUIWidgetUpdate> widgetUpdateEncoder = (packet, buf) -> {
            buf.writeVarInt(packet.updateData.readableBytes());
            buf.writeBytes(packet.updateData);
            buf.writeVarInt(packet.windowId);
            buf.writeVarInt(packet.widgetId);
        };

        PacketDecoder<PacketUIWidgetUpdate> widgetUpdateDecoder = (buf) -> {
            ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
            ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
            directSliceBuffer.release();
            return new PacketUIWidgetUpdate(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    new PacketBuffer(copiedDataBuffer));
        };

        registerPacket(1, PacketUIOpen.class, new PacketCodec<>(
                (packet, buf) -> {
                    buf.writeVarInt(packet.serializedHolder.readableBytes());
                    buf.writeBytes(packet.serializedHolder);
                    buf.writeVarInt(packet.uiFactoryId);
                    buf.writeVarInt(packet.windowId);
                    buf.writeVarInt(packet.initialWidgetUpdates.size());
                    for (PacketUIWidgetUpdate widgetUpdate : packet.initialWidgetUpdates) {
                        widgetUpdateEncoder.encode(widgetUpdate, buf);
                    }
                },
                (buf) -> {
                    ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
                    ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
                    directSliceBuffer.release();
                    int uiFactoryId = buf.readVarInt();
                    int windowId = buf.readVarInt();
                    ArrayList<PacketUIWidgetUpdate> initialWidgetUpdates = new ArrayList<>();
                    int initialWidgetUpdatesCount = buf.readVarInt();
                    for (int i = 0; i < initialWidgetUpdatesCount; i++) {
                        initialWidgetUpdates.add(widgetUpdateDecoder.decode(buf));
                    }
                    return new PacketUIOpen(
                            uiFactoryId,
                            new PacketBuffer(copiedDataBuffer),
                            windowId,
                            initialWidgetUpdates);
                }
        ));

        registerPacket(2, PacketUIWidgetUpdate.class, new PacketCodec<>(
                (packet, buf) -> {
                    buf.writeVarInt(packet.updateData.readableBytes());
                    buf.writeBytes(packet.updateData);
                    buf.writeVarInt(packet.windowId);
                    buf.writeVarInt(packet.widgetId);
                },
                (buf) -> {
                    ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
                    ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
                    directSliceBuffer.release();
                    return new PacketUIWidgetUpdate(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            new PacketBuffer(copiedDataBuffer));
                }
        ));

        registerPacket(3, PacketUIClientAction.class, new PacketCodec<>(
                (packet, buf) -> {
                    buf.writeVarInt(packet.updateData.readableBytes());
                    buf.writeBytes(packet.updateData);
                    buf.writeVarInt(packet.windowId);
                    buf.writeVarInt(packet.widgetId);
                },
                (buf) -> {
                    ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
                    ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
                    directSliceBuffer.release();
                    return new PacketUIClientAction(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            new PacketBuffer(copiedDataBuffer));
                }
        ));

        registerPacket(4, PacketBlockParticle.class, new PacketCodec<>(
                (packet, buf) -> {
                    buf.writeBlockPos(packet.blockPos);
                    buf.writeFloat((float) packet.entityPos.x);
                    buf.writeFloat((float) packet.entityPos.y);
                    buf.writeFloat((float) packet.entityPos.z);
                    buf.writeVarInt(packet.particlesAmount);
                },
                (buf) -> new PacketBlockParticle(buf.readBlockPos(),
                        new Vector3(buf.readFloat(), buf.readFloat(), buf.readFloat()),
                        buf.readVarInt())
        ));

        registerPacket(5, PacketClipboard.class, new PacketCodec<>(
                (packet, buf) -> buf.writeString(packet.text),
                (buf) -> new PacketClipboard(buf.readString(32767))
        ));

        registerPacket(6, PacketClipboardUIWidgetUpdate.class, new NetworkHandler.PacketCodec<>(
                (packet, buf) -> {
                    buf.writeVarInt(packet.clipboard.getWorld().provider.getDimension());
                    buf.writeBlockPos(packet.clipboard.getPos());
                    buf.writeVarInt(packet.id);
                    if(packet.payloadWriter != null) {
                        packet.payloadWriter.accept(buf);
                    }
                },
                (buf) -> {
                    int dim = buf.readVarInt();
                    BlockPos pos = buf.readBlockPos();
                    TileEntity te = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim).getTileEntity(pos);
                    if(te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
                        return new PacketClipboardUIWidgetUpdate((MetaTileEntityClipboard) ((MetaTileEntityHolder) te).getMetaTileEntity(), buf);
                    }
                    return new PacketClipboardUIWidgetUpdate(null, buf);
                }
        ));


        registerServerExecutor(PacketUIClientAction.class, (packet, handler) -> {
            Container openContainer = handler.player.openContainer;
            if (openContainer instanceof ModularUIContainer &&
                    openContainer.windowId == packet.windowId) {
                ModularUI modularUI = ((ModularUIContainer) openContainer).getModularUI();
                PacketBuffer buffer = packet.updateData;
                modularUI.guiWidgets.get(packet.widgetId).handleClientAction(buffer.readVarInt(), buffer);
            }
        });

        NetworkHandler.registerServerExecutor(PacketClipboardUIWidgetUpdate.class, (packet, handler) -> {
            if (packet.clipboard != null) {
                packet.clipboard.readUIAction(handler.player, packet.id, packet.buf);
            }
        });

        if (FMLCommonHandler.instance().getSide().isClient()) {
            initClient();
        }

    }

    @SideOnly(Side.CLIENT)
    private static void initClient() {
        registerClientExecutor(PacketUIOpen.class, (packet, handler) -> {
            UIFactory<?> uiFactory = UIFactory.FACTORY_REGISTRY.getObjectById(packet.uiFactoryId);
            if (uiFactory == null) {
                GTLog.logger.warn("Couldn't find UI Factory with id '{}'", packet.uiFactoryId);
            } else {
                uiFactory.initClientUI(packet.serializedHolder, packet.windowId, packet.initialWidgetUpdates);
            }
        });
        registerClientExecutor(PacketUIWidgetUpdate.class, (packet, handler) -> {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (currentScreen instanceof ModularUIGui) {
                ((ModularUIGui) currentScreen).handleWidgetUpdate(packet);
            }
        });

        registerClientExecutor(PacketBlockParticle.class, (packet, handler) -> {
            World world = Minecraft.getMinecraft().world;
            IBlockState blockState = world.getBlockState(packet.blockPos);
            ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;
            ((ICustomParticleBlock) blockState.getBlock()).handleCustomParticle(world, packet.blockPos, particleManager, packet.entityPos, packet.particlesAmount);
        });

        registerClientExecutor(PacketClipboard.class, (packet, handler) -> ClipboardUtil.copyToClipboard(packet.text));
    }

    public static <T extends Packet> void registerPacket(int packetId, Class<T> packetClass, PacketCodec<T> codec) {
        packetMap.put(packetClass, packetId);
        codecMap.put(packetClass, codec);
    }

    @SideOnly(Side.CLIENT)
    public static <T extends Packet> void registerClientExecutor(Class<T> packet, PacketExecutor<T, NetHandlerPlayClient> executor) {
        clientExecutors.put(packet, executor);
    }

    public static <T extends Packet> void registerServerExecutor(Class<T> packet, PacketExecutor<T, NetHandlerPlayServer> executor) {
        serverExecutors.put(packet, executor);
    }

    @SuppressWarnings("unchecked")
    public static FMLProxyPacket packet2proxy(Packet packet) {
        PacketCodec<Packet> codec = (PacketCodec<Packet>) codecMap.get(packet.getClass());
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(packetMap.getId(packet.getClass()));
        codec.encoder.encode(packet, buf);
        return new FMLProxyPacket(buf, GTValues.MODID);
    }

    @SuppressWarnings("unchecked")
    public static Packet proxy2packet(FMLProxyPacket packet) {
        PacketBuffer payload = (PacketBuffer) packet.payload();
        Class<Packet> packetClass = (Class<Packet>) packetMap.get(payload.readVarInt());
        PacketCodec<Packet> codec = (PacketCodec<Packet>) codecMap.get(packetClass);
        return codec.decoder.decode(payload);
    }

    public static TargetPoint blockPoint(World world, BlockPos blockPos) {
        return new TargetPoint(world.provider.getDimension(), blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 128.0);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        Packet packet = proxy2packet(event.getPacket());
        if (clientExecutors.containsKey(packet.getClass())) {
            PacketExecutor<Packet, NetHandlerPlayClient> executor = (PacketExecutor<Packet, NetHandlerPlayClient>) clientExecutors.get(packet.getClass());
            NetHandlerPlayClient handler = (NetHandlerPlayClient) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                executor.execute(packet, handler);
            } else {
                threadListener.addScheduledTask(() -> executor.execute(packet, handler));
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        Packet packet = proxy2packet(event.getPacket());
        if (serverExecutors.containsKey(packet.getClass())) {
            PacketExecutor<Packet, NetHandlerPlayServer> executor = (PacketExecutor<Packet, NetHandlerPlayServer>) serverExecutors.get(packet.getClass());
            NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                executor.execute(packet, handler);
            } else {
                threadListener.addScheduledTask(() -> executor.execute(packet, handler));
            }
        }
    }
}
