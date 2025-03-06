package gregtech.api.statemachine;

import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

public final class GTStateMachine {

    private static final Executor executor = Executors.newWorkStealingPool(4);

    private final List<OperatorHolder> operators;

    private GTStateMachine(int c) {
        operators = new ObjectArrayList<>(c);
    }

    public static GTStateMachine create() {
        return new GTStateMachine(8);
    }

    public int operatorCount() {
        return operators.size();
    }

    public int registerOperator(GTStateMachineOperator operator, boolean asyncCompatible) {
        operators.add(new OperatorHolder(operator, asyncCompatible));
        return operators.size() - 1;
    }

    public int registerOperatorTransient(GTStateMachineOperator operator, boolean asyncCompatible) {
        return registerOperatorTransient(GTStateMachineTransientOperator.of(operator), asyncCompatible);
    }

    public int registerOperatorTransient(GTStateMachineTransientOperator operator, boolean asyncCompatible) {
        operators.add(new OperatorHolder(operator, asyncCompatible));
        return operators.size() - 1;
    }

    public boolean isOperatorTransient(int operatorID) {
        if (operatorID < 0) return false;
        return operators.get(operatorID).isTransient();
    }

    public boolean isAsyncCompatible(int operatorID) {
        if (operatorID < 0) return false;
        return operators.get(operatorID).isAsyncCompatible();
    }

    /**
     * Sets the link of the state machine operator at the specified ID.
     * 
     * @param operatorID the id of the operator whose link should be overwritten.
     * @param link       the link the operator's link should be set to.
     */
    public void setLink(int operatorID, GTStateMachineLink link) {
        operators.get(operatorID).setLink(link);
    }

    /**
     * Gets the link of the state machine operator at the specified ID.
     * 
     * @param operatorID the id of the operator whose link should be returned.
     * @return the link of the specified operator.
     */
    public GTStateMachineLink getLink(int operatorID) {
        return operators.get(operatorID).getLink();
    }

    /**
     * Modifies the link of the state machine operator at the specified ID.
     * 
     * @param operatorID the id of the operator whose link should be modified.
     * @param link       the modification operator for the operator's link.
     */
    public void modifyLink(int operatorID, UnaryOperator<GTStateMachineLink> link) {
        OperatorHolder holder = operators.get(operatorID);
        holder.setLink(link.apply(holder.getLink()));
    }

    /**
     * Operates on the state machine operator at the specified ID
     * 
     * @param operatorID    the id of the operator to operate.
     * @param data          the data to provide to the operator. May be mutated.
     * @param transientData the transient data to provide to the operator, if it is transient.
     *                      If it is not transient, this will be cleared.
     * @return the id of the next operator to operate. -1 is a special case of not knowing what next to operate.
     */
    public int operate(int operatorID, NBTTagCompound data, Map<String, Object> transientData) {
        if (operatorID < 0) return -1;
        OperatorHolder holder = operators.get(operatorID);
        if (holder.isTransient()) {
            holder.getOperatorTransient().operate(data, transientData);
        } else {
            holder.getOperator().operate(data);
            transientData.clear();
        }
        return holder.getLink().getLink(data);
    }

    /**
     * Moves along the state machine until the next state is unknown or it hits an async operator, if stopOnAsync is
     * true.
     * 
     * @param operatorID    the operator ID to start at.
     * @param data          the data for the walk.
     * @param transientData the transient data for the walk.
     * @param stopOnAsync   whether the walk should terminate before starting an async-compatible operation.
     * @return completion data associated with the walk.
     */
    public @NotNull GTSMWalkCompletionData walk(int operatorID, NBTTagCompound data, Map<String, Object> transientData,
                                                boolean stopOnAsync) {
        return walk(operatorID, data, transientData, 100, stopOnAsync);
    }

    /**
     * Moves along the state machine until the next state is unknown or it hits an async operator, if stopOnAsync is
     * true.
     * 
     * @param operatorID    the operator ID to start at.
     * @param data          the data for the walk.
     * @param transientData the transient data for the walk.
     * @param stepLimit     the maximum number of steps the walk can execute before exiting.
     * @param stopOnAsync   whether the walk should terminate before starting an async-compatible operation.
     * @return completion data associated with the walk.
     */
    public @NotNull GTSMWalkCompletionData walk(int operatorID, NBTTagCompound data, Map<String, Object> transientData,
                                                int stepLimit, boolean stopOnAsync) {
        if (operatorID < 0 || (stopOnAsync && isAsyncCompatible(operatorID))) {
            return new GTSMWalkCompletionData(operatorID, data, transientData, -2, null);
        }
        int nextAfterlastSerializable = -2;
        NBTTagCompound lastSerializableNBT = null;
        int count = 0;
        while (count < stepLimit && !(operatorID < 0 || stopOnAsync && isAsyncCompatible(operatorID))) {
            int next = operate(operatorID, data, transientData);
            if (!isOperatorTransient(operatorID) || next < 0) {
                transientData.clear();
                nextAfterlastSerializable = next;
                lastSerializableNBT = data.copy();
            }
            operatorID = next;
            count++;
        }
        return new GTSMWalkCompletionData(operatorID, data, transientData, nextAfterlastSerializable,
                lastSerializableNBT);
    }

    /**
     * Dispatches an offthread worker that progresses along the state machine until it hits an operator that is not
     * async compatible.
     * 
     * @param operatorID    the operator ID to start at.
     * @param data          the data for the offthread execution. Should not be interacted with until the worker exits!
     * @param transientData the transient data for the offthread execution. Should not be interacted with until the
     *                      worker exits!
     * @return a completable future associated with the worker. When complete, information related to the work will be
     *         provided.
     */
    public @NotNull CompletableFuture<GTSMWalkCompletionData> dispatchAsync(int operatorID, NBTTagCompound data,
                                                                            Map<String, Object> transientData) {
        return dispatchAsync(operatorID, data, transientData, 100);
    }

    /**
     * Dispatches an offthread worker that progresses along the state machine until it hits an operator that is not
     * async compatible.
     * 
     * @param operatorID    the operator ID to start at.
     * @param data          the data for the offthread execution. Should not be interacted with until the worker exits!
     * @param transientData the transient data for the offthread execution. Should not be interacted with until the
     *                      worker exits!
     * @param stepLimit     the maximum number of steps the worker can execute before exiting.
     * @return a completable future associated with the worker. When complete, information related to the work will be
     *         provided.
     */
    public @NotNull CompletableFuture<GTSMWalkCompletionData> dispatchAsync(int operatorID, NBTTagCompound data,
                                                                            Map<String, Object> transientData,
                                                                            int stepLimit) {
        if (!isAsyncCompatible(operatorID)) {
            return CompletableFuture
                    .completedFuture(new GTSMWalkCompletionData(operatorID, data, transientData, -2, null));
        }
        return CompletableFuture.supplyAsync(() -> {
            int operator = operatorID;
            int nextAfterlastSerializable = -2;
            NBTTagCompound lastSerializableNBT = null;
            int count = 0;
            while (count < stepLimit && isAsyncCompatible(operator)) {
                int next = operate(operator, data, transientData);
                if (!isOperatorTransient(operator) || next < 0) {
                    transientData.clear();
                    nextAfterlastSerializable = next;
                    lastSerializableNBT = data.copy();
                }
                operator = next;
                count++;
            }
            return new GTSMWalkCompletionData(operator, data, transientData, nextAfterlastSerializable,
                    lastSerializableNBT);
        }, executor);
    }

    public @NotNull GTStateMachine copy() {
        GTStateMachine create = new GTStateMachine(this.operatorCount());
        for (OperatorHolder holder : this.operators) {
            create.operators.add(holder.copy());
        }
        return create;
    }
}
