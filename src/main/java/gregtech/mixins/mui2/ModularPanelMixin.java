package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModularPanel.class, remap = false)
public abstract class ModularPanelMixin extends ParentWidget<ModularPanel> implements IViewport {

    @Redirect(method = "lambda$onMousePressed$3",
              at = @At(value = "INVOKE",
                       target = "Lcom/cleanroommc/modularui/screen/viewport/LocatedWidget;getElement()Ljava/lang/Object;",
                       ordinal = 1))
    private Object checkDrag(LocatedWidget instance) {
        // if we're dragging something, prevent interaction
        if (ModularUIJeiPlugin.hasDraggingGhostIngredient()) {
            return null;
        }

        return instance.getElement();
    }
}
