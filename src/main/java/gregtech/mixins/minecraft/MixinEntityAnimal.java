package gregtech.mixins.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gregtech.common.ConfigHolder.vanillaOptimizeOptions;

@Mixin(EntityAnimal.class)
public abstract class MixinEntityAnimal {

    @Inject(
            method = "onLivingUpdate",  // 目标方法：实体每帧更新时调用
            at = @At("HEAD")      // 在方法开头注入
            )
    private void onUpdateHead(CallbackInfo ci) {
        if(vanillaOptimizeOptions.killAnimalEnable) {
            Entity entity = (Entity) (Object) this;
            entity.setDead();  // 标记实体为死亡状态
        }
    }
}
