package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public enum TransferMode implements IStringSerializable {

    TRANSFER_ANY("cover.robotic_arm.transfer_mode.transfer_any", 1, 1),
    TRANSFER_EXACT("cover.robotic_arm.transfer_mode.transfer_exact", 1024, 163840),
    KEEP_EXACT("cover.robotic_arm.transfer_mode.keep_exact", Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final TransferMode[] VALUES = values();
    public final String localeName;
    public final int maxStackSize;
    public final int maxFluidStackSize;

    TransferMode(String localeName, int maxStackSize, int maxFluidStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
        this.maxFluidStackSize = maxFluidStackSize;
    }

    @NotNull
    @Override
    public String getName() {
        return localeName;
    }
}
