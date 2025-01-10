package gregtech.api.util;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class KeyUtil {

    public static final String SECTION = "§";

    public static IKey string(String s) {
        return IKey.str(s);
    }

    public static IKey string(Supplier<String> s) {
        return IKey.dynamic(s);
    }

    public static IKey string(TextFormatting formatting, String string) {
        if (string == null) return IKey.EMPTY;
        return IKey.str(string).format(formatting);
    }

    public static IKey string(TextFormatting formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(stringSupplier).format(formatting);
    }

    public static IKey string(Supplier<TextFormatting> formatting, String s) {
        return IKey.dynamic(() -> IKey.str(s).format(formatting.get()).getFormatted());
    }

    public static IKey string(Supplier<TextFormatting> formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(() -> IKey.str(stringSupplier.get()).format(formatting.get()).getFormatted());
    }

    public static IKey lang(String lang, Object... args) {
        return IKey.lang(lang, args);
    }

    public static IKey lang(TextFormatting formatting, String lang, Object... args) {
        return IKey.lang(lang, checkFormatting(formatting, args)).format(formatting);
    }

    public static IKey lang(TextFormatting formatting, String lang, Supplier<?>... argSuppliers) {
        if (ArrayUtils.isEmpty(argSuppliers)) return IKey.lang(lang).format(formatting);
        if (argSuppliers.length == 1)
            return IKey.dynamic(
                    () -> IKey.lang(lang, fixArg(formatting, argSuppliers[0].get())).format(formatting).getFormatted());
        final Object[] args = new Object[argSuppliers.length];
        return IKey.dynamic(() -> {
            Arrays.setAll(args, value -> fixArg(formatting, argSuppliers[value].get()));
            return IKey.lang(lang, args).format(formatting).getFormatted();
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

    public static IDrawable setHover(IKey body, IDrawable... hover) {
        if (ArrayUtils.isEmpty(hover)) return body;
        return body.asTextIcon().asHoverable().addTooltipDrawableLines(Arrays.asList(hover));
    }

    private static IKey wrap(TextFormatting formatting) {
        return IKey.str(formatting.toString());
    }

    private static Object[] checkFormatting(TextFormatting formatting, Object[] args) {
        if (ArrayUtils.isEmpty(args)) return args;
        Arrays.setAll(args, value -> fixArg(formatting, args[value]));
        return args;
    }

    private static Object fixArg(TextFormatting formatting, Object arg) {
        if (arg instanceof IKey key) {
            if (hasFormatting(key.getFormatted()))
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
