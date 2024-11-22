package gregtech.api.util;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class KeyUtil {

    public static final IKey RESET = toColor(TextFormatting.RESET);
    public static final String SECTION = "§";

    public static IKey toColor(TextFormatting formatting) {
        return IKey.str(formatting.toString());
    }

    public static IKey withColor(TextFormatting formatting, IKey... keys) {
        if (keys == null) return toColor(formatting);
        if (keys.length == 1) return IKey.comp(toColor(formatting), keys[0], RESET);
        return IKey.comp(toColor(formatting), IKey.comp(keys), RESET);
    }

    public static IKey coloredLang(TextFormatting formatting, String lang, Object... args) {
        if (ArrayUtils.isEmpty(args)) return withColor(formatting, IKey.lang(lang));
        return withColor(formatting, IKey.lang(lang, checkFormatting(formatting, args)));
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
        return coloredLang(TextFormatting.RESET, lang, args);
    }

    public static IKey dynamicString(TextFormatting formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(() -> coloredString(formatting, stringSupplier.get()).get());
    }

    public static IKey dynamicString(Supplier<TextFormatting> formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(() -> coloredString(formatting.get(), stringSupplier.get()).get());
    }

    @SafeVarargs
    public static IKey dynamicLang(TextFormatting formatting, String lang, Supplier<Object>... argSuppliers) {
        if (ArrayUtils.isEmpty(argSuppliers)) return coloredLang(formatting, lang);
        if (argSuppliers.length == 1) return IKey.dynamic(() -> coloredLang(formatting, lang,
                fixArg(formatting, argSuppliers[0].get().toString())).get());
        return IKey.dynamic(() -> {
            Object[] args = new Object[argSuppliers.length];
            for (int i = 0; i < args.length; i++) {
                args[i] = fixArg(formatting, argSuppliers[i].get());
            }
            return coloredLang(formatting, lang, args).get();
        });
    }

    public static IKey dynamicLong(TextFormatting formatting, LongSupplier supplier) {
        return IKey.dynamic(() -> coloredNumber(formatting, supplier.getAsLong()).get());
    }

    public static IKey dynamicLong(TextFormatting formatting, LongSupplier supplier, String suffix) {
        return IKey.dynamic(() -> coloredNumber(formatting, supplier.getAsLong(), suffix).get());
    }

    public static Object[] checkFormatting(TextFormatting formatting, Object[] args) {
        Object[] fixedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            fixedArgs[i] = fixArg(formatting, args[i]);
        }
        return fixedArgs;
    }

    public static Object fixArg(TextFormatting formatting, Object arg) {
        if (arg instanceof IKey key) {
            if (hasFormatting(key.get()))
                return IKey.comp(key, toColor(formatting));
        } else if (arg instanceof String s) {
            if (hasFormatting(s))
                return s + formatting;
        }
        return arg;
    }

    public static boolean hasFormatting(String s) {
        return s.contains(SECTION);
    }
}
