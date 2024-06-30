package gregtech.mixins.jei;

import gregtech.api.items.toolitem.ToolHelper;

import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = BasicRecipeTransferHandlerServer.class, remap = false)
public class BasicRecipeTransferHandlerServerMixin {

    /**
     * @reason create the list used to indicate what slots contain tools, and thus should be ignored on the 2nd+ checks
     */
    @Inject(method = "removeItemsFromInventory", at = @At("HEAD"))
    private static void gregtechCEu$initCompletedList(CallbackInfoReturnable<Map<Integer, ItemStack>> cir,
                                                      @Share("completed") LocalRef<IntArrayList> completed) {
        completed.set(new IntArrayList());
    }

    /**
     * @reason add the index of the entry to the list of skipped locations on the 2nd+ checks
     */
    @Inject(method = "removeItemsFromInventory",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                     ordinal = 1))
    private static void gregtechCEu$markItemStackAsCompleted(CallbackInfoReturnable<Map<Integer, ItemStack>> cir,
                                                             @Local(ordinal = 0) Map.Entry<Integer, ItemStack> entry,
                                                             @Local(ordinal = 2) ItemStack removedItemStack,
                                                             @Share("completed") LocalRef<IntArrayList> completed) {
        // mark tool stacks as 'permanently' satisfied
        if (removedItemStack != null && ToolHelper.isTool(removedItemStack)) completed.get().add(entry.getKey());
    }

    /**
     * @reason if the given location is marked as completed, skip processing this slot and check the next slot
     */
    @ModifyExpressionValue(method = "removeItemsFromInventory",
                           at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private static boolean gregtechCEu$skipCompletedStacks(boolean original,
                                                           @Local(ordinal = 0) Iterator<Map.Entry<Integer, ItemStack>> iterator,
                                                           @Share("completed") LocalRef<IntArrayList> completed,
                                                           @Share("iterator_next") LocalRef<Map.Entry<Integer, ItemStack>> next) {
        if (!original) return false;
        next.set(iterator.next());
        while (completed.get().contains(next.get().getKey())) {
            if (!iterator.hasNext()) return false;
            next.set(iterator.next());
        }
        return true;
    }

    /**
     * @reason use the next variable that respects the completed status
     */
    @WrapOperation(method = "removeItemsFromInventory",
                   at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0))
    private static Object gregtechCEu$useCorrectIteratorNext(Iterator<Map.Entry<Integer, ItemStack>> instance,
                                                             Operation<Map.Entry<Integer, ItemStack>> original,
                                                             @Share("completed") LocalRef<IntArrayList> completed,
                                                             @Share("iterator_next") LocalRef<Map.Entry<Integer, ItemStack>> next) {
        return next.get() == null ? original.call(instance) : next.get();
    }
}
