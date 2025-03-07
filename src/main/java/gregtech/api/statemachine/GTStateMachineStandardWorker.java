package gregtech.api.statemachine;

import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GTStateMachineStandardWorker implements INBTSerializable<NBTTagCompound> {

    protected final GTStateMachine machine;

    private static final NBTTagCompound EMPTY = new NBTTagCompound();
    protected final NBTTagCompound defaultTag;
    protected boolean logicEnabled = true;
    protected NBTTagCompound logicData = new NBTTagCompound();
    protected int logicPosition;
    protected NBTTagCompound logicDataSafe = new NBTTagCompound();
    protected int logicPositionSafe;
    protected final Map<String, Object> logicTransientData = new Object2ObjectOpenHashMap<>();

    protected @Nullable CompletableFuture<GTSMWalkCompletionData> trackedFuture;

    protected final List<Consumer<GTStateMachineStandardWorker>> changeListeners = new ObjectArrayList<>();

    public GTStateMachineStandardWorker(GTStateMachine machine) {
        this(machine, EMPTY);
    }

    public GTStateMachineStandardWorker(GTStateMachine machine, NBTTagCompound defaultTag) {
        this.machine = machine;
        this.defaultTag = defaultTag;
    }

    public void clear() {
        abortAsyncWalk();
        logicData = defaultTag.copy();
        logicDataSafe = defaultTag.copy();
        logicPosition = -1;
        logicPositionSafe = -1;
        logicTransientData.clear();
        pingChangeListeners();
    }

    public void walk(boolean stopOnAsync) {
        if (!hasAsyncWalkCompleted()) {
            GTLog.logger.warn("Attempted to walk a GTStateMachineStandardWorker " +
                    "before its dispatched async worker completed.");
            return;
        }
        handleCompletedWalk(machine.walk(logicPosition, logicData, logicTransientData, stopOnAsync));
    }

    public void dispatchAsyncWalk() {
        if (!hasAsyncWalkCompleted()) {
            GTLog.logger.warn("Attempted to dispatch a second async worker for a GTStateMachineStandardWorker " +
                    "before its dispatched async worker completed.");
            return;
        }
        trackedFuture = machine.dispatchAsync(logicPosition, logicData, logicTransientData);
    }

    public void abortAsyncWalk() {
        if (trackedFuture != null) {
            trackedFuture.cancel(true);
            trackedFuture = null;
        }
    }

    public boolean hasAsyncWalkCompleted() {
        if (trackedFuture == null) return true;
        if (trackedFuture.isDone()) {
            handleCompletedWalk(trackedFuture.join());
            trackedFuture = null;
            return true;
        }
        return false;
    }

    protected void handleCompletedWalk(GTSMWalkCompletionData completionData) {
        logicPosition = completionData.nextOpID();
        if (completionData.serializableTag() != null) {
            logicPositionSafe = completionData.nextOpAfterLastSerializable();
            logicDataSafe = completionData.serializableTag();
            pingChangeListeners();
        }
    }

    public void registerChangeListener(Consumer<GTStateMachineStandardWorker> listener) {
        changeListeners.add(listener);
    }

    protected void pingChangeListeners() {
        changeListeners.forEach(l -> l.accept(this));
    }

    public boolean isLogicEnabled() {
        return logicEnabled;
    }

    public void setLogicEnabled(boolean logicEnabled) {
        if (logicEnabled != this.logicEnabled) {
            this.logicEnabled = logicEnabled;
            pingChangeListeners();
        }
    }

    public NBTTagCompound logicData() {
        return logicDataSafe;
    }

    public int logicPosition() {
        return logicPositionSafe;
    }

    public void setPosition(int logicPosition) {
        if (!hasAsyncWalkCompleted()) {
            GTLog.logger.warn("Attempted to change the position of a GTStateMachineStandardWorker " +
                    "before its dispatched async worker completed.");
            return;
        }
        this.logicPosition = logicPosition;
        this.logicPositionSafe = logicPosition;
        this.logicDataSafe = this.logicData;
        this.logicTransientData.clear();
        pingChangeListeners();
    }

    public Map<String, Object> getLogicTransientData() {
        return logicTransientData;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        // check in on whether our async walker is done, and we can serialize its results too.
        hasAsyncWalkCompleted();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Enabled", logicEnabled);
        tag.setTag("Data", logicDataSafe);
        tag.setInteger("Position", logicPositionSafe);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        abortAsyncWalk();
        logicTransientData.clear();
        logicData = nbt.hasKey("Data") ? nbt.getCompoundTag("Data") : defaultTag.copy();
        logicPosition = nbt.hasKey("Position") ? nbt.getInteger("Position") : -1;
        logicEnabled = nbt.getBoolean("Enabled");
        logicDataSafe = logicData.copy();
        logicPositionSafe = logicPosition;
        pingChangeListeners();
    }
}
