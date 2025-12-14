package gregtech.mixins.minecraft;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.registry.MTERegistry;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

    @Inject(method = "handleChunkData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/chunk/Chunk;read(Lnet/minecraft/network/PacketBuffer;IZ)V"))
    public void initClientTiles(SPacketChunkData packetIn, CallbackInfo ci) {
        for (NBTTagCompound tag : packetIn.getTileEntityTags()) {
            gregTech$initMetaTile(tag);
        }
    }

    @Inject(method = "handleUpdateTileEntity",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/multiplayer/WorldClient;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
    public void initClientTile(SPacketUpdateTileEntity packetIn, CallbackInfo ci) {
        gregTech$initMetaTile(packetIn.getNbtCompound());
    }

    @Unique
    public void gregTech$initMetaTile(NBTTagCompound tag) {
        if (!tag.hasKey("MetaId")) return;

        ResourceLocation metaId = new ResourceLocation(tag.getString("MetaId"));
        MTERegistry registry = GregTechAPI.mteManager.getRegistry(metaId.getNamespace());
        MetaTileEntity mte = registry.getObject(metaId);
        BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
        if (mte == null) return;

        // set te in world directly
        // check if world contains a TE at this pos?
        // is null checking good enough?
        if (this.world.getChunk(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) == null) {
            if (world.getBlockState(pos).getBlock() != registry.getBlock())
                this.world.setBlockState(pos, registry.getBlock().getDefaultState());
            this.world.setTileEntity(pos, mte.createMetaTileEntity(null));
        }
    }
}
