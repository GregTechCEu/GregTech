package gregtech.mixins.mui2;

import com.cleanroommc.modularui.value.sync.IntSyncValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

// todo remove once mui2 rc3 is released
@Mixin(value = IntSyncValue.class, remap = false)
public abstract class IntSyncValueMixin {

    @Shadow
    private int cache;

    @Inject(method = "<init>(Ljava/util/function/IntSupplier;Ljava/util/function/IntConsumer;)V", at = @At("TAIL"))
    public void setCache(IntSupplier getter, IntConsumer setter, CallbackInfo ci) {
        this.cache = getter.getAsInt();
    }
}
