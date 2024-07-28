package gregtech.client.renderer.pipe.util;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CacheKey implements IStringSerializable {

    protected final float thickness;

    private final int hash;

    public CacheKey(float thickness) {
        this.thickness = thickness;
        this.hash = computeHash();
    }

    public float getThickness() {
        return thickness;
    }

    protected int computeHash() {
        return Objects.hash(thickness);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CacheKey) obj;
        return Float.floatToIntBits(this.thickness) == Float.floatToIntBits(that.thickness);
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public @NotNull String getName() {
        return String.valueOf(Float.floatToIntBits(thickness));
    }
}
