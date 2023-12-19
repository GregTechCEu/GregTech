package gregtech.common.command.worldgen;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;

public class CommandWorldgen extends CommandTreeBase {

    public CommandWorldgen() {
        addSubcommand(new CommandWorldgenReload());
    }

    @NotNull
    @Override
    public String getName() {
        return "worldgen";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.worldgen.usage";
    }
}
