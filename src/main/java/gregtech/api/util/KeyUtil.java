package gregtech.api.util;

import gregtech.api.mui.drawables.HoverableKey;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import org.apache.commons.lang3.ArrayUtils;

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
        return IKey.str(string).style(formatting);
    }

    public static IKey string(TextFormatting formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(stringSupplier).style(formatting);
    }

    public static IKey string(Supplier<TextFormatting> formatting, String s) {
        return IKey.dynamic(() -> IKey.str(s).style(formatting.get()).getFormatted());
    }

    public static IKey string(Supplier<TextFormatting> formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(() -> IKey.str(stringSupplier.get()).style(formatting.get()).getFormatted());
    }

    public static IKey lang(String lang, Object... args) {
        return IKey.lang(lang, args);
    }

    public static IKey lang(TextFormatting formatting, String lang, Object... args) {
        return IKey.lang(lang, args).style(formatting);
    }

    public static IKey lang(TextFormatting formatting, String lang, Supplier<Object[]> argSupplier) {
        return IKey.lang(lang, argSupplier).style(formatting);
    }

    public static IKey lang(Supplier<TextFormatting> formatting, String lang, Supplier<Object[]> argSupplier) {
        return IKey.dynamic(() -> lang(lang, argSupplier.get()).style(formatting.get()).getFormatted());
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

    public static IKey number(Supplier<TextFormatting> formatting, long number) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(number));
    }

    public static IKey number(Supplier<TextFormatting> formatting, long number, String suffix) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(number) + suffix);
    }

    public static IKey number(Supplier<TextFormatting> formatting, LongSupplier supplier, String suffix) {
        return string(formatting, () -> TextFormattingUtil.formatNumbers(supplier.getAsLong()) + suffix);
    }

    public static IDrawable setHover(IKey body, IDrawable... hover) {
        if (ArrayUtils.isEmpty(hover)) return body;
        return HoverableKey.of(body, hover);
    }
}
