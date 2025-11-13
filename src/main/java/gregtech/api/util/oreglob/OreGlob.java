package gregtech.api.util.oreglob;

import gregtech.api.unification.OreDictUnifier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Glob-like string matcher language designed for ore dictionary matching.
 * <p>
 * An OreGlob instance provides two functions: the ability to match strings, and the ability to translate expression
 * structure into user-friendly text explanations. The text can be either a plaintext, or a text formatted by standard
 * Minecraft text format.
 */
public abstract class OreGlob {

    private static OreGlobCompiler compiler;

    /**
     * Tries to compile the string expression into OreGlob instance.
     *
     * @param expression OreGlob expression
     * @param ignoreCase Whether the resulting OreGlob instance should do case-insensitive matches
     * @return Compilation result
     * @throws IllegalStateException If compiler is not provided yet
     */
    @NotNull
    public static OreGlobCompileResult compile(@NotNull String expression, boolean ignoreCase) {
        if (compiler == null) throw new IllegalStateException("Compiler unavailable");
        return compiler.compile(expression, ignoreCase);
    }

    @ApiStatus.Internal
    public static void setCompiler(@NotNull OreGlobCompiler compiler) {
        OreGlob.compiler = compiler;
    }

    /**
     * Visualize this OreGlob instance by appending each text components into given visualizer.
     *
     * @param visualizer Visualizer
     * @param <V>        Type of visualizer
     * @return Visualizer
     */
    @NotNull
    public abstract <V extends OreGlobTextBuilder> V visualize(@NotNull V visualizer);

    /**
     * Tries to match the given input.
     *
     * @param input String input
     * @return Whether this instance matches the input
     */
    public abstract boolean matches(@NotNull String input);

    /**
     * <p>
     * Tries to match each ore dictionary entries associated with given item. If any of them matches, {@code true} is
     * returned.
     * </p>
     * <p>
     * For items not associated with any ore dictionary entries, this method returns {@code true} if this instance
     * matches empty string instead.
     * </p>
     *
     * @param stack Item input
     * @return Whether this instance matches the input
     */
    public final boolean matchesAny(@NotNull ItemStack stack) {
        return matchesAny(OreDictUnifier.getOreDictionaryNames(stack), true);
    }

    /**
     * <p>
     * Tries to match each ore dictionary entries associated with given item. If all of them matches, {@code true} is
     * returned.
     * </p>
     * <p>
     * For items not associated with any ore dictionary entries, this method returns {@code true} if this instance
     * matches empty string instead.
     * </p>
     *
     * @param stack Item input
     * @return Whether this instance matches the input
     */
    public final boolean matchesAll(@NotNull ItemStack stack) {
        return matchesAll(OreDictUnifier.getOreDictionaryNames(stack), true);
    }

    /**
     * <p>
     * Tries to match each input. If any of them matches, {@code true} is returned.
     * </p>
     *
     * @param inputs            Collection of input strings
     * @param specialEmptyMatch If {@code true}, this method will match an empty string ({@code ""}) if the input
     *                          collection is empty. If {@code true}, this method will return {@code false} in such
     *                          scenario.
     * @return Whether this instance matches the input
     */
    public final boolean matchesAny(@NotNull Collection<String> inputs, boolean specialEmptyMatch) {
        if (specialEmptyMatch && inputs.isEmpty()) return matches("");
        for (String input : inputs) if (matches(input)) return true;
        return false;
    }

    /**
     * <p>
     * Tries to match each input. If all of them matches, {@code true} is returned. Note that this method does not have
     * special case for empty inputs.
     * </p>
     *
     * @param inputs            Collection of input strings
     * @param specialEmptyMatch If {@code true}, this method will match an empty string ({@code ""}) if the input
     *                          collection is empty. If {@code true}, this method will return {@code true} in such
     *                          scenario.
     * @return Whether this instance matches the input
     */
    public final boolean matchesAll(@NotNull Collection<String> inputs, boolean specialEmptyMatch) {
        if (specialEmptyMatch && inputs.isEmpty()) return matches("");
        for (String input : inputs) if (!matches(input)) return false;
        return true;
    }

    /**
     * Visualize this instance with standard Minecraft text formatting. Two spaces ({@code '  '}) will be used as
     * indentation.
     *
     * @return Formatted visualization
     * @see OreGlob#toFormattedString(String)
     */
    @NotNull
    public final List<String> toFormattedString() {
        return visualize(new OreGlobTextBuilder(OreGlobTextFormatting.DEFAULT_FORMATTING)).getLines();
    }

    /**
     * Visualize this instance with standard Minecraft text formatting and provided indentation.
     *
     * @return Formatted visualization
     * @see OreGlob#toFormattedString()
     */
    @NotNull
    public final List<String> toFormattedString(@NotNull String indent) {
        return visualize(new OreGlobTextBuilder(OreGlobTextFormatting.DEFAULT_FORMATTING, indent)).getLines();
    }

    /**
     * Visualize this instance into plaintext string.
     *
     * @return Plaintext visualization
     */
    @Override
    public final String toString() {
        return String.join("\n", visualize(new OreGlobTextBuilder(OreGlobTextFormatting.NO_FORMATTING)).getLines());
    }
}
