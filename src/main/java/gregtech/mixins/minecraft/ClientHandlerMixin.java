package gregtech.mixins.minecraft;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.GTBaseTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class ClientHandlerMixin {

    @Inject(method = "handleChunkData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/chunk/Chunk;read(Lnet/minecraft/network/PacketBuffer;IZ)V"))
    public void initClientTiles(SPacketChunkData packetIn, CallbackInfo ci) {
        for (NBTTagCompound tag : packetIn.getTileEntityTags()) {
            if (!tag.hasKey("MetaId")) continue;
            ResourceLocation metaId = new ResourceLocation(tag.getString("MetaId"));
            MetaTileEntity mte = GregTechAPI.mteManager.getRegistry(metaId.getNamespace()).getObject(metaId);
            BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
            if (!GTBaseTileEntity.hasTE(pos))
                GTBaseTileEntity.storeTE(pos, mte);
        }
    }
}
