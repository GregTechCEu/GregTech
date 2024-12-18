package gregtech.api.util.math;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Desugar
public record Vec2i(int x, int y) implements Comparable<Vec2i> {

    private static final Comparator<Vec2i> comparator = Comparator.comparingInt(Vec2i::x)
            .thenComparing(Vec2i::y);

    @Override
    public int compareTo(@NotNull Vec2i o) {
        return comparator.compare(this, o);
    }
}
