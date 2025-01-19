package gregtech.mixins.minecraft;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public abstract class ContainerMixin {

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/item/ItemStack;splitStack(I)Lnet/minecraft/item/ItemStack;",
                     ordinal = 1))
    private void setPlayer(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player,
                           CallbackInfoReturnable<ItemStack> cir) {
        var playerStack = player.inventory.getItemStack();
        if (player instanceof EntityPlayerMP serverPlayer && playerStack.getItem() instanceof ItemGTToolbelt) {
            ItemGTToolbelt.setCraftingSlot(slotId, serverPlayer);
        }
    }
}
