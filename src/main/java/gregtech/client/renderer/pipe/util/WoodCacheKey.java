package gregtech.client.renderer.pipe.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WoodCacheKey extends CacheKey {

    private final boolean wood;

    public WoodCacheKey(float thickness, boolean wood) {
        super(thickness);
        this.wood = wood;
    }

    public boolean isWood() {
        return wood;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WoodCacheKey) obj;
        return Float.floatToIntBits(thickness) == Float.floatToIntBits(thickness) &&
                this.wood == that.wood;
    }

    @Override
    protected int computeHash() {
        return Objects.hash(thickness, wood);
    }

    @Override
    public @NotNull String getName() {
        return super.getName() + wood;
    }
}
