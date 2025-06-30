package gregtech.mixins.mui2;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.ItemSlot;
import mezz.jei.Internal;
import mezz.jei.gui.ghost.GhostIngredientDrag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: remove once MUI PR 146 merges into a release we use
@Debug(export = true)
@Mixin(value = ItemSlot.class)
public abstract class ItemSlotMixin {

    @Shadow(remap = false)
    private ItemSlotSH syncHandler;

    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Lcom/cleanroommc/modularui/integration/jei/ModularUIJeiPlugin;hasDraggingGhostIngredient()Z"),
              remap = false)
    private boolean onlyHighlightOnValidDrag() {
        GhostIngredientDrag<?> ingredientDrag = ModularUIJeiPlugin.getGhostDrag();
        if (ingredientDrag == null) return false;
        Object ingredient = ingredientDrag.getIngredient();
        if (ingredient == null) return false;
        return gregTech$castIfItemStack(ingredient) != null;
    }

    /**
     * @author Zorbatron
     * @reason Add support for dragging enchanted books from JEI
     */
    @Overwrite(remap = false)
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return gregTech$castIfItemStack(ingredient);
    }

    @Unique
    private @Nullable ItemStack gregTech$castIfItemStack(Object ingredient) {
        if (ingredient instanceof EnchantmentData enchantmentData) {
            ingredient = Internal.getIngredientRegistry().getIngredientHelper(enchantmentData)
                    .getCheatItemStack(enchantmentData);
        }

        if (ingredient instanceof ItemStack itemStack) {
            return syncHandler.isItemValid(itemStack) ? itemStack : null;
        }

        return null;
    }
}
