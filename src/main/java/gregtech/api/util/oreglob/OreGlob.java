package gregtech.api.util.oreglob;

import gregtech.api.unification.OreDictUnifier;
import net.minecraft.item.ItemStack;

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
    public static OreGlobCompileResult compile(String expression) {
        if (compiler == null) throw new IllegalStateException("Compiler unavailable");
        return compiler.apply(expression);
    }

    public static void setCompiler(Function<String, OreGlobCompileResult> compiler) {
        OreGlob.compiler = compiler;
    }

    /**
     * Visualize this OreGlob instance by appending each text components into given visualizer.
     *
     * @param visualizer Visualizer
     * @param <V>        Type of visualizer
     * @return Visualizer
     */
    public abstract <V extends Visualizer> V visualize(V visualizer);

    /**
     * Tries to match the given input.
     *
     * @param input String input
     * @return Whether this instance matches the input
     */
    public abstract boolean matches(String input);

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
    public final boolean matches(ItemStack stack) {
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
     * Visualize this instance with standard Minecraft text formatting. Two spaces ('  ') will
     * be used as indentation.
     *
     * @return Formatted visualization
     * @see OreGlob#toFormattedString(String)
     */
    public final List<String> toFormattedString() {
        return toFormattedString("  ");
    }

    /**
     * Visualize this instance with standard Minecraft text formatting and provided indentation.
     *
     * @return Formatted visualization
     * @see OreGlob#toFormattedString()
     */
    public final List<String> toFormattedString(String indent) {
        return visualize(new OreGlobFormattedStringVisualizer(indent)).getLines();
    }

    /**
     * Visualize this instance into plaintext string.
     *
     * @return Plaintext visualization
     */
    @Override
    public final String toString() {
        return visualize(new OreGlobStringVisualizer()).toString();
    }

    /**
     * Visualizer accepts text components from OreGlob implementation to create text representation.
     */
    public interface Visualizer {

        void newLine(int indents);

        void text(String text, VisualizationHint hint);

        void number(int number, VisualizationHint hint);

        void text(int codePoint, VisualizationHint hint);

        default void text(String text) {
            text(text, VisualizationHint.PLAINTEXT);
        }

        default void number(int number) {
            number(number, VisualizationHint.VALUE);
        }

        default void text(int codePoint) {
            text(codePoint, VisualizationHint.PLAINTEXT);
        }
    }

    public enum VisualizationHint {
        PLAINTEXT, // Plain text
        NODE, // Text indicating part of a node
        VALUE, // Text indicating some kind of value, whether it's string or number
        NEGATION, // Text indicating logical negation of the statement
        LABEL, // Text indication for each label in group nodes
        ERROR // Text indicating a syntax error; you shouldn't be able to see this
    }
}
