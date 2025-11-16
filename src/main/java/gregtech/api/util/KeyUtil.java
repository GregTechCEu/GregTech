package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluid;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class KeyUtil {

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

    // maybe enforce using keys for args in lang/string keys since they format correctly
    public static IKey string(TextFormatting formatting, String string, Object... args) {
        if (string == null) return IKey.EMPTY;
        return IKey.str(string, args).style(formatting);
    }

    public static IKey string(TextFormatting formatting, Supplier<String> stringSupplier) {
        return IKey.dynamic(stringSupplier).style(formatting);
    }

    public static IKey string(TextFormatting formatting, Supplier<String> stringSupplier,
                              Supplier<Object[]> argSupplier) {
        return IKey.dynamic(() -> String.format(stringSupplier.get(), argSupplier.get())).style(formatting);
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

    public static IKey number(long number) {
        return string(TextFormattingUtil.formatNumbers(number));
    }

    public static IKey number(TextFormatting formatting, long number) {
        return number(number).style(formatting);
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

    public static IKey compactNumber(@NotNull TextFormatting formatting, @NotNull String prefix, long number,
                                     int precision, @NotNull String suffix) {
        return compactNumber(prefix, number, precision, suffix).style(formatting);
    }

    public static IKey compactNumber(@NotNull String prefix, long number, int precision, @NotNull String suffix) {
        return string(prefix + TextFormattingUtil.formatLongToCompactString(number, precision) + suffix);
    }

    public static IKey compactNumber(@NotNull TextFormatting formatting, @NotNull String prefix, long number,
                                     @NotNull String suffix) {
        return compactNumber(prefix, number, suffix).style(formatting);
    }

    public static IKey compactNumber(@NotNull String prefix, long number, @NotNull String suffix) {
        return string(prefix + TextFormattingUtil.formatLongToCompactString(number) + suffix);
    }

    public static IKey compactNumber(@NotNull TextFormatting formatting, long number, int precision,
                                     @NotNull String suffix) {
        return compactNumber(number, precision, suffix).style(formatting);
    }

    public static IKey compactNumber(long number, int precision, @NotNull String suffix) {
        return string(TextFormattingUtil.formatLongToCompactString(number, precision) + suffix);
    }

    public static IKey compactNumber(@NotNull TextFormatting formatting, long number, @NotNull String suffix) {
        return compactNumber(number, suffix).style(formatting);
    }

    public static IKey compactNumber(long number, @NotNull String suffix) {
        return string(TextFormattingUtil.formatLongToCompactString(number) + suffix);
    }

    public static IKey compactNumber(@NotNull TextFormatting formatting, long number, int precision) {
        return compactNumber(number, precision).style(formatting);
    }

    public static IKey compactNumber(long number, int precision) {
        return string(TextFormattingUtil.formatLongToCompactString(number, precision));
    }

    public static IKey compactNumber(@NotNull TextFormatting formatting, long number) {
        return compactNumber(number).style(formatting);
    }

    public static IKey compactNumber(long number) {
        return string(TextFormattingUtil.formatLongToCompactString(number));
    }

    public static IDrawable setHover(IKey body, IDrawable... hover) {
        if (ArrayUtils.isEmpty(hover)) return body;
        if (!GTValues.isClientSide()) return IDrawable.NONE;
        return body.asTextIcon()
                .asHoverable()
                .addTooltipDrawableLines(Arrays.asList(hover));
    }

    @NotNull
    public static IKey fluid(TextFormatting formatting, FluidStack fluid) {
        return fluid(fluid.getFluid(), fluid).style(formatting);
    }

    @NotNull
    public static IKey fluid(TextFormatting formatting, Fluid fluid) {
        return fluid(fluid).style(formatting);
    }

    @NotNull
    public static IKey fluid(@Nullable FluidStack fluid) {
        if (fluid == null) return IKey.EMPTY;
        return fluid(fluid.getFluid(), fluid);
    }

    @NotNull
    public static IKey fluid(@Nullable Fluid fluid) {
        return fluid(fluid, null);
    }

    @NotNull
    public static IKey fluid(@Nullable Fluid fluid, @Nullable FluidStack stack) {
        if (fluid == null) return IKey.EMPTY;
        if (fluid instanceof GTFluid.GTMaterialFluid gtFluid) {
            return gtFluid.getLocalizedKey();
        }
        if (stack == null) return IKey.lang(fluid.getUnlocalizedName());
        else return IKey.lang(fluid.getUnlocalizedName(stack));
    }

    public static @NotNull IKey item(@NotNull ItemStack itemStack) {
        return IKey.str(itemStack.getDisplayName());
    }
}
