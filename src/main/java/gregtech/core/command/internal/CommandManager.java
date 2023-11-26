package gregtech.core.command.internal;

import gregtech.api.command.ICommandManager;

import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.command.CommandTreeBase;

public class CommandManager implements ICommandManager {

    private static final CommandManager INSTANCE = new CommandManager();
    private final CommandTreeBase baseCommand;

    private CommandManager() {
        baseCommand = new GregTechCommand();
    }

    public static CommandManager getInstance() {
        return INSTANCE;
    }

    public void addCommand(ICommand command) {
        baseCommand.addSubcommand(command);
    }

    public void registerServerCommand(FMLServerStartingEvent event) {
        event.registerServerCommand(baseCommand);
    }
}
