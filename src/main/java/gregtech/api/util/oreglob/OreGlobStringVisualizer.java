package gregtech.api.util.oreglob;

import gregtech.api.util.oreglob.OreGlob.VisualizationHint;
import gregtech.api.util.oreglob.OreGlob.Visualizer;

/**
 * Implementation of oreGlob visualizer that outputs String as result.
 */
public class OreGlobStringVisualizer implements Visualizer {

    private final StringBuilder stb = new StringBuilder();
    private final String indent;

    public OreGlobStringVisualizer() {
        this("  ");
    }

    public OreGlobStringVisualizer(String indent) {
        this.indent = indent;
    }

    @Override
    public void newLine(int indents) {
        this.stb.append('\n');
        for (int i = 0; i < indents; i++) {
            this.stb.append(this.indent);
        }
    }

    @Override
    public void text(String text, VisualizationHint hint) {
        this.stb.append(text);
    }

    @Override
    public void number(int number, VisualizationHint hint) {
        this.stb.append(number);
    }

    @Override
    public void text(int codePoint, VisualizationHint hint) {
        this.stb.appendCodePoint(codePoint);
    }

    @Override
    public String toString() {
        return this.stb.toString();
    }
}
