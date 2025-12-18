package gregtech.client.renderer.pipe.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class ActivableCacheKey extends CacheKey {

    private final boolean active;

    public ActivableCacheKey(float thickness, boolean active) {
        super(thickness);
        this.active = active;
    }

    public static ActivableCacheKey of(@Nullable Float thickness, @Nullable Boolean active) {
        float thick = thickness == null ? 0.5f : thickness;
        boolean act = active != null && active;
        return new ActivableCacheKey(thick, act);
    }

    public boolean isActive() {
        return active;
    }

    // activeness is merely a way to pass information onwards, it does not require a separate cache to be built.
    // thus we do not override equals() and hashCode() to account for the active field.
}
