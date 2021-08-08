package gregtech.common.command.worldgen;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;

public class CommandWorldgen extends CommandTreeBase {

    public CommandWorldgen() {
        addSubcommand(new CommandWorldgenReload());
    }

    @Nonnull
    @Override
    public String getName() {
        return "worldgen";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.worldgen.usage";
    }
}
