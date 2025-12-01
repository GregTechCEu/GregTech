package gregtech.api.util;

import org.jetbrains.annotations.NotNull;

// java.awt.Rectangle uses ints and I need floats.
public class Rectangle {

    private float x;
    private float y;
    private float width;
    private float height;

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.width = size;
        this.height = size;
    }

    public Rectangle() {
        this(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        if (width < 0) {
            throw new IllegalArgumentException("Cannot set the width of a rectangle to a negative value!");
        }
        this.width = width;
    }

    public void setHeight(float height) {
        if (height < 0) {
            throw new IllegalArgumentException("Cannot set the height of a rectangle to a negative value!");
        }
        this.height = height;
    }

    public void incrementX(float val) {
        this.x += val;
    }

    public void incrementY(float val) {
        this.y += val;
    }

    public void incrementWidth(float val) {
        this.width = Math.max(0, this.width + val);
    }

    public void incrementHeight(float val) {
        this.height = Math.max(0, this.height + val);
    }

    public void decrementX(float val) {
        this.x -= val;
    }

    public void decrementY(float val) {
        this.y -= val;
    }

    public void decrementWidth(float val) {
        this.width = Math.max(0, this.width - val);
    }

    public void decrementHeight(float val) {
        this.height = Math.max(0, this.height - val);
    }

    /**
     * @param otherX      the X position of the other rectangle.
     * @param otherY      the Y position of the other rectangle.
     * @param otherWidth  the width of the other rectangle.
     * @param otherHeight the height of the other rectangle.
     * @param xOffset     apply an X position offset to this rectangle before testing.
     * @return true if the given rectangle would touch or intersect with this one.
     */
    public boolean collides(float otherX, float otherY, float otherWidth, float otherHeight, float xOffset) {
        if (otherWidth < 0 || otherHeight < 0) {
            throw new IllegalArgumentException("#collides called with a negative width or height!");
        }

        final float thisX = this.x + xOffset;
        return (thisX <= otherX + otherWidth) &&
                (thisX + this.width >= otherX) &&
                (this.y <= otherY + otherHeight) &&
                (this.y + this.height >= otherY);
    }

    /**
     * @param xOffset apply an X position offset to this rectangle before testing.
     * @return true if the given rectangle would touch or intersect with this one.
     */
    public boolean collides(@NotNull Rectangle other, float xOffset) {
        return collides(other.x, other.y, other.width, other.height, xOffset);
    }

    /**
     * @param other the rectangle to test against.
     * @return true if the given rectangle would touch or intersect with this one.
     */
    public boolean collides(@NotNull Rectangle other) {
        return collides(other, 0.0f);
    }
}
