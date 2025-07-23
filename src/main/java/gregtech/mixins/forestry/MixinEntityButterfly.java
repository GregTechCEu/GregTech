package gregtech.mixins.forestry;

import net.minecraft.entity.Entity;

import forestry.lepidopterology.entities.EntityButterfly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gregtech.common.ConfigHolder.vanillaOptimizeOptions;

@Mixin(EntityButterfly.class)
public abstract class MixinEntityButterfly {

    @Inject(
            method = "onUpdate",  // 目标方法：实体每帧更新时调用
            at = @At("HEAD")      // 在方法开头注入
            )
    private void onUpdateHead(CallbackInfo ci) {
        if(vanillaOptimizeOptions.killButterflyEnable) {
            Entity entity = (Entity) (Object) this;
            entity.setDead();  // 标记实体为死亡状态
        }
    }
}
