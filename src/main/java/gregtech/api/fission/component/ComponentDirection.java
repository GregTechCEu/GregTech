package gregtech.api.fission.component;

import org.jetbrains.annotations.NotNull;

public enum ComponentDirection {

    LEFT,
    RIGHT,
    UP,
    DOWN;

    public static final ComponentDirection[] VALUES = values();

    public int offsetX() {
        return switch (this) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP, DOWN -> 0;
        };
    }

    public int offsetY() {
        return switch (this) {
            case LEFT, RIGHT -> 0;
            case UP -> -1;
            case DOWN -> 1;
        };
    }

    public @NotNull ComponentDirection opposite() {
        return switch (this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case UP -> DOWN;
            case DOWN -> UP;
        };
    }
}
