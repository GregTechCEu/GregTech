package gregtech.api.util.oreglob;

import gregtech.api.util.oreglob.OreGlob.VisualizationHint;
import gregtech.api.util.oreglob.OreGlob.Visualizer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of oreGlob visualizer that outputs list of formatted strings as result.
 */
public class OreGlobFormattedStringVisualizer implements Visualizer {

    private final StringBuilder stb = new StringBuilder();
    private final List<String> lines = new ArrayList<>();
    private final String indent;

    @Nullable
    private TextFormatting lastFormatting;

    public OreGlobFormattedStringVisualizer(String indent) {
        this.indent = indent;
    }

    @Override
    public void newLine(int indents) {
        finishLine();
        for (int i = 0; i < indents; i++) {
            this.stb.append(this.indent);
        }
    }

    @Override
    public void text(String text, VisualizationHint hint) {
        format(hint);
        this.stb.append(text);
    }

    @Override
    public void number(int number, VisualizationHint hint) {
        format(hint);
        this.stb.append(number);
    }

    @Override
    public void text(int codePoint, VisualizationHint hint) {
        format(hint);
        this.stb.appendCodePoint(codePoint);
    }

    public List<String> getLines() {
        finishLine();
        return this.lines;
    }

    private void finishLine() {
        this.lines.add(this.stb.append(TextFormatting.RESET).toString());
        this.stb.delete(0, this.stb.length());
        this.lastFormatting = null;
    }

    private void format(VisualizationHint hint) {
        TextFormatting fmt = textFormatting(hint);
        if (this.lastFormatting == fmt) return;
        this.lastFormatting = fmt;
        this.stb.append(fmt);
    }

    private TextFormatting textFormatting(VisualizationHint hint) {
        switch (hint) {
            case PLAINTEXT:
            case LABEL:
                return TextFormatting.DARK_GREEN;
            case NODE:
                return TextFormatting.GREEN;
            case VALUE:
                return TextFormatting.YELLOW;
            case LOGIC_INVERSION:
            case ERROR:
                return TextFormatting.RED;
            default:
                throw new IllegalStateException("Unknown VisualizationHint value '" + hint + "'");
        }
    }
}
