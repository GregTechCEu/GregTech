package gregtech.api.recipes.roll;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record RollInformation<T> (T value, long rollValue, long rollBoost) {

    public static <T> RollInformation<T> of(T value, long rollValue, long rollBoost) {
        return new RollInformation<>(value, rollValue, rollBoost);
    }
}
