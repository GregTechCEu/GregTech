package gregtech.mixins.mui2;

import gregtech.api.util.JEIUtil;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mezz.jei.gui.ghost.GhostIngredientDrag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: remove once MUI PR 146 merges into a release we use
@Mixin(value = ItemSlot.class, remap = false)
public abstract class ItemSlotMixin {

    @Shadow
    public abstract @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient);

    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Lcom/cleanroommc/modularui/integration/jei/ModularUIJeiPlugin;hasDraggingGhostIngredient()Z"))
    private boolean onlyHighlightOnValidDrag() {
        GhostIngredientDrag<?> ingredientDrag = ModularUIJeiPlugin.getGhostDrag();
        if (ingredientDrag == null) return false;
        Object ingredient = ingredientDrag.getIngredient();
        if (ingredient == null) return false;
        return castGhostIngredientIfValid(ingredient) != null;
    }

    @WrapMethod(method = "castGhostIngredientIfValid(Ljava/lang/Object;)Lnet/minecraft/item/ItemStack;")
    public @Nullable ItemStack addSupportForEnchantedBooks(Object ingredient, Operation<ItemStack> original) {
        return original.call(JEIUtil.getBookStackIfEnchantment(ingredient));
    }
}
