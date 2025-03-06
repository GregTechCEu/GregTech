package gregtech.api.statemachine;

import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Predicate;

public class GTStateMachineBuilder {

    private final @NotNull GTStateMachine constructing;
    private final IntList pointerStack;

    public static GTStateMachineBuilder copy(GTStateMachineBuilder builder) {
        return new GTStateMachineBuilder(builder.getConstructing().copy(), new IntArrayList(builder.pointerStack));
    }

    public static GTStateMachineBuilder copy(GTStateMachine machine) {
        return new GTStateMachineBuilder(machine.copy());
    }

    public static GTStateMachineBuilder modify(GTStateMachine machine) {
        return new GTStateMachineBuilder(machine);
    }

    public GTStateMachineBuilder() {
        constructing = GTStateMachine.create();
        pointerStack = new IntArrayList();
    }

    private GTStateMachineBuilder(@NotNull GTStateMachine toModify) {
        constructing = toModify;
        pointerStack = new IntArrayList();
    }

    private GTStateMachineBuilder(@NotNull GTStateMachine toModify, IntList pointerStack) {
        constructing = toModify;
        this.pointerStack = pointerStack;
    }

    public @NotNull GTStateMachine getConstructing() {
        return constructing;
    }

    public GTStateMachineBuilder setPointer(@Range(from = 0, to = Integer.MAX_VALUE) int pointer) {
        if (pointer < constructing.operatorCount()) {
            this.pointerStack.add(pointer);
        }
        return this;
    }

    public int getPointer() {
        if (this.pointerStack.isEmpty()) {
            return -1;
        }
        return this.pointerStack.get(this.pointerStack.size() - 1);
    }

    public GTStateMachineBuilder movePointerBack() {
        if (!this.pointerStack.isEmpty()) {
            this.pointerStack.remove(this.pointerStack.size() - 1);
        }
        return this;
    }

    public GTStateMachineBuilder newOperator(GTStateMachineOperator operator, boolean async) {
        return setPointer(constructing.registerOperator(operator, async));
    }

    public GTStateMachineBuilder newOperatorTransient(GTStateMachineOperator operator, boolean async) {
        return setPointer(constructing.registerOperatorTransient(operator, async));
    }

    public GTStateMachineBuilder newOperator(GTStateMachineTransientOperator operator, boolean async) {
        return setPointer(constructing.registerOperatorTransient(operator, async));
    }

    public GTStateMachineBuilder andThenDefault(GTStateMachineOperator operator, boolean async) {
        int id = constructing.registerOperator(operator, async);
        constructing.modifyLink(getPointer(), l -> l.elseLink(id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenDefaultTransient(GTStateMachineOperator operator, boolean async) {
        int id = constructing.registerOperatorTransient(operator, async);
        constructing.modifyLink(getPointer(), l -> l.elseLink(id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenDefault(GTStateMachineTransientOperator operator, boolean async) {
        int id = constructing.registerOperatorTransient(operator, async);
        constructing.modifyLink(getPointer(), l -> l.elseLink(id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenIf(Predicate<NBTTagCompound> predicate, GTStateMachineOperator operator,
                                           boolean async) {
        int id = constructing.registerOperator(operator, async);
        constructing.modifyLink(getPointer(), l -> l.elseIf(predicate, id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenIfTransient(Predicate<NBTTagCompound> predicate,
                                                    GTStateMachineOperator operator,
                                                    boolean async) {
        int id = constructing.registerOperatorTransient(operator, async);
        constructing.modifyLink(getPointer(), l -> l.elseIf(predicate, id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenIf(Predicate<NBTTagCompound> predicate,
                                           GTStateMachineTransientOperator operator,
                                           boolean async) {
        int id = constructing.registerOperatorTransient(operator, async);
        constructing.modifyLink(getPointer(), l -> l.elseIf(predicate, id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenToDefault(int id) {
        constructing.modifyLink(getPointer(), l -> l.elseLink(id));
        return setPointer(id);
    }

    public GTStateMachineBuilder andThenToIf(Predicate<NBTTagCompound> predicate, int id) {
        constructing.modifyLink(getPointer(), l -> l.elseIf(predicate, id));
        return setPointer(id);
    }
}
