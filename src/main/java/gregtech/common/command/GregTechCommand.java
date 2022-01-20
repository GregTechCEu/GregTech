package gregtech.common.command;

import com.google.common.collect.Lists;
import gregtech.api.util.ClipboardUtil;
import gregtech.common.command.worldgen.CommandWorldgen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class GregTechCommand extends CommandTreeBase {

    public GregTechCommand() {
        addSubcommand(new CommandWorldgen());
        addSubcommand(new CommandHand());
        addSubcommand(new CommandRecipeCheck());
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

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            if (args[0].equals("copy")) {
                StringBuilder message = new StringBuilder();

                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]);
                    if (i != args.length - 1)
                        message.append(" ");
                }

                if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
                    ClipboardUtil.copyToClipboard((EntityPlayerMP) sender.getCommandSenderEntity(), message.toString());
                    sender.sendMessage(new TextComponentString("Copied [\u00A76" + message.toString() + "\u00A7r] to the clipboard"));
                }
                return;
            }
            if (args[0].equals("util")) {
                if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
                    sender.sendMessage(new TextComponentString("\u00A76/gt util hand\u00A7r was yeeted. The new command is \u00A76/gt hand\u00A7r"));
                }
                return;
            }
        }
        super.execute(server, sender, args);
    }
}
