package gregtech.api.util;

import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Helper class to make working with {@link ITextComponent} implementations more readable.
 */
public class TextComponentUtil {

    /**
     * Create a {@link TextComponentString} with specified color format.
     */
    public static TextComponentString stringWithColor(TextFormatting color, String string) {
        return (TextComponentString) new TextComponentString(string).setStyle(new Style().setColor(color));
    }

    /**
     * Create a {@link TextComponentTranslation} with specified color format and args.
     */
    public static TextComponentTranslation translationWithColor(TextFormatting color, String key, Object... args) {
        return (TextComponentTranslation) new TextComponentTranslation(key, args).setStyle(new Style().setColor(color));
    }

    /**
     * Add hover text to a Text Component. Each hover component will be on a new line.
     */
    public static ITextComponent setHover(ITextComponent base, ITextComponent... hover) {
        if (hover == null || hover.length == 0) return base;

        ITextComponent hoverText = hover[0];
        if (hover.length > 1) {
            for (int i = 1; i < hover.length; i++) {
                hoverText.appendText("\n").appendSibling(hover[i]);
            }
        }

        // getStyle() always returns non-null, if there is no style it will create a new one.
        base.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        return base;
    }

    /**
     * Set the color of a Text Component.
     */
    public static ITextComponent setColor(ITextComponent base, TextFormatting color) {
        base.getStyle().setColor(color);
        return base;
    }
}
