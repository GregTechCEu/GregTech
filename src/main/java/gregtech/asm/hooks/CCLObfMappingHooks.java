package gregtech.asm.hooks;

import gregtech.asm.util.ObfMapping;

import javax.annotation.Nonnull;
import java.io.File;

public final class CCLObfMappingHooks {

    private CCLObfMappingHooks() {/**/}

    @SuppressWarnings("unused")
    @Nonnull
    public static File[] getConfFiles() {
        File[] ret = new File[3];
        System.arraycopy(ObfMapping.MCPRemapper.getConfFiles(), 0, ret, 1, 2);
        return ret;
    }
}
