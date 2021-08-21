package gregtech.api.util;

import net.minecraft.world.World;

import java.util.Optional;

public class TickingObjectHolder<T> {

    private T object;
    private long lastUpdateTime;
    private final long defaultLifeTime;
    private long lifeTime;

    public TickingObjectHolder(T t, long lifeTime) {
        this.object = t;
        this.defaultLifeTime = lifeTime;
        this.lifeTime = lifeTime;
    }

    private void checkState(World world) {
        if(lifeTime <= 0) return;
        long worldTime = world.getTotalWorldTime();
        if(lastUpdateTime != worldTime) {
            lifeTime -= (worldTime - lastUpdateTime);
            if(lifeTime <= 0)
                object = null;
            lastUpdateTime = worldTime;
        }
    }

    public Optional<T> get(World world) {
        checkState(world);
        return object == null ? Optional.empty() : Optional.of(object);
    }

    public T getNullable(World world) {
        checkState(world);
        return object;
    }

    public long getRemainingTime(World world) {
        checkState(world);
        return Math.max(0, lifeTime);
    }

    public void reset(T object, long lifeTime) {
        this.object = object;
        this.lifeTime = lifeTime;
    }

    public void reset(T object) {
        this.object = object;
        this.lifeTime = defaultLifeTime;
    }
}
