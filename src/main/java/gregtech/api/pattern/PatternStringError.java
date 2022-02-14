package gregtech.api.pattern;


import gregtech.api.util.LocalizationUtils;

public class PatternStringError extends PatternError{
    public final String translateKey;

    public PatternStringError(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String getErrorInfo() {
        return LocalizationUtils.format(translateKey);
    }
}
