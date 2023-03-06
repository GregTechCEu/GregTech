package gregtech.api.util.oreglob;

import java.util.List;
import java.util.function.Function;

/**
 * Representation of ore filter expression.
 */
public abstract class OreGlob {

    private static Function<String, OreGlobCompileResult> compiler;

    public static OreGlobCompileResult compile(String expression) {
        if (compiler == null) throw new IllegalStateException("Compiler unavailable");
        return compiler.apply(expression);
    }

    public static void setCompiler(Function<String, OreGlobCompileResult> compiler) {
        OreGlob.compiler = compiler;
    }

    public abstract <V extends Visualizer> V visualize(V visualizer);

    public abstract boolean matches(String input);

    public final List<String> toFormattedString() {
        return toFormattedString("  ");
    }

    public final List<String> toFormattedString(String indent) {
        return visualize(new OreGlobFormattedStringVisualizer(indent)).getLines();
    }

    @Override
    public final String toString() {
        return visualize(new OreGlobStringVisualizer()).toString();
    }

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
        LOGIC_INVERSION, // Text indicating logical inversion of the statement
        LABEL, // Text indication for each label in group nodes
        ERROR // Text indicating a syntax error; you shouldn't be able to see this
    }
}
