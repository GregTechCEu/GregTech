package gregtech.mixins.mui2;

import gregtech.api.mui.InputAccessor;
import gregtech.api.util.Mods;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.mixins.jei.DragManagerAccessor;
import gregtech.mixins.jei.GhostDragAccessor;

import net.minecraft.client.Minecraft;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.widget.ParentWidget;
import mezz.jei.gui.ghost.GhostIngredientDrag;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ModularPanel.class, remap = false)
public abstract class ModularPanelMixin extends ParentWidget<ModularPanel> implements IViewport {

    @Shadow
    @Final
    private ObjectList<LocatedWidget> hovering;

    @Shadow
    public abstract boolean closeOnOutOfBoundsClick();

    @Shadow
    public abstract void animateClose();

    @Shadow
    @Final
    private @NotNull String name;
    @Unique
    InputAccessor gregTech$mouse = null;

    /**
     * @author Ghzdude - GTCEu
     * @reason Implement fixes to phantom slot handling from Mui2 master
     */
    // this looks really cursed in mixin.out, but it works
    @Overwrite
    private boolean lambda$onMousePressed$3(int mouseButton) {
        LocatedWidget pressed = LocatedWidget.EMPTY;
        boolean result = false;

        if (gregTech$mouse == null) {
            // reflection because the type is a private inner class
            try {
                gregTech$mouse = (InputAccessor) ModularPanel.class.getDeclaredField("mouse").get(this);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.hovering.isEmpty()) {
            if (closeOnOutOfBoundsClick()) {
                animateClose();
                result = true;
            }
        } else {
            loop:
            for (LocatedWidget widget : this.hovering) {
                widget.applyMatrix(getContext());
                if (widget.getElement() instanceof JeiGhostIngredientSlot<?>ghostSlot &&
                        Mods.JustEnoughItems.isModLoaded()) {
                    GhostIngredientDrag<?> drag = gregTech$getGhostDrag();
                    if (drag != null && gregTech$insertGhostIngredient(drag, ghostSlot)) {
                        gregTech$stopDrag();
                        pressed = LocatedWidget.EMPTY;
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                }
                if (widget.getElement() instanceof Interactable interactable) {
                    switch (interactable.onMousePressed(mouseButton)) {
                        case IGNORE:
                            break;
                        case ACCEPT: {
                            if (!gregTech$mouse.held()) {
                                gregTech$mouse.addInteractable(interactable);
                            }
                            pressed = widget;
                            // result = false;
                            break;
                        }
                        case STOP: {
                            pressed = LocatedWidget.EMPTY;
                            result = true;
                            widget.unapplyMatrix(getContext());
                            break loop;
                        }
                        case SUCCESS: {
                            if (!gregTech$mouse.held()) {
                                gregTech$mouse.addInteractable(interactable);
                            }
                            pressed = widget;
                            result = true;
                            widget.unapplyMatrix(getContext());
                            break loop;
                        }
                    }
                }
                if (getContext().onHoveredClick(mouseButton, widget)) {
                    pressed = LocatedWidget.EMPTY;
                    result = true;
                    widget.unapplyMatrix(getContext());
                    break;
                }
                widget.unapplyMatrix(getContext());
                if (widget.getElement().canHover()) {
                    result = true;
                    break;
                }
            }
        }

        if (result && pressed.getElement() instanceof IFocusedWidget) {
            getContext().focus(pressed);
        } else {
            getContext().removeFocus();
        }
        if (!gregTech$mouse.held()) {
            gregTech$mouse.lastPressed(pressed);
            if (gregTech$mouse.lastPressed().getElement() != null) {
                gregTech$mouse.timeHeld(Minecraft.getSystemTime());
            }
            gregTech$mouse.lastButton(mouseButton);
            gregTech$mouse.held(true);
        }
        return result;
    }

    @Unique
    private static GhostIngredientDrag<?> gregTech$getGhostDrag() {
        GhostIngredientDragManager manager = ((DragManagerAccessor) JustEnoughItemsModule.jeiRuntime
                .getIngredientListOverlay()).getManager();
        return ((GhostDragAccessor) manager).getDrag();
    }

    @Unique
    @SuppressWarnings("rawtypes")
    private static boolean gregTech$insertGhostIngredient(GhostIngredientDrag drag,
                                                          JeiGhostIngredientSlot slot) {
        Object object = slot.castGhostIngredientIfValid(drag.getIngredient());
        if (object != null) {
            // noinspection unchecked
            slot.setGhostIngredient(object);
            return true;
        }
        return false;
    }

    @Unique
    private static void gregTech$stopDrag() {
        GhostIngredientDragManager manager = ((DragManagerAccessor) JustEnoughItemsModule.jeiRuntime
                .getIngredientListOverlay()).getManager();
        manager.stopDrag();
    }
}
