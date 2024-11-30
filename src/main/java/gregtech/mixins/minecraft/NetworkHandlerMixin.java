package gregtech.mixins.minecraft;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetworkHandlerMixin {

    @Shadow
    public EntityPlayerMP player;

    @Inject(method = "processClickWindow",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/inventory/Container;slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    private void setPlayer(CPacketClickWindow packetIn, CallbackInfo ci) {
        ForgeHooks.setCraftingPlayer(this.player);
        for (Slot slot : this.player.openContainer.inventorySlots) {
            if (slot.getStack().getItem() instanceof ItemGTToolbelt)
                ItemGTToolbelt.setCraftingSlot(slot.slotNumber);
        }
    }

    @Inject(method = "processClickWindow",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/inventory/Container;slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    private void clearPlayer(CPacketClickWindow packetIn, CallbackInfo ci) {
        ForgeHooks.setCraftingPlayer(null);
        ItemGTToolbelt.setCraftingSlot(-999);
    }
}
