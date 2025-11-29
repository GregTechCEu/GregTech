package gregtech.common.command.benchmark;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandBenchmark extends CommandTreeBase {

    static @Nullable BenchmarkTask ACTIVE_BENCHMARK = null;

    public CommandBenchmark() {
        addSubcommand(new CommandBenchmarkLookup());
        addSubcommand(new CommandBenchmarkAbort());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public @NotNull String getName() {
        return "benchmark";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.benchmark.usage";
    }
}
