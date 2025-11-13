package gregtech.common.command.benchmark;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.NotNull;

public class CommandBenchmarkAbort extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "abort";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.benchmark.abort.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender,
                        String @NotNull [] args) throws CommandException {
        if (CommandBenchmark.ACTIVE_BENCHMARK != null) {
            CommandBenchmark.ACTIVE_BENCHMARK.abort();
            CommandBenchmark.ACTIVE_BENCHMARK = null;
            throw new CommandException("Currently running benchmark successfully aborted.");
        } else {
            throw new CommandException("No benchmark is currently running!");
        }
    }
}
