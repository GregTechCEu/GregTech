package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.api.drawable.IDrawable;

@FunctionalInterface
public interface KeyManager {

    void add(Operation op);

    default void add(IDrawable drawable) {
        add(Operation.addLineSpace(drawable));
    }

    default void addAll(Iterable<? extends IDrawable> drawables) {
        drawables.forEach(this::add);
    }
}
