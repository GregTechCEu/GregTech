package gregtech.api.statemachine;

import org.jetbrains.annotations.NotNull;

final class OperatorHolder {

    private final @NotNull Object operator;
    private final boolean isTransient;
    private final boolean asyncCompatible;
    private @NotNull GTStateMachineLink link = GTStateMachineLink.UNKNOWN_LINK;

    public OperatorHolder(@NotNull GTStateMachineOperator operator, boolean asyncCompatible) {
        this.operator = operator;
        this.asyncCompatible = asyncCompatible;
        this.isTransient = false;
    }

    public OperatorHolder(@NotNull GTStateMachineTransientOperator operator, boolean asyncCompatible) {
        this.operator = operator;
        this.asyncCompatible = asyncCompatible;
        this.isTransient = true;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public boolean isAsyncCompatible() {
        return asyncCompatible;
    }

    public void setLink(@NotNull GTStateMachineLink link) {
        this.link = link;
    }

    public @NotNull GTStateMachineLink getLink() {
        return link;
    }

    public @NotNull GTStateMachineOperator getOperator() {
        return (GTStateMachineOperator) operator;
    }

    public @NotNull GTStateMachineTransientOperator getOperatorTransient() {
        return (GTStateMachineTransientOperator) operator;
    }

    public @NotNull OperatorHolder copy() {
        OperatorHolder holder;
        if (isTransient()) {
            holder = new OperatorHolder(getOperatorTransient(), asyncCompatible);
        } else {
            holder = new OperatorHolder(getOperator(), asyncCompatible);
        }
        holder.setLink(getLink());
        return holder;
    }
}
