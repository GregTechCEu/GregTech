package gregtech.api.util.oreglob;

import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Visualizer accepts text components from OreGlob implementation to create text representation.
 */
public abstract class OreGlobVisualizer {

    private final List<String> finishedLines = new ArrayList<>();
    private final StringBuilder builder = new StringBuilder();
    private final String indent;

    public OreGlobVisualizer() {
        this("  ");
    }

    public OreGlobVisualizer(@Nonnull String indent) {
        this.indent = indent;
    }

    public void newLine(int indents) {
        finishLine();
        // it's not intellij please shut up
        // noinspection StringRepeatCanBeUsed
        for (int i = 0; i < indents; i++) {
            this.builder.append(this.indent);
        }
    }

    @Nonnull
    public StringBuilder getBuilder() {
        return this.builder;
    }

    private void finishLine() {
        this.finishedLines.add(this.builder.toString());
        this.builder.delete(0, this.builder.length());
    }

    @Nullable
    public abstract TextFormatting getColor(@Nonnull VisualizationHint hint);

    @Nonnull
    public List<String> getLines() {
        finishLine();
        return Collections.unmodifiableList(finishedLines);
    }
}
