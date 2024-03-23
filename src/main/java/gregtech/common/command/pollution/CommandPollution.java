package gregtech.common.command.pollution;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;

public class CommandPollution extends CommandTreeBase {

    public CommandPollution() {
        addSubcommand(new CommandPollutionAdd());
    }

    @Override
    public @NotNull String getName() {
        return "pollution";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.pollution.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }
}
