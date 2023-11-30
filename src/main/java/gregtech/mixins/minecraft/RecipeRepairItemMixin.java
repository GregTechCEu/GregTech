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

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeRepairItem.class)
public class RecipeRepairItemMixin {

    @Inject(method = "matches(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z",
            at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;get(I)Ljava/lang/Object;"),
            cancellable = true)
    public void gregtechCEu$matches(InventoryCrafting inv, World worldIn, CallbackInfoReturnable<Boolean> cir,
                                    @Local LocalRef<ItemStack> itemstack, @Local LocalRef<ItemStack> itemstack1) {
        if (itemstack.get().getItem() instanceof IGTTool first &&
                itemstack1.get().getItem() instanceof IGTTool second) {
            if (first.isElectric() || second.isElectric()) {
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(first.getToolMaterial(itemstack.get()) == second.getToolMaterial(itemstack1.get()));
            }
        }
    }

    @Inject(method = "getCraftingResult(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0),
            cancellable = true)
    public void gregtechCEu$getCraftingResultFirst(InventoryCrafting inv, CallbackInfoReturnable<ItemStack> cir,
                                                   @Local(ordinal = 0) LocalRef<ItemStack> itemstack,
                                                   @Local(ordinal = 1) LocalRef<ItemStack> itemstack1) {
        if (itemstack.get().getItem() instanceof IGTTool tool && tool.isElectric()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else if (itemstack1.get().getItem() instanceof IGTTool tool && tool.isElectric()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "getCraftingResult(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true)
    public void gregtechCEu$getCraftingResultSecond(InventoryCrafting inv, CallbackInfoReturnable<ItemStack> cir,
                                                    @Local(ordinal = 4) LocalIntRef i1,
                                                    @Local(ordinal = 2) LocalRef<ItemStack> itemstack2,
                                                    @Local(ordinal = 3) LocalRef<ItemStack> itemstack3) {
        if (itemstack2.get().getItem() instanceof IGTTool first && itemstack3.get().getItem() instanceof IGTTool) {
            // do not allow repairing tools if both are full durability
            if (itemstack2.get().getItemDamage() == 0 && itemstack3.get().getItemDamage() == 0) {
                cir.setReturnValue(ItemStack.EMPTY);
            } else {
                ItemStack output = first.get(first.getToolMaterial(itemstack2.get()));
                NBTTagCompound outputTag = ToolHelper.getToolTag(output);
                outputTag.setInteger(ToolHelper.DURABILITY_KEY, i1.get());
                cir.setReturnValue(output);
            }
        }
    }

    // TODO, see if we can change this from an overwrite

    /**
     * @author GregTech CEu
     * @reason Properly account for GT Tools in Crafting Recipes
     */
    @Overwrite
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof IGTTool) {
                // bypass GT tools not disappearing in crafting recipes
                ForgeEventFactory.onPlayerDestroyItem(ForgeHooks.getCraftingPlayer(), stack, null);
            } else {
                list.set(i, ForgeHooks.getContainerItem(stack));
            }
        }
        return list;
    }
}
