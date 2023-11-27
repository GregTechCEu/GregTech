package gregtech.api.util.oreglob;

import gregtech.api.unification.OreDictUnifier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Glob-like string matcher language designed for ore dictionary matching.
 * <p>
 * An OreGlob instance provides two functions: the ability to match strings,
 * and the ability to translate expression structure into user-friendly text
 * explanations. The text can be either a plaintext, or a text formatted by standard
 * Minecraft text format.
 */
public abstract class OreGlob {

    private static Function<String, OreGlobCompileResult> compiler;

    /**
     * Tries to compile the string expression into OreGlob instance.
     *
     * @param expression OreGlob expression
     * @return Compilation result
     * @throws IllegalStateException If compiler is not provided yet
     */
    @NotNull
    public static OreGlobCompileResult compile(@NotNull String expression) {
        if (compiler == null) throw new IllegalStateException("Compiler unavailable");
        return compiler.apply(expression);
    }

    @ApiStatus.Internal
    public static void setCompiler(@NotNull Function<String, OreGlobCompileResult> compiler) {
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
     * Tries to match each ore dictionary entries associated with given item.
     * If any of them matches, {@code true} is returned.
     * <p>
     * For items not associated with any ore dictionary entries, this method returns
     * {@code true} if this instance matches empty string instead.
     *
     * @param stack Item input
     * @return Whether this instance matches the input
     */
    public final boolean matches(@NotNull ItemStack stack) {
        Set<String> oreDicts = OreDictUnifier.getOreDictionaryNames(stack);
        if (oreDicts.isEmpty()) {
            return matches("");
        } else {
            for (String oreDict : oreDicts) {
                if (matches(oreDict)) return true;
            }
            return false;
        }
    }

    /**
     * Visualize this instance with standard Minecraft text formatting. Two spaces (' ') will
     * be used as indentation.
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
