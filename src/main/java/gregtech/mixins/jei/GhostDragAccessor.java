package gregtech.mixins.jei;

import mezz.jei.gui.ghost.GhostIngredientDrag;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// todo remove on next mui2 update
@Mixin(value = GhostIngredientDragManager.class, remap = false)
public interface GhostDragAccessor {

    @Accessor("ghostIngredientDrag")
    GhostIngredientDrag<?> getDrag();
}
