package gregtech.mixins.minecraft;

import gregtech.api.util.GTUtility;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import net.minecraft.item.ItemStack;

import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static net.minecraft.util.text.TextFormatting.AQUA;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "addInformation", at = @At("HEAD"), remap = false)
    private void injectAddInformation(ItemStack stack, @NotNull World worldIn, List<String> tooltip, ITooltipFlag flagIn, CallbackInfo ci) {
        tooltip.add(AQUA + GTUtility.getTableBarNameByItemStack(stack));
    }

}
