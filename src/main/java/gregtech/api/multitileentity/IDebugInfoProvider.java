package gregtech.api.multitileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IDebugInfoProvider {

    /**
     *
     * @param player the player collecting the info
     * @param logLevel the log level to retrieve the info at
     * @return the debug info
     */
    @Nonnull
    List<ITextComponent> getDebugInfo(@Nullable EntityPlayer player, int logLevel);
}
