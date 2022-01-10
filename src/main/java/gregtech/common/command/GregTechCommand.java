package gregtech.common.command;

import com.google.common.collect.Lists;
import gregtech.common.command.util.CommandHand;
import gregtech.common.command.util.CommandUtil;
import gregtech.common.command.worldgen.CommandWorldgen;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;
import java.util.List;

public class GregTechCommand extends CommandTreeBase {

    public GregTechCommand() {
        addSubcommand(new CommandWorldgen());
        addSubcommand(new CommandUtil());
        addSubcommand(new CommandHand());
    }

    @Nonnull
    @Override
    public String getName() {
        return "gregtech";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("gt");
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.usage";
    }
}
