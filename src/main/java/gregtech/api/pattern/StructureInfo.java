package gregtech.api.pattern;

public class StructureInfo {
    protected final PatternMatchContext context;
    protected PatternError error;
    public StructureInfo(PatternMatchContext context, PatternError error) {
        this.context = context;
        this.error = error;
    }

    public PatternError getError() {
        return error;
    }

    public PatternMatchContext getContext() {
        return context;
    }

    public void setError(PatternError error) {
        this.error = error;
    }
}
