package gregtech.api.pattern;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;

public class PatternStringError extends PatternError {

    public final String translateKey;

    public PatternStringError(String translateKey) {
        super(new BlockPos(0, 0, 0), Collections.emptyList());
        this.translateKey = translateKey;
    }

    @Override
    public String getErrorInfo() {
        return I18n.format(translateKey);
    }
}
