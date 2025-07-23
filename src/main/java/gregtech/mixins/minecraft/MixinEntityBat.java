package gregtech.mixins.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gregtech.common.ConfigHolder.vanillaOptimizeOptions;

@Mixin(EntityBat.class)
public abstract class MixinEntityBat {

    @Inject(
            method = "onUpdate",  // 目标方法：实体每帧更新时调用
            at = @At("HEAD")      // 在方法开头注入
            )
    private void onUpdateHead(CallbackInfo ci) {
        if(vanillaOptimizeOptions.killEntityEnable) {
            Entity entity = (Entity) (Object) this;
            entity.setDead();  // 标记实体为死亡状态
        }
    }
}
