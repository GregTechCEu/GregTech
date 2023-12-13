package gregtech.api.util.oreglob;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface OreGlobCompiler {

    @NotNull
    OreGlobCompileResult compile(@NotNull String expression, boolean ignoreCase);
}
