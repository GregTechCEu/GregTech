package gregtech.client.renderer.pipe.util;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class CacheKey implements IStringSerializable {

    protected final float thickness;

    private final int hash;

    public CacheKey(float thickness) {
        this.thickness = thickness;
        this.hash = computeHash();
    }

    public static CacheKey of(@Nullable Float thickness) {
        float thick = thickness == null ? 0.5f : thickness;
        return new CacheKey(thick);
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
