package gregtech.mixins.minecraft;

import gregtech.api.damagesources.DamageSources;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSource.class)
public class DamageSourceMixin {

    @ModifyReturnValue(method = "causePlayerDamage", at = @At("RETURN"))
    private static DamageSource modifyPlayerDamageWithTool(DamageSource originalReturnValue, EntityPlayer source) {
        return DamageSources.getPlayerDamage(source);
    }

    @ModifyReturnValue(method = "causeMobDamage", at = @At("RETURN"))
    private static DamageSource modifyMobDamageWithTool(DamageSource originalReturnValue, EntityLivingBase source) {
        return DamageSources.getMobDamage(source);
    }
}
