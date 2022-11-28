package gregtech.common.command;

import gregtech.api.GregTechAPI;
import gregtech.core.network.packets.PacketReloadShaders;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CommandShaders extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "reloadshaders";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender iCommandSender) {
        return "Reload GTCEu Shaders";
    }

    @Override
    public void execute(@Nonnull MinecraftServer minecraftServer, @Nonnull ICommandSender iCommandSender, @Nonnull String[] strings) {
        if (iCommandSender instanceof EntityPlayerMP) {
            GregTechAPI.networkHandler.sendTo(new PacketReloadShaders(), (EntityPlayerMP) iCommandSender);
            iCommandSender.sendMessage(new TextComponentString("Reloaded Shaders"));
        } else {
            iCommandSender.sendMessage(new TextComponentString("Command cannot be run on the server"));
        }
    }
}
