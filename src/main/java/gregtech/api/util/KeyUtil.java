package gregtech.api.util;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class KeyUtil {

    public static final String SECTION = "§";

    public static IKey colored(TextFormatting formatting, IKey... keys) {
        if (ArrayUtils.isEmpty(keys)) return wrap(formatting);
        if (keys.length == 1) return IKey.comp(wrap(formatting), keys[0]);
        return IKey.comp(wrap(formatting), IKey.comp(keys));
    }

    public static IKey string(String s) {
        return string(TextFormatting.RESET, s);
    }

    public static IKey string(Supplier<String> s) {
        return string(TextFormatting.RESET, s);
    }

    public static IKey string(TextFormatting formatting, String string) {
        if (string == null) return IKey.EMPTY;
        return IKey.comp(wrap(formatting), IKey.str(string));
    }

    public static IKey string(TextFormatting formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(() -> formatting + stringSupplier.get());
    }

    public static IKey string(Supplier<TextFormatting> formatting, String s) {
        return IKey.dynamic(() -> formatting.get() + s);
    }

    public static IKey string(Supplier<TextFormatting> formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(() -> formatting.get() + stringSupplier.get());
    }

    public static IKey lang(String lang, Object... args) {
        return lang(TextFormatting.RESET, lang, args);
    }

    public static IKey lang(TextFormatting formatting, String lang, Object... args) {
        if (ArrayUtils.isEmpty(args)) return colored(formatting, IKey.lang(lang));
        return colored(formatting, IKey.lang(lang, checkFormatting(formatting, args)));
    }

    public static IKey lang(TextFormatting formatting, String lang, Supplier<?>... argSuppliers) {
        if (ArrayUtils.isEmpty(argSuppliers)) return colored(formatting, IKey.lang(lang));
        if (argSuppliers.length == 1)
            return string(formatting, () -> IKey.lang(lang, fixArg(formatting, argSuppliers[0].get())).get());
        final Object[] fixedArgs = new Object[argSuppliers.length];
        return IKey.dynamic(() -> {
            for (int i = 0; i < fixedArgs.length; i++) {
                fixedArgs[i] = fixArg(formatting, argSuppliers[i].get());
            }
            return formatting + IKey.lang(lang, fixedArgs).get();
        });
    }

    public static IKey lang(Supplier<TextFormatting> formatting, String lang, Supplier<?>... argSuppliers) {
        return IKey.dynamic(() -> lang(formatting.get(), lang, argSuppliers).get());
    }

    public static IKey number(TextFormatting formatting, long number) {
        return string(formatting, TextFormattingUtil.formatNumbers(number));
    }

    public static IKey number(TextFormatting formatting, long number, String suffix) {
        return string(formatting, TextFormattingUtil.formatNumbers(number) + suffix);
    }

    public static IKey number(TextFormatting formatting, LongSupplier supplier) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(supplier.getAsLong()));
    }

    public static IKey number(TextFormatting formatting, LongSupplier supplier, String suffix) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(supplier.getAsLong()) + suffix);
    }

    public static IKey number(Supplier<TextFormatting> formatting, LongSupplier supplier) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(supplier.getAsLong()));
    }

    public static IKey number(Supplier<TextFormatting> formatting, LongSupplier supplier, String suffix) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(supplier.getAsLong()) + suffix);
    }

    private static IKey wrap(TextFormatting formatting) {
        return IKey.str(formatting.toString());
    }

    private static Object[] checkFormatting(TextFormatting formatting, Object[] args) {
        Object[] fixedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            fixedArgs[i] = fixArg(formatting, args[i]);
        }
        return fixedArgs;
    }

    private static Object fixArg(TextFormatting formatting, Object arg) {
        if (arg instanceof IKey key) {
            if (hasFormatting(key.get()))
                return IKey.comp(key, wrap(formatting));
        } else if (arg instanceof String s) {
            if (hasFormatting(s))
                return s + formatting;
        }
        return arg;
    }

    private static boolean hasFormatting(String s) {
        return s.contains(SECTION);
    }
}
