package gregtech.api.util;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;

public class KeyUtil {

    public static final IKey RESET = toColor(TextFormatting.RESET);

    public static IKey toColor(TextFormatting formatting) {
        return IKey.str(formatting.toString());
    }

    public static IKey withColor(TextFormatting formatting, IKey... keys) {
        if (keys == null) return toColor(formatting);
        if (keys.length == 1) return IKey.comp(toColor(formatting), keys[0], RESET);
        return IKey.comp(toColor(formatting), IKey.comp(keys), RESET);
    }

    public static IKey coloredTranslation(TextFormatting formatting, String lang, Object... args) {
        if (args == null || args.length == 0) return withColor(formatting, IKey.lang(lang));
        Object[] fixedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            fixedArgs[i] = IKey.str(args[i].toString() + formatting);
        }
        return withColor(formatting, IKey.lang(lang, fixedArgs));
    }

    public static IKey coloredString(TextFormatting formatting, String string) {
        return IKey.comp(toColor(formatting), IKey.str(string), RESET);
    }

    public static IKey coloredNumber(TextFormatting formatting, long number) {
        return coloredString(formatting, TextFormattingUtil.formatNumbers(number));
    }

    public static IKey coloredNumber(TextFormatting formatting, long number, String suffix) {
        return coloredString(formatting, TextFormattingUtil.formatNumbers(number) + suffix);
    }

    public static IKey unformattedString(String s) {
        return coloredString(TextFormatting.RESET, s);
    }

    public static IKey unformattedLang(String lang, Object... args) {
        return coloredTranslation(TextFormatting.RESET, lang, args);
    }
}
