package gregtech.mixins.minecraft;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gregtech.api.damagesources.DamageSourceTool;
import gregtech.api.items.toolitem.IGTTool;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSource.class)
@SuppressWarnings("unused")
public class DamageSourceMixin {

    @ModifyReturnValue(method = "causePlayerDamage", at = @At("RETURN"))
    private static DamageSource modifyPlayerDamageWithTool(DamageSource originalReturnValue, EntityPlayer source) {
        ItemStack stack = source != null ? source.getHeldItemMainhand() : ItemStack.EMPTY;
        if (!stack.isEmpty() && stack.getItem() instanceof IGTTool tool) {
            return new DamageSourceTool("player", source, String.format("death.attack.%s", tool.getId()));
        }
        return originalReturnValue;
    }

    @ModifyReturnValue(method = "causeMobDamage", at = @At("RETURN"))
    private static DamageSource modifyMobDamageWithTool(DamageSource originalReturnValue, EntityLivingBase source) {
        ItemStack stack = source != null ? source.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) : ItemStack.EMPTY;
        if (!stack.isEmpty() && stack.getItem() instanceof IGTTool tool) {
            return new DamageSourceTool("mob", source, String.format("death.attack.%s", tool.getId()));
        }
        return new EntityDamageSource("mob", source);

    }
}
