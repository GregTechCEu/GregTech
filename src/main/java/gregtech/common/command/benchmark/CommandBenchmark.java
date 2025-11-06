package gregtech.common.command.benchmark;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;

public class CommandBenchmark extends CommandTreeBase {

    public CommandBenchmark() {
        addSubcommand(new CommandBenchmarkLookup());
    }

    @Override
    public @NotNull String getName() {
        return "benchmark";
    }

    @Override
    public @NotNull String getUsage(ICommandSender sender) {
        return "gregtech.command.benchmark.usage";
    }
}
