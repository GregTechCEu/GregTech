package gregtech.client.renderer.pipe.util;

public class ActivableCacheKey extends CacheKey {

    private final boolean active;

    public ActivableCacheKey(float thickness, boolean active) {
        super(thickness);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    // activeness is merely a way to pass information onwards, it does not result in separate mappings.
}
