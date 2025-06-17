package gregtech.mixins.jei;

import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.overlay.IngredientListOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// todo remove on next mui2 update
@Mixin(value = IngredientListOverlay.class, remap = false)
public interface DragManagerAccessor {

    @Accessor("ghostIngredientDragManager")
    GhostIngredientDragManager getManager();
}
