package gregtech.mixins.minecraft;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeRepairItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeRepairItem.class)
public class RecipeRepairItemMixin {

    @Inject(method = "matches(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/List;get(I)Ljava/lang/Object;",
                     shift = At.Shift.AFTER),
            cancellable = true)
    public void gregtechCEu$matches(InventoryCrafting inv, World worldIn, CallbackInfoReturnable<Boolean> cir,
                                    @Local(ordinal = 0) ItemStack itemstack, @Local(ordinal = 0) List<ItemStack> list) {
        ItemStack itemstack1 = list.get(0);
        if (itemstack.getItem() instanceof IGTTool first &&
                itemstack1.getItem() instanceof IGTTool second) {
            if (first.isElectric() || second.isElectric()) {
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(first.getToolMaterial(itemstack) == second.getToolMaterial(itemstack1));
            }
        }
    }

    @Inject(method = "getCraftingResult(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Ljava/util/List;get(I)Ljava/lang/Object;",
                     ordinal = 0,
                     shift = At.Shift.BY,
                     by = 2),
            cancellable = true)
    public void gregtechCEu$getCraftingResultFirst(InventoryCrafting inv, CallbackInfoReturnable<ItemStack> cir,
                                                   @Local(ordinal = 0) ItemStack itemstack,
                                                   @Local(ordinal = 1) ItemStack itemstack1) {
        if (itemstack.getItem() instanceof IGTTool tool && tool.isElectric()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else if (itemstack1.getItem() instanceof IGTTool tool && tool.isElectric()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    /*
     * @Inject(method = "getCraftingResult(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;",
     * at = @At(value = "RETURN", ordinal = 1),
     * cancellable = true)
     */
    @ModifyReturnValue(method = "getCraftingResult", at = @At(value = "RETURN", ordinal = 1))
    public ItemStack gregtechCEu$getCraftingResultSecond(ItemStack originalResult, InventoryCrafting inv,
                                                         @Local(ordinal = 3) int itemDamage,
                                                         @Local(ordinal = 0) ItemStack itemstack2,
                                                         @Local(ordinal = 1) ItemStack itemstack3) {
        if (itemstack2.getItem() instanceof IGTTool first && itemstack3.getItem() instanceof IGTTool) {
            // do not allow repairing tools if both are full durability
            if (itemstack2.getItemDamage() == 0 && itemstack3.getItemDamage() == 0) {
                return ItemStack.EMPTY;
            } else {
                ItemStack output = first.get(first.getToolMaterial(itemstack2));
                NBTTagCompound outputTag = ToolHelper.getToolTag(output);
                outputTag.setInteger(ToolHelper.DURABILITY_KEY, itemDamage);
                return output;
            }
        }

        return originalResult;
    }

    @WrapOperation(method = "getRemainingItems",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    public Object gregtechCEU$getRemainingItemsWrap(NonNullList<Object> instance, int index, Object newValue,
                                                    Operation<Object> original,
                                                    @Local(ordinal = 0) ItemStack itemStack) {
        if (itemStack.getItem() instanceof IGTTool) {
            ForgeEventFactory.onPlayerDestroyItem(ForgeHooks.getCraftingPlayer(), itemStack, null);
            return instance.get(index);
        } else {
            return instance.set(index, newValue);
        }
    }
}
