package gregtech.mixins.mui2;

import gregtech.api.util.Mods;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import com.cleanroommc.modularui.api.MCHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = MCHelper.class, remap = false)
public class MCHelperMixin {

    @ModifyReturnValue(method = "getItemToolTip", at = @At(value = "RETURN"))
    private static List<String> addModName(List<String> original, ItemStack stack) {
        if (original.isEmpty() || Mods.ModNameTooltip.isModLoaded()) return original;

        ModContainer modContainer = Loader.instance().getIndexedModList().get(stack.getItem().getCreatorModId(stack));
        if (modContainer != null) {
            original.add("§9§o" + modContainer.getName() + "§r");
        }

        return original;
    }
}
