package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluid;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.StyledText;
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

    /**
     * Create an {@link IKey} that dynamically shows the result of {@link GTUtility#getButtonIncrementValue()}. <br/>
     * Example: player is holding down ctrl and {@code positive} is {@code true} = {@code +16}. <br/>
     * 
     * @param positive if the prefix should be {@code +} or {@code -}
     */
    @NotNull
    public static IKey createMultiplierKey(boolean positive) {
        Object[] args = new Object[] { positive ? '+' : '-', 0 };
        StyledText key = IKey.str("%c%d", args)
                .withStyle();

        // Using the color supplier here as a callback to update the multiplier and scale before it gets rendered.
        key.color(() -> {
            int multiplier = GTUtility.getButtonIncrementValue();
            args[1] = multiplier;

            if (multiplier < 10) {
                key.scale(1.0f);
            } else if (multiplier < 100) {
                key.scale(0.8f);
            } else if (multiplier < 1000) {
                key.scale(0.6f);
            } else {
                key.scale(0.5f);
            }

            return -1;
        });

        return key;
    }
}
