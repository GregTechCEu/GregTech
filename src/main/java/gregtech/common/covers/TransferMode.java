package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum TransferMode implements IStringSerializable {
    TRANSFER_ANY("cover.robotic_arm.transfer_mode.transfer_any", 1),
    TRANSFER_EXACT("cover.robotic_arm.transfer_mode.transfer_exact", 1024),
    KEEP_EXACT("cover.robotic_arm.transfer_mode.keep_exact", 1024);

    public final String localeName;
    public final int maxStackSize;

    TransferMode(String localeName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
    }


    @Nonnull
    @Override
    public String getName() {
        return localeName;
    }
}

