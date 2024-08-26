package gregtech.api.pattern;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;

public class PatternStringError extends PatternError {

    public final String translateKey;

    public PatternStringError(BlockPos pos, String translateKey) {
        super(pos, Collections.emptyList());
        this.translateKey = translateKey;
    }

    @Override
    public String getErrorInfo() {
        return I18n.format(translateKey);
    }
}
