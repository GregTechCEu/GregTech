package gregtech.client.renderer.pipe.cache;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Desugar
public record SubListAddress(int startInclusive, int endExclusive) {

    public <T> @NotNull List<T> getSublist(@NotNull List<T> list) {
        return list.subList(startInclusive, endExclusive);
    }
}
