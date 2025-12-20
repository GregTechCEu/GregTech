package gregtech.mixins.minecraft;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.registry.MTERegistry;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
        for (NBTTagCompound tag : packetIn.getTileEntityTags()) {
            gregTech$initMetaTile(this.world, tag);
        }
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
        gregTech$initMetaTile(this.world, packetIn.getNbtCompound());
    }

    // todo move this into a hook class or something
    /**
     * Creates the correct MetaTileEntity from the given tag and sets the tile entity to the world
     * IF a TileEntity doesn't already exist at the position
     * 
     * @param tag TileEntity tag data
     */
    @Unique
    public void gregTech$initMetaTile(World world, NBTTagCompound tag) {
        if (!tag.hasKey("MetaId")) return;

        ResourceLocation metaId = new ResourceLocation(tag.getString("MetaId"));
        MTERegistry registry = GregTechAPI.mteManager.getRegistry(metaId.getNamespace());
        MetaTileEntity mte = registry.getObject(metaId);
        BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
        if (mte == null) return;

        // set te in world directly
        // check if world contains a TE at this pos?
        // is null checking good enough?
        if (world.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) == null) {
            if (world.getBlockState(pos).getBlock() != registry.getBlock())
                world.setBlockState(pos, registry.getBlock().getDefaultState());
            world.setTileEntity(pos, mte.copy());
        }
    }
}
