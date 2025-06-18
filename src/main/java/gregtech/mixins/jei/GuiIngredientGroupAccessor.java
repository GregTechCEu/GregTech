package gregtech.mixins.jei;

import mezz.jei.gui.ingredients.GuiIngredientGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = GuiIngredientGroup.class, remap = false)
public interface GuiIngredientGroupAccessor {

    @Accessor(value = "inputSlots")
    Set<Integer> getInputSlotIndexes();
}
