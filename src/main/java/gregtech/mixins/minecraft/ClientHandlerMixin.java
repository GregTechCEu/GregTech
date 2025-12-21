package gregtech.mixins.minecraft;

import gregtech.client.utils.ClientHandlerHooks;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class ClientHandlerMixin {

    @Shadow
    private WorldClient world;

    /**
     * Initializes any MetaTileEntities right before {@link Chunk#read(PacketBuffer, int, boolean) Chunk.read()} is
     * called in
     * {@link NetHandlerPlayClient#handleChunkData(SPacketChunkData) handleChunkData()}.
     * 
     * @param packetIn Chunk Data Packet
     */
    @Inject(method = "handleChunkData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/chunk/Chunk;read(Lnet/minecraft/network/PacketBuffer;IZ)V"))
    public void initClientTiles(SPacketChunkData packetIn, CallbackInfo ci) {
        // todo this might not be necessary anymore, though the TE does need to be initialized for initial sync data
        ClientHandlerHooks.handleTags(this.world, packetIn.getTileEntityTags());
    }

    /**
     * Initializes the MetaTileEntity before it handles
     * 
     * @param packetIn Tile Entity Update Packet
     */
    @Inject(method = "handleUpdateTileEntity",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/multiplayer/WorldClient;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
    public void initClientTile(SPacketUpdateTileEntity packetIn, CallbackInfo ci) {
        ClientHandlerHooks.handleTag(this.world, packetIn.getNbtCompound());
    }
}
