package gregtech.api.util;

import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Helper class to make working with {@link ITextComponent} implementations more readable.
 */
public class TextComponentUtil {

    public static TextComponentString stringWithColor(TextFormatting color, String string) {
        return (TextComponentString) new TextComponentString(string).setStyle(new Style().setColor(color));
    }

    public static TextComponentTranslation translationWithColor(TextFormatting color, String key, Object... args) {
        return (TextComponentTranslation) new TextComponentTranslation(key, args).setStyle(new Style().setColor(color));
    }

    public static ITextComponent setHover(ITextComponent base, ITextComponent hover) {
        // getStyle() always returns non-null, if there is no style it will create a new one.
        base.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        return base;
    }

    public static ITextComponent setColor(ITextComponent base, TextFormatting color) {
        base.getStyle().setColor(color);
        return base;
    }
}
