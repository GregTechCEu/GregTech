package gregtech.api.util;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class PositionedRect {

    public final Position position;
    public final Size size;

    public PositionedRect(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    public PositionedRect(Position position, Size size) {
        this.position = position;
        this.size = size;
    }

    public PositionedRect(Position pos1, Position pos2) {
        this.position = new Position(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y));
        this.size = new Size(Math.max(pos1.x, pos2.x) - position.x, Math.max(pos1.y, pos2.y) - position.y);
    }

    public Position getPosition() {
        return position;
    }

    public Size getSize() {
        return size;
    }

    public boolean intersects(Position other) {
        return position.x <= other.x &&
                position.y <= other.y &&
                position.x + size.width >= other.x &&
                position.y + size.height >= other.y;
    }

    public boolean intersects(PositionedRect other) {
        return intersects(other.position) ||
                intersects(other.position.add(other.size)) ||
                intersects(other.position.add(new Size(other.size.width, 0))) ||
                intersects(other.position.add(new Size(0, other.size.height)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionedRect)) return false;
        PositionedRect that = (PositionedRect) o;
        return position.equals(that.position) &&
                size.equals(that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, size);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("position", position)
                .add("size", size)
                .toString();
    }
}
