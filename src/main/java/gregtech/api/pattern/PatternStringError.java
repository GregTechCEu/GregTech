package gregtech.api.pattern;

import net.minecraft.client.resources.I18n;

public class PatternStringError extends PatternError {

    public final String translateKey;

    public PatternStringError(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String getErrorInfo() {
        return I18n.format(translateKey);
    }
}
