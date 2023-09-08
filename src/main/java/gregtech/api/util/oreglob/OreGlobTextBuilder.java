package gregtech.api.util.oreglob;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Builder for OreGlob instance visualization.
 */
public class OreGlobTextBuilder {

    private final List<String> finishedLines = new ArrayList<>();
    private final StringBuilder builder = new StringBuilder();
    private final OreGlobTextFormatting formatting;
    private final String indent;

    public OreGlobTextBuilder(@Nonnull OreGlobTextFormatting formatting) {
        this(formatting, "  ");
    }

    public OreGlobTextBuilder(@Nonnull OreGlobTextFormatting formatting, @Nonnull String indent) {
        this.formatting = Objects.requireNonNull(formatting, "formatting == null");
        this.indent = Objects.requireNonNull(indent, "indent == null");
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
    public StringBuilder getStringBuilder() {
        return this.builder;
    }

    private void finishLine() {
        this.finishedLines.add(this.builder.toString());
        this.builder.delete(0, this.builder.length());
    }

    @Nonnull
    public OreGlobTextFormatting getFormatting() {
        return formatting;
    }

    @Nonnull
    public List<String> getLines() {
        finishLine();
        return Collections.unmodifiableList(finishedLines);
    }

}
