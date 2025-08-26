package gregtech.mixins.minecraft;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.items.toolitem.ToolHelper;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeRepairItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(RecipeRepairItem.class)
public class RecipeRepairItemMixin {

    @Redirect(method = "matches",
              at = @At(value = "INVOKE",
                       ordinal = 0,
                       target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    public Item gregtechCEu$checkFirst(ItemStack instance) {
        if (instance.getItem() instanceof IGTTool)
            return null; // return null to bypass item check
        else return instance.getItem();
    }

    @Redirect(method = "matches",
              at = @At(value = "INVOKE",
                       ordinal = 1,
                       target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    public Item gregtechCEu$checkSecond(ItemStack instance) {
        if (instance.getItem() instanceof IGTTool)
            return null; // return null to bypass item check
        else return instance.getItem();
    }

    @ModifyReturnValue(method = "matches", at = @At(value = "RETURN", ordinal = 1), remap = false)
    public boolean gregtechCEu$matches(boolean b, @Local List<ItemStack> list) {
        if (!b) return false; // list size is not two

        ItemStack stack1 = list.get(0);
        ItemStack stack2 = list.get(1);

        if (!(stack1.getItem() instanceof IGTTool first) || stack1.getItem() instanceof ItemGTToolbelt)
            return false;

        // items must be the same at this point
        IGTTool second = (IGTTool) stack2.getItem();

        // must be same material
        if (first.getToolMaterial(stack1) != second.getToolMaterial(stack2))
            return false;

        // must not be electric
        if (first.isElectric() || second.isElectric())
            return false;

        // must share at least one tool class
        Set<String> firstClasses = first.getToolClasses(stack1);
        Set<String> secondClasses = second.getToolClasses(stack2);
        if (!firstClasses.isEmpty() && !secondClasses.isEmpty()) {
            for (String toolClass : first.getToolClasses(stack1)) {
                if (second.getToolClasses(stack2).contains(toolClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Inject(method = "getCraftingResult(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Ljava/util/List;get(I)Ljava/lang/Object;",
                     ordinal = 0),
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
