package gregtech.api.terminal.gui;

public interface IDraggable {
    boolean allowDrag(int mouseX, int mouseY, int button);
    default void startDrag(int mouseX, int mouseY) {}
    default boolean dragging(int mouseX, int mouseY, int deltaX, int deltaY) {return true;}
    default void endDrag(int mouseX, int mouseY) {}
}
