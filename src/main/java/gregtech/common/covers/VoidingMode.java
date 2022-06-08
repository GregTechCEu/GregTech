package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum VoidingMode implements IStringSerializable {
    VOID_ANY("cover.voiding.voiding_mode.void_any", 1),
    VOID_OVERFLOW("cover.voiding.voiding_mode.void_overflow", 1024);

    public final String localeName;
    public final int maxStackSize;

    VoidingMode(String localeName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
    }


    @Nonnull
    @Override
    public String getName() {
        return localeName;
    }
}
