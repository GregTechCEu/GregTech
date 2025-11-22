package gregtech.mixins.mui2;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.ByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// todo remove in next mui2 update
@Mixin(targets = "com.cleanroommc.modularui.utils.serialization.ByteBufAdapters$3")
public class BigIntAdapterMixin {

    @Redirect(method = "serialize(Lnet/minecraft/network/PacketBuffer;Ljava/math/BigInteger;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/network/PacketBuffer;writeBytes([B)Lio/netty/buffer/ByteBuf;"))
    public ByteBuf fixIncorrectCall(PacketBuffer instance, byte[] bytes) {
        return instance.writeByteArray(bytes);
    }
}
