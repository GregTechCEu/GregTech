package gregtech.api.util;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.GTValues.CLIENT_TIME;
import static net.minecraft.util.text.TextFormatting.*;

// TODO Move to client?
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
            GTLog.logger.error("Could not create GT Format Code with codes {}, must have length greater than one!", Arrays.toString(codes));
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

    public static class GTFormatCode {

        private final int rate;
        private final TextFormatting[] codes;
        private int index = 0;

        private GTFormatCode(int rate, TextFormatting... codes) {
            this.rate = rate;
            this.codes = codes;
        }

        private void updateIndex() {
            if (CLIENT_TIME % rate == 0) {
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
