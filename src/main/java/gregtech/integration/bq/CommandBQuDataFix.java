package gregtech.integration.bq;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@ApiStatus.Internal
public final class CommandBQuDataFix extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "bqu";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.datafix.bqu.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) {
        if (args.length < 1 || !"confirm".equalsIgnoreCase(args[0])) {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.datafix.bqu.backup")
                    .setStyle(new Style().setColor(TextFormatting.YELLOW)));
            return;
        }

        sender.sendMessage(new TextComponentTranslation("gregtech.command.datafix.bqu.start")
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));

        Path worldDir = server.getEntityWorld().getSaveHandler().getWorldDirectory().toPath();

        if (BQuDataFixer.processConfigDir(worldDir) && BQuDataFixer.processWorldDir(worldDir)) {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.datafix.bqu.complete")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.datafix.bqu.failed")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }
}
