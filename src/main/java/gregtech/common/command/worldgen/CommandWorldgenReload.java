package gregtech.common.command.worldgen;

import gregtech.api.util.GTLog;
import gregtech.api.worldgen.config.WorldGenRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.io.IOException;

public class CommandWorldgenReload extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "reload";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.worldgen.reload.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        try {
            WorldGenRegistry.INSTANCE.reinitializeRegisteredVeins();
            sender.sendMessage(new TextComponentTranslation("gregtech.command.worldgen.reload.success")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } catch (IOException | RuntimeException exception) {
            GTLog.logger.error("Failed to reload worldgen config", exception);
            sender.sendMessage(new TextComponentTranslation("gregtech.command.worldgen.reload.failed")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }
}
