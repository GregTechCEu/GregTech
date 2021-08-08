package gregtech.common.command.util;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;

public class CommandUtil extends CommandTreeBase {

    public CommandUtil() {
        addSubcommand(new CommandHand());
    }

    @Nonnull
    @Override
    public String getName() {
        return "util";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.util.usage";
    }
}
