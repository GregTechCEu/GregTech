package gregtech.api.graphnet.logic;

import gregtech.api.util.TickUtil;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class RingBufferTransientLogicData<T extends AbstractTransientLogicData<T>, B>
                                                  extends AbstractTransientLogicData<T> {

    protected final Object[] ringBuffer;
    protected @Nullable Int2ObjectMap<B> mapView;
    protected int bufferIndex;
    protected int lastBufferUpdateTick;

    public RingBufferTransientLogicData(int bufferSize) {
        ringBuffer = new Object[bufferSize];
    }

    public @NotNull Int2ObjectMap<B> getMemory() {
        return getMemory(false);
    }

    public @NotNull Int2ObjectMap<B> getMemory(boolean reducedUpdate) {
        int tick = TickUtil.getTick();
        updateBuffer(tick, reducedUpdate);
        return getMapView(tick);
    }

    @NotNull
    protected Int2ObjectMap<B> getMapView(int tick) {
        if (mapView == null) {
            mapView = new Int2ObjectArrayMap<>(ringBuffer.length);
            for (int i = 0; i < ringBuffer.length; i++) {
                Object l = ringBuffer[wrapPointer(bufferIndex - i)];
                if (l != null) mapView.put(tick - i, (B) l);
            }
        }
        return mapView;
    }

    protected void setCurrent(B b) {
        ringBuffer[bufferIndex] = b;
    }

    protected @Nullable B getCurrent() {
        return (B) ringBuffer[bufferIndex];
    }

    protected @NotNull B getCurrentOrDefault(B fallback) {
        return ringBuffer[bufferIndex] == null ? fallback : (B) ringBuffer[bufferIndex];
    }

    protected @NotNull B computeCurrentIfAbsent(@NotNull Supplier<B> supplier) {
        if (ringBuffer[bufferIndex] == null) ringBuffer[bufferIndex] = supplier.get();
        return (B) ringBuffer[bufferIndex];
    }

    protected @Nullable B getAt(int pointer) {
        return (B) ringBuffer[wrapPointer(pointer)];
    }

    protected void updateBuffer(int tick, boolean reducedUpdate) {
        int update = tick - lastBufferUpdateTick;
        if (update == 0) return;
        if (reducedUpdate) {
            if (update == 1) return;
            update = update - 1;
        }
        lastBufferUpdateTick = tick;
        if (update < 0) {
            rotateBuffer(ringBuffer.length);
            bufferIndex = 0;
        } else {
            rotateBuffer(update);
        }
        invalidateViews();
    }

    protected void invalidateViews() {
        mapView = null;
    }

    protected void rotateBuffer(int rot) {
        if (rot > ringBuffer.length) rot = ringBuffer.length;
        for (int i = 0; i < rot; i++) {
            bufferIndex = wrapPointer(bufferIndex + 1);
            if (ringBuffer[bufferIndex] != null) dropEntry((B) ringBuffer[bufferIndex]);
            ringBuffer[bufferIndex] = null;
        }
    }

    protected void dropEntry(B entry) {}

    protected int wrapPointer(int pointer) {
        pointer = pointer % ringBuffer.length;
        return pointer < 0 ? pointer + ringBuffer.length : pointer;
    }
}
