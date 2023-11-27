package gregtech.api.util;

public class ValidationResult<T> {

    private final EnumValidationResult type;
    private final T result;

    public ValidationResult(EnumValidationResult typeIn, T resultIn) {
        this.type = typeIn;
        this.result = resultIn;
    }

    public EnumValidationResult getType() {
        return this.type;
    }

    public T getResult() {
        return this.result;
    }

    public static <T> ValidationResult<T> newResult(EnumValidationResult result, T value) {
        return new ValidationResult<>(result, value);
    }
}
