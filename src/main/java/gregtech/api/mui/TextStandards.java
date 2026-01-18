package gregtech.api.mui;

import gregtech.api.util.KeyUtil;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;

public final class TextStandards {

    public static class Colors {

        public static final TextFormatting MACHINE_WORKING = TextFormatting.GREEN;
        public static final TextFormatting MACHINE_DONE = TextFormatting.GREEN;
        public static final TextFormatting MACHINE_PAUSED = TextFormatting.GOLD;
        public static final TextFormatting NO_OUTPUT_SPACE = TextFormatting.RED;
        public static final TextFormatting STEAM_VENT_BLOCKED = TextFormatting.RED;
        public static final TextFormatting NO_POWER = TextFormatting.RED;
    }

    public static class Keys {

        public static final IKey MACHINE_PAUSED = KeyUtil.lang(Colors.MACHINE_PAUSED,
                "gregtech.multiblock.work_paused");
    }
}
