package gregtech.common.command.pollution;

import gregtech.api.GregTechAPI;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class CommandPollutionAdd extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "add";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.pollution.add.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) {
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.pollution.invalid.value", args[0]));
            return;
        }

        World world = sender.getEntityWorld();
        BlockPos pos = sender.getPosition();
        GregTechAPI.pollutionManager.changePollution(world.provider.getDimension(), pos.getX() >> 4, pos.getZ() >> 4, amount);
    }
}
