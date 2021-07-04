package gregtech.common.covers;

import net.minecraft.util.IStringSerializable;

public enum TransferMode implements IStringSerializable {
        TRANSFER_ANY("cover.robotic_arm.transfer_mode.transfer_any", Integer.MAX_VALUE),
        TRANSFER_EXACT("cover.robotic_arm.transfer_mode.transfer_exact", 64),
        KEEP_EXACT("cover.robotic_arm.transfer_mode.keep_exact", 1024);

        public final String localeName;
        public final int maxStackSize;

        TransferMode(String localeName, int maxStackSize) {
            this.localeName = localeName;
            this.maxStackSize = maxStackSize;
        }


        @Override
        public String getName() {
            return localeName;
        }
}

