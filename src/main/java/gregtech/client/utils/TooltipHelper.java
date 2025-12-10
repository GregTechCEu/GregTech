package gregtech.client.utils;

import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTLog;
import gregtech.api.util.KeyUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.items.ToolItems;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.GTValues.CLIENT_TIME;
import static net.minecraft.util.text.TextFormatting.*;

public class TooltipHelper {

    private static final List<GTFormatCode> CODES = new ArrayList<>();

    /* Array of TextFormatting codes that are used to create a rainbow effect */
    private static final TextFormatting[] ALL_COLORS = new TextFormatting[] {
            RED, GOLD, YELLOW, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, DARK_PURPLE, LIGHT_PURPLE
    };

    /** Oscillates through all colors, changing each tick */
    public static final GTFormatCode RAINBOW_FAST = createNewCode(1, ALL_COLORS);
    /** Oscillates through all colors, changing every 5 ticks */
    public static final GTFormatCode RAINBOW = createNewCode(5, ALL_COLORS);
    /** Oscillates through all colors, changing every 25 ticks */
    public static final GTFormatCode RAINBOW_SLOW = createNewCode(25, ALL_COLORS);
    /** Switches between AQUA and WHITE, changing every 5 ticks */
    public static final GTFormatCode BLINKING_CYAN = createNewCode(5, AQUA, WHITE);
    /** Switches between RED and WHITE, changing every 5 ticks */
    public static final GTFormatCode BLINKING_RED = createNewCode(5, RED, WHITE);
    /** Switches between GOLD and YELLOW, changing every 25 ticks */
    public static final GTFormatCode BLINKING_ORANGE = createNewCode(25, GOLD, YELLOW);
    /** Switches between GRAY and DARK_GRAY, changing every 25 ticks */
    public static final GTFormatCode BLINKING_GRAY = createNewCode(25, GRAY, DARK_GRAY);

    /**
     * Creates a Formatting Code which can oscillate through a number of different formatting codes at a specified rate.
     *
     * @param rate  The number of ticks this should wait before changing to the next code. MUST be greater than zero.
     * @param codes The codes, in order, that this formatting code should oscillate through. MUST be at least 2.
     */
    public static GTFormatCode createNewCode(int rate, TextFormatting... codes) {
        if (rate <= 0) {
            GTLog.logger.error("Could not create GT Format Code with rate {}, must be greater than zero!", rate);
            return null;
        }
        if (codes == null || codes.length <= 1) {
            GTLog.logger.error("Could not create GT Format Code with codes {}, must have length greater than one!",
                    Arrays.toString(codes));
            return null;
        }
        GTFormatCode code = new GTFormatCode(rate, codes);
        CODES.add(code);
        return code;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CODES.forEach(GTFormatCode::updateIndex);
        }
    }

    public static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static void addMaintenanceProblems(@NotNull IRichTextBuilder<?> richText, byte maintenanceProblems) {
        if (maintenanceProblems >= IMaintenance.NO_PROBLEMS || maintenanceProblems < 0) return;

        richText.addLine(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.has_problems"))
                .spaceLine(2);

        // Wrench
        if ((maintenanceProblems & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.WRENCH.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.problem.wrench"))
                    .newLine();
        }

        // Screwdriver
        if (((maintenanceProblems >> 1) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.SCREWDRIVER.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.problem.screwdriver"))
                    .newLine();
        }

        // Soft Mallet
        if (((maintenanceProblems >> 2) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.SOFT_MALLET.get(Materials.Wood)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.problem.soft_mallet"))
                    .newLine();
        }

        // Hammer
        if (((maintenanceProblems >> 3) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.HARD_HAMMER.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.problem.hard_hammer"))
                    .newLine();
        }

        // Wire Cutters
        if (((maintenanceProblems >> 4) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.WIRE_CUTTER.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.problem.wire_cutter"))
                    .newLine();
        }

        // Crowbar
        if (((maintenanceProblems >> 5) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.CROWBAR.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(YELLOW, "gregtech.multiblock.universal.problem.crowbar"))
                    .newLine();
        }
    }

    public static class GTFormatCode {

        private final int rate;
        private final TextFormatting[] codes;
        private int index = 0;

        private GTFormatCode(int rate, TextFormatting... codes) {
            this.rate = rate;
            this.codes = codes;
        }

        private void updateIndex() {
            if (CLIENT_TIME % rate == 0 && !ConfigHolder.client.preventBlinkingTooltips) {
                if (index + 1 >= codes.length) index = 0;
                else index++;
            }
        }

        @Override
        public String toString() {
            return codes[index].toString();
        }
    }
}
