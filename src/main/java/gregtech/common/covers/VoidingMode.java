package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import org.jetbrains.annotations.NotNull;

public enum VoidingMode implements ITranslatable {

    VOID_ANY("cover.voiding.voiding_mode.void_any", 1),
    VOID_OVERFLOW("cover.voiding.voiding_mode.void_overflow", Integer.MAX_VALUE);

    public static final VoidingMode[] VALUES = values();
    private final String localeName;
    private final int maxStackSize;

    VoidingMode(String localeName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
    }

    @NotNull
    @Override
    public String getName() {
        return localeName;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public boolean isVoidingAny() {
        return this == VOID_ANY;
    }

    public boolean isVoidingOverflow() {
        return this == VOID_OVERFLOW;
    }
}
