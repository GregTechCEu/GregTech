package gregtech.datafix.command;

import gregtech.api.util.Mods;
import gregtech.integration.bq.CommandBQuDataFix;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;

public final class CommandDataFix extends CommandTreeBase {

    public CommandDataFix() {
        if (Mods.BetterQuestingUnofficial.isModLoaded()) {
            addSubcommand(new CommandBQuDataFix());
        }
    }

    @Override
    public @NotNull String getName() {
        return "datafix";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.datafix.usage";
    }
}
