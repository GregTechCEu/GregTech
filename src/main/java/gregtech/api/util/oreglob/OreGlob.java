package gregtech.api.util.oreglob;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Representation of ore filter expression.
 */
public final class OreGlob implements OreEvaluator {

    private static Function<String, OreGlobCompileResult> compiler;

    public static OreGlobCompileResult compile(String expression) {
        if (compiler == null) throw new IllegalStateException("Compiler unavailable");
        return compiler.apply(expression);
    }

    public static void setCompiler(Function<String, OreGlobCompileResult> compiler) {
        OreGlob.compiler = compiler;
    }

    private final Consumer<Visualizer> visualRepresentation;
    private final OreEvaluator evaluator;

    public OreGlob(Consumer<Visualizer> visualRepresentation, OreEvaluator evaluator) {
        this.visualRepresentation = visualRepresentation;
        this.evaluator = evaluator;
    }

    public <V extends Visualizer> V visualize(V visualizer) {
        this.visualRepresentation.accept(visualizer);
        return visualizer;
    }

    @Override
    public boolean matches(String input) {
        return evaluator.matches(input);
    }

    @Override
    public String toString() {
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
